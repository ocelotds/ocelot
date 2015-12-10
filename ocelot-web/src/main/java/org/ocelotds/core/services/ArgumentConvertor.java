/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.services;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.SimpleType;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.json.stream.JsonParsingException;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.marshalling.annotations.JsonUnmarshaller;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@Default
public class ArgumentConvertor implements IArgumentConvertor {
	@Inject
	@OcelotLogger
	private Logger logger;

	/**
	 * Convert json to Java
	 *
	 * @param jsonArg
	 * @param paramType
	 * @param parameterAnnotations
	 * @return
	 * @throws JsonUnmarshallingException
	 */
	@Override
	public Object convertJsonToJava(String jsonArg, Type paramType, Annotation[] parameterAnnotations) throws JsonUnmarshallingException {
		Class<? extends org.ocelotds.marshalling.JsonUnmarshaller> unmarshaller = getUnMarshallerAnnotation(parameterAnnotations);
		if (null != unmarshaller) {
			try {
				org.ocelotds.marshalling.JsonUnmarshaller newInstance = unmarshaller.newInstance();
				return newInstance.toJava(jsonArg);
			} catch (JsonParsingException | InstantiationException | IllegalAccessException ex) {
				throw new JsonUnmarshallingException(jsonArg);
			}
		} else {
			return convertArgument(jsonArg, paramType);
		}
	}

	/**
	 * If argument is annotated with JsonUnmarshaller annotation, get the JsonUnmarshaller class
	 *
	 * @param annotations
	 * @param paramType
	 * @return
	 */
	Class<? extends org.ocelotds.marshalling.JsonUnmarshaller> getUnMarshallerAnnotation(Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if (JsonUnmarshaller.class.isInstance(annotation)) {
				JsonUnmarshaller unmarshallerAnnotation = (JsonUnmarshaller) annotation;
				return unmarshallerAnnotation.value();
			}
		}
		return null;
	}

	/**
	 * try to convert json argument in java type
	 *
	 * @param arg
	 * @param paramType
	 * @return
	 * @throws IllegalArgumentException
	 */
	Object convertArgument(String arg, Type paramType) throws IllegalArgumentException {
		Object result = null;
		if (null == arg || "null".equals(arg)) {
			return result;
		}
		logger.debug("Try to convert {} : param = {} : {}", new Object[]{arg, paramType, paramType.getClass()});
		try {
			ObjectMapper mapper = new ObjectMapper();
			if (ParameterizedType.class.isInstance(paramType)) {
				JavaType javaType = getJavaType(paramType);
				logger.debug("Try to convert '{}' to JavaType : '{}'", arg, paramType);
				result = mapper.readValue(arg, javaType);
				logger.debug("Conversion of '{}' to '{}' : OK", arg, paramType);
			} else if (Class.class.isInstance(paramType)) {
				Class cls = (Class) paramType;
				logger.debug("Try to convert '{}' to Class '{}'", arg, paramType);
				checkStringArgument(cls, arg);
				result = mapper.readValue(arg, cls);
				logger.debug("Conversion of '{}' to '{}' : OK", arg, paramType);
			}
		} catch (IOException ex) {
			logger.debug("Conversion of '{}' to '{}' failed", arg, paramType);
			throw new IllegalArgumentException(paramType.toString());
		}
		return result;
	}
	
	/**
	 * check if class and argument are string
	 * @param cls
	 * @param arg
	 * @throws IOException 
	 */
	void checkStringArgument(Class cls, String arg) throws IOException {
		if (cls.equals(String.class)) {
			if(!arg.startsWith("\"") || !arg.endsWith("\"")) { // on cherche une string
				throw new IOException();
			}
		} else {
			if (arg.startsWith("\"") && arg.endsWith("\"")) { // on a une string
				throw new IOException();
			}
		}
	}

	private JavaType getJavaType(Type type) {
		Class clazz;
		logger.debug("Computing type of {} - {}", type.getClass(), type.toString());
		if (type instanceof ParameterizedType) {
			clazz = (Class) ((ParameterizedType) type).getRawType();
		} else {
			clazz = (Class) type;
		}
		JavaType javaType;
		Type actualType;
		if (Collection.class.isAssignableFrom(clazz)) {
			ParameterizedType pt = (ParameterizedType) type;
			actualType = pt.getActualTypeArguments()[0];
			JavaType t1 = getJavaType(actualType);
			javaType = CollectionType.construct(Collection.class, t1);
		} else if (clazz.isArray()) {
			Class t = clazz.getComponentType();
			JavaType t1 = getJavaType(t);
			javaType = ArrayType.construct(t1, null, null);
		} else if (Map.class.isAssignableFrom(clazz)) {
			ParameterizedType pt = (ParameterizedType) type;
			actualType = pt.getActualTypeArguments()[0];
			JavaType t1 = getJavaType(actualType);
			actualType = pt.getActualTypeArguments()[1];
			JavaType t2 = getJavaType(actualType);
			javaType = MapType.construct(Map.class, t1, t2);
		} else {
			javaType = SimpleType.construct(clazz);
		}
		return javaType;
	}
	
}
