/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.annotations.TransientDataService;
import org.ocelotds.marshallers.TemplateMarshaller;
import org.ocelotds.marshalling.annotations.JsonUnmarshaller;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
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

	@Inject
	private TemplateMarshaller templateMarshaller;

	/**
	 * Return classname but in short formal java.lang.Collection&lt;java.lang.String&gt; to Collection&lt;String&gt;
	 *
	 * @param fullformat
	 * @return
	 */
	public String getShortName(String fullformat) {
		if (fullformat != null) {
			return fullformat.replaceAll("(\\w)*\\.", "");
		}
		return "";
	}

	/**
	 * Return human reading of type
	 *
	 * @param type
	 * @return
	 */
	public String getLiteralType(Type type) {
		String result = "";
		if (type != null) {
			if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				result = parameterizedType.toString();
			} else {
				result = ((Class) type).getCanonicalName();
			}
		}
		return result;
	}

	/**
	 * Return JsonUnmarshaller instance if annotation JsonUnmarshaller is present
	 *
	 * @param annotations
	 * @return
	 */
	public org.ocelotds.marshalling.IJsonMarshaller getJsonMarshaller(Annotation[] annotations) {
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (JsonUnmarshaller.class.isAssignableFrom(annotation.annotationType())) {
					return getJsonMarshallerFromAnnotation((JsonUnmarshaller) annotation);
				}
			}
		}
		return null;
	}

	/**
	 * Return JsonUnmarshaller instance from annotation JsonUnmarshaller
	 *
	 * @param jua
	 * @return
	 */
	public org.ocelotds.marshalling.IJsonMarshaller getJsonMarshallerFromAnnotation(JsonUnmarshaller jua) {
		if (jua != null) {
			Class<? extends org.ocelotds.marshalling.IJsonMarshaller> juCls = jua.value();
			return getCDICurrentSelect(juCls);
		}
		return null;
	}

	/**
	 * Get template for unknown type (class or parameterizedType)
	 *
	 * @param type
	 * @param jsonMarshaller
	 * @return
	 */
	public String getTemplateOfType(Type type, org.ocelotds.marshalling.IJsonMarshaller jsonMarshaller) {
		if (jsonMarshaller == null) {
			jsonMarshaller = templateMarshaller;
		}
		return _getTemplateOfType(type, jsonMarshaller);
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
	 * @return
	 */
//	String _getTemplateOfType(Type type) {
//		if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
//			return getTemplateOfParameterizedType((ParameterizedType) type);
//		}
//		return getTemplateOfClass((Class) type);
//	}
	/**
	 * Get template for unknown type (class or parameterizedType)
	 *
	 * @param type
	 * @param jsonUnmarshaller
	 * @return
	 */
	String _getTemplateOfType(Type type, org.ocelotds.marshalling.IJsonMarshaller jsonMarshaller) {
		if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
			return getTemplateOfParameterizedType((ParameterizedType) type, jsonMarshaller);
		}
		try {
			return jsonMarshaller.toJson(getInstanceOfClass((Class) type));
		} catch (JsonMarshallingException ex) {
		}
		return ((Class) type).getSimpleName().toLowerCase(Locale.ENGLISH);
	}

	/**
	 * Get instance for classic class
	 *
	 * @param cls
	 * @return
	 */
	Object getInstanceOfClass(Class cls) {
		if (Boolean.class.isAssignableFrom(cls) || Boolean.TYPE.isAssignableFrom(cls)) {
			return Boolean.FALSE;
		} else if (Integer.TYPE.isAssignableFrom(cls) || Short.TYPE.isAssignableFrom(cls) || Integer.class.isAssignableFrom(cls) || Short.class.isAssignableFrom(cls)) {
			return 0;
		} else if (Long.TYPE.isAssignableFrom(cls) || Long.class.isAssignableFrom(cls)) {
			return 0L;
		} else if (Float.TYPE.isAssignableFrom(cls) || Float.class.isAssignableFrom(cls)) {
			return 0.1F;
		} else if (Double.TYPE.isAssignableFrom(cls) || Double.class.isAssignableFrom(cls)) {
			return 0.1D;
		} else if (cls.isArray()) {
			Class<?> comp = cls.getComponentType();
			Object instance = getInstanceOfClass(comp);
			return new Object[]{instance, instance};
		} else {
			try {
				return cls.newInstance();
			} catch (InstantiationException | IllegalAccessException ex) {
				return getObjectFromConstantFields(cls);
			}
		}
	}

	/**
	 * If the class has not empty constructor, look static field if it return instance
	 *
	 * @param cls
	 * @return
	 */
	Object getObjectFromConstantFields(Class cls) {
		Field[] fields = cls.getFields();
		Object instance = null;
		for (Field field : fields) {
			instance = getObjectFromConstantField(cls, field);
			if(instance!=null) {
				break;
			}
		}
		return instance;
	}

	Object getObjectFromConstantField(Class cls, Field field) {
		Object instance = null;
		if (Modifier.isStatic(field.getModifiers()) && field.getType().isAssignableFrom(cls)) {
			try {
				instance = getConstantFromField(field);
			} catch (IllegalArgumentException | IllegalAccessException ex1) {
			}
		}
		return instance;
	}
	
	Object getConstantFromField(Field field) throws IllegalArgumentException, IllegalAccessException {
		return field.get(null);
	}

	/**
	 * Get template for classic class
	 *
	 * @param cls
	 * @return
	 */
