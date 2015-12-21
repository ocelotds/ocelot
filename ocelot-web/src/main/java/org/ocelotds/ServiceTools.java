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
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.annotations.TransientDataService;
import org.ocelotds.marshalling.annotations.JsonUnmarshaller;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
public class ServiceTools {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	private ObjectMapper objectMapper;

	/**
	 * Return classname but in short formal java.lang.Collection&lt;java.lang.String&gt; to Collection&lt;String&gt;
	 *
	 * @param fullformat
	 * @return
	 */
	public String getShortName(String fullformat) {
		return fullformat.replaceAll("(\\w)*\\.", "");
	}

	/**
	 * Return human reading of type
	 *
	 * @param type
	 * @return
	 */
	public String getLiteralType(Type type) {
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
	 *
	 * @param annotations
	 * @return
	 */
	public org.ocelotds.marshalling.JsonUnmarshaller getJsonUnmarshaller(Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if (JsonUnmarshaller.class.isAssignableFrom(annotation.annotationType())) {
				JsonUnmarshaller jua = (JsonUnmarshaller) annotation;
				Class<? extends org.ocelotds.marshalling.JsonUnmarshaller> juCls = jua.value();
				try {
					return juCls.newInstance();
				} catch (InstantiationException | IllegalAccessException ex) {
					logger.error("Fail to instanciate " + juCls.getName(), ex);
				}
			}
		}
		return null;
	}

	/**
	 * Get template for unknown type (class or parameterizedType)
	 *
	 * @param type
	 * @param jsonUnmarshaller
	 * @return
	 */
	public String getTemplateOfType(Type type, org.ocelotds.marshalling.JsonUnmarshaller jsonUnmarshaller) {
		if (jsonUnmarshaller != null) {
			logger.debug("Class {} is annotated with jsonUnmarshaller {}", type, jsonUnmarshaller);
		}
		return _getTemplateOfType(type);
	}

	/**
	 * Get instancename from Dataservice Class
	 *
	 * @param cls
	 * @return
	 */
	public String getInstanceNameFromDataservice(Class cls) {
		DataService dataService = (DataService) cls.getAnnotation(DataService.class);
		String clsName = dataService.name();
		if (clsName.isEmpty()) {
			clsName = cls.getSimpleName();
		}
		return getInstanceName(clsName);
	}

	/**
	 * Get class without proxy CDI based on $ separator
	 *
	 * @param proxy
	 * @return
	 */
	public Class getRealClass(Class proxy) {
		try {
			return Class.forName(getRealClassname(proxy.getName()));
		} catch (ClassNotFoundException ex) {
		}
		return proxy;
	}
	
	/**
	 * Method is expose to frontend ?
	 *
	 * @param method
	 * @return
	 */
	public boolean isConsiderateMethod(Method method) {
		if (method.isAnnotationPresent(TransientDataService.class) || method.getDeclaringClass().isAssignableFrom(Object.class)) {
			return false;
		}
		int modifiers = method.getModifiers();
		return Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers);
	}

	/**
	 * Get template for unknown type (class or parameterizedType)
	 *
	 * @param type
	 * @param jsonUnmarshaller
	 * @return
	 */
	String _getTemplateOfType(Type type) {
		if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
			return getTemplateOfParameterizedType((ParameterizedType) type);
		}
		return getTemplateOfClass((Class) type);
	}

	/**
	 * Get template for classic class
	 *
	 * @param cls
	 * @param jsonUnmarshaller
	 * @return
	 */
	String getTemplateOfClass(Class cls) {
		if (Boolean.class.isAssignableFrom(cls) || Boolean.TYPE.isAssignableFrom(cls)) {
			return "false";
		} else if (Integer.TYPE.isAssignableFrom(cls) || Long.TYPE.isAssignableFrom(cls) || Integer.class.isAssignableFrom(cls) || Long.class.isAssignableFrom(cls)) {
			return "0";
		} else if (Float.TYPE.isAssignableFrom(cls) || Double.TYPE.isAssignableFrom(cls) || Float.class.isAssignableFrom(cls) || Double.class.isAssignableFrom(cls)) {
			return "0.0";
		} else if (cls.isArray()) {
			String template = getTemplateOfClass(cls.getComponentType());
			return "[" + template + "," + template + "]";
		} else {
			try {
				return getObjectMapper().writeValueAsString(cls.newInstance());
			} catch (InstantiationException | IllegalAccessException | JsonProcessingException ex) {
			}
		}
		return cls.getSimpleName().toLowerCase(Locale.ENGLISH);
	}

	/**
	 * Get template from parameterizedType
	 *
	 * @param parameterizedType
	 * @return
	 */
	String getTemplateOfParameterizedType(ParameterizedType parameterizedType) {
		Class cls = (Class) parameterizedType.getRawType();
		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		String res;
		if (Iterable.class.isAssignableFrom(cls)) {
			res = getTemplateOfIterable(actualTypeArguments);
		} else if (Map.class.isAssignableFrom(cls)) {
			res = getTemplateOfMap(actualTypeArguments);
		} else {
			res = cls.getSimpleName().toLowerCase(Locale.ENGLISH);
		}
		return res;
	}

	/**
	 * Get template of iterable class from generic type
	 *
	 * @param actualTypeArguments
	 * @return
	 */
	String getTemplateOfIterable(Type[] actualTypeArguments) {
		String res = "[";
		for (Type actualTypeArgument : actualTypeArguments) {
			String template = _getTemplateOfType(actualTypeArgument);
			res += template+","+template;
			break;
		}
		return res + "]";
	}

	/**
	 * Get template of map class from generic type
	 *
	 * @param actualTypeArguments
	 * @return
	 */
	String getTemplateOfMap(Type[] actualTypeArguments) {
		StringBuilder res = new StringBuilder("{");
		boolean first = true;
		for (Type actualTypeArgument : actualTypeArguments) {
			if (!first) {
				res.append(":");
			}
			res.append(_getTemplateOfType(actualTypeArgument));
			first = false;
		}
		res.append("}");
		return res.toString();
	}

	/**
	 * Get instancename from clasname
	 *
	 * @param clsName
	 * @return
	 */
	String getInstanceName(String clsName) {
		return clsName.substring(0, 1).toLowerCase(Locale.ENGLISH) + clsName.substring(1);
	}
	
	/**
	 * Expose objectMapper
	 * @return 
	 */
	ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	/**
	 * Get class without proxy CDI based on $ separator
	 *
	 * @param proxyname 
	 * @return
	 * @throws java.lang.ClassNotFoundException
	 */
	String getRealClassname(String proxyname) throws ClassNotFoundException {
		int index = proxyname.indexOf('$');
		if (index != -1) {
			return proxyname.substring(0, index);
		} else {
			throw new ClassNotFoundException();
		}
	}
}
