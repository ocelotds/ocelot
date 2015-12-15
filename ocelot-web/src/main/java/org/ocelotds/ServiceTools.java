/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import javax.inject.Inject;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.TransientDataService;
import org.ocelotds.marshalling.annotations.JsonUnmarshaller;

/**
 *
 * @author hhfrancois
 */
public class ServiceTools {

	@Inject
	private ObjectMapper objectMapper;

	/**
	 * Return classname but in short formal
	 * java.lang.Collection&lt;java.lang.String&gt; to Collection&lt;String&gt;
	 * @param fullformat
	 * @return 
	 */
	String getShortName(String fullformat) {
		return fullformat.replaceAll("(\\w)*\\.", "");
	}
	
	/**
	 * Return human reading of type
	 * @param type
	 * @return 
	 */
	String getLiteralType(Type type) {
		String result;
		if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			result = parameterizedType.toString();
		} else {
			result = ((Class) type).getCanonicalName();
		}
		return result;
	}

	/**
	 * The parameter is Annotation JsonUnmarshaller present
	 * @param annotations
	 * @return 
	 */
	boolean isJsonUnmarshallerAnnotationPresent(Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if (JsonUnmarshaller.class.isAssignableFrom(annotation.annotationType())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get template for unknown type (class or parameterizedType)
	 * @param type
	 * @param jsonUnmarshaller
	 * @return 
	 */
	String getTemplateOfType(Type type, boolean jsonUnmarshaller) {
		if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
			return getTemplateOfParameterizedType((ParameterizedType) type);
		}
		return getTemplateOfClass((Class) type, jsonUnmarshaller);
	}

	/**
	 * Get template for classic class
	 * @param cls
	 * @param jsonUnmarshaller
	 * @return 
	 */
	String getTemplateOfClass(Class cls, boolean jsonUnmarshaller) {
		if (Boolean.class.isAssignableFrom(cls) || Boolean.TYPE.isAssignableFrom(cls)) {
			return "false";
		} else if (Integer.TYPE.isAssignableFrom(cls) || Long.TYPE.isAssignableFrom(cls) || Integer.class.isAssignableFrom(cls) || Long.class.isAssignableFrom(cls)) {
			return "0";
		} else if (Float.TYPE.isAssignableFrom(cls) || Double.TYPE.isAssignableFrom(cls) || Float.class.isAssignableFrom(cls) || Double.class.isAssignableFrom(cls)) {
			return "0.0";
		} else if (cls.isArray()) {
			return "[" + getTemplateOfClass(cls.getComponentType(), false) + "]";
		} else if (jsonUnmarshaller) {
			System.out.println("Object jsonUnmarshaller");
		} else {
			try {
				return objectMapper.writeValueAsString(cls.newInstance());
			} catch (InstantiationException | IllegalAccessException | JsonProcessingException ex) {
			}
		}
		return cls.getSimpleName().toLowerCase();
	}

	/**
	 * Get template from parameterizedType
	 * @param parameterizedType
	 * @return 
	 */
	String getTemplateOfParameterizedType(ParameterizedType parameterizedType) {
		Class cls = (Class) parameterizedType.getRawType();
		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		if (Iterable.class.isAssignableFrom(cls)) {
			return getTemplateOfIterable(actualTypeArguments);
		} else if (Map.class.isAssignableFrom(cls)) {
			return getTemplateOfMap(actualTypeArguments);
		}
		return cls.toString();
	}

	/**
	 * Get template of iterable class from generic type
	 * @param actualTypeArguments
	 * @return 
	 */
	String getTemplateOfIterable(Type[] actualTypeArguments) {
		String res = "[";
		for (Type actualTypeArgument : actualTypeArguments) {
			res += getTemplateOfType(actualTypeArgument, false);
		}
		return res + "]";
	}

	/**
	 * Get template of map class from generic type
	 * @param actualTypeArguments
	 * @return 
	 */
	String getTemplateOfMap(Type[] actualTypeArguments) {
		String res = "{";
		boolean first = true;
		for (Type actualTypeArgument : actualTypeArguments) {
			if (!first) {
				res += ":";
			}
			res += getTemplateOfType(actualTypeArgument, false);
			first = false;
		}
		return res + "}";
	}

	/**
	 * Get instancename from Dataservice Class
	 * @param cls
	 * @return 
	 */
	String getInstanceNameFromDataservice(Class cls) {
		DataService dataService = (DataService) cls.getAnnotation(DataService.class);
		String clsName = dataService.name();
		if (clsName.isEmpty()) {
			clsName = cls.getSimpleName();
		}
		return getInstanceName(clsName);
	}

	/**
	 * Get instancename from clasname
	 * @param clsName
	 * @return 
	 */
	String getInstanceName(String clsName) {
		return clsName.substring(0, 1).toLowerCase() + clsName.substring(1);
	}

	/**
	 * Get class without proxy CDI
	 * based on $ separator
	 * @param proxy
	 * @return 
	 */
	Class getRealClass(Class proxy) {
		String proxyname = proxy.getName();
		int index = proxyname.indexOf("$");
		if (index != -1) {
			try {
				return Class.forName(proxyname.substring(0, index));
			} catch (ClassNotFoundException ex) {
			}
		}
		return proxy;
	}

	/**
	 * Method is expose to frontend ?
	 * @param method
	 * @return 
	 */
	boolean isConsiderateMethod(Method method) {
		if (method.isAnnotationPresent(TransientDataService.class)) {
			return false;
		}
		int modifiers = method.getModifiers();
		return Modifier.isPublic(modifiers) && !Modifier.isAbstract(modifiers) && !Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers);
	}
}