//	String getTemplateOfClass(Class cls) {
//		if (Boolean.class.isAssignableFrom(cls) || Boolean.TYPE.isAssignableFrom(cls)) {
//			return "false";
//		} else if (Integer.TYPE.isAssignableFrom(cls) || Long.TYPE.isAssignableFrom(cls) || Integer.class.isAssignableFrom(cls) || Long.class.isAssignableFrom(cls)) {
//			return "0";
//		} else if (Float.TYPE.isAssignableFrom(cls) || Double.TYPE.isAssignableFrom(cls) || Float.class.isAssignableFrom(cls) || Double.class.isAssignableFrom(cls)) {
//			return "0.0";
//		} else if (cls.isArray()) {
//			String template = getTemplateOfClass(cls.getComponentType());
//			return "[" + template + "," + template + "]";
//		} else {
//			try {
//				return getObjectMapper().writeValueAsString(cls.newInstance());
//			} catch (InstantiationException | IllegalAccessException | JsonProcessingException ex) {
//			}
//		}
//		return cls.getSimpleName().toLowerCase(Locale.ENGLISH);
//	}
	/**
	 * Get template from parameterizedType
	 *
	 * @param parameterizedType
	 * @return
	 */
	String getTemplateOfParameterizedType(ParameterizedType parameterizedType, org.ocelotds.marshalling.IJsonMarshaller jsonMarshaller) {
		Class cls = (Class) parameterizedType.getRawType();
		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		String res;
		if (Iterable.class.isAssignableFrom(cls)) {
			res = getTemplateOfIterable(actualTypeArguments, jsonMarshaller);
		} else if (Map.class.isAssignableFrom(cls)) {
			res = getTemplateOfMap(actualTypeArguments, jsonMarshaller);
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
	String getTemplateOfIterable(Type[] actualTypeArguments, org.ocelotds.marshalling.IJsonMarshaller jsonMarshaller) {
		String res = "[";
		for (Type actualTypeArgument : actualTypeArguments) {
			String template = _getTemplateOfType(actualTypeArgument, jsonMarshaller);
			res += template + "," + template;
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
	String getTemplateOfMap(Type[] actualTypeArguments, org.ocelotds.marshalling.IJsonMarshaller jsonMarshaller) {
		StringBuilder res = new StringBuilder("{");
		boolean first = true;
		for (Type actualTypeArgument : actualTypeArguments) {
			if (!first) {
				res.append(":");
			}
			res.append(_getTemplateOfType(actualTypeArgument, jsonMarshaller));
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
	 *
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

	<T> T getCDICurrentSelect(Class<T> cls) {
		return CDI.current().select(cls).get();
	}
}
