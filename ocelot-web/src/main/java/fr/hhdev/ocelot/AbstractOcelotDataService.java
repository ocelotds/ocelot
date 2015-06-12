/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.SimpleType;
import fr.hhdev.ocelot.messaging.MessageFromClient;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.el.MethodNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class of OcelotDataService
 *
 * @author hhfrancois
 */
public abstract class AbstractOcelotDataService {

	private static final Logger logger = LoggerFactory.getLogger(AbstractOcelotDataService.class);

	/**
	 * Retourne la methode adéquate et peuple la liste des arguments en les déserialisant du message
	 *
	 * @param dataService
	 * @param message
	 * @param arguments
	 * @return
	 */
	protected Method getMethodFromDataService(final Object dataService, final MessageFromClient message, Object[] arguments) throws MethodNotFoundException {
		List<String> parameters = message.getParameters();
		for (Method method : dataService.getClass().getMethods()) {
			if (method.getName().equals(message.getOperation()) && method.getParameterCount() == parameters.size()) {
				logger.trace("Process method {}", method.getName());
				try {
					Type[] params = method.getGenericParameterTypes();
					int idx = 0;
					for (Type param : params) {
						String arg = cleanArg(parameters.get(idx));
						logger.trace("Get argument ({}) {} : {}.", new Object[]{idx, param.getTypeName(), arg});
						arguments[idx++] = convertArgument(arg, param);
					}
					logger.trace("Method {}.{} with good signature found.", dataService.getClass(), message.getOperation());
					return method;
				} catch (IllegalArgumentException iae) {
					logger.trace("Method {}.{} not found. Arguments didn't match. {}.", new Object[]{dataService.getClass(), message.getOperation(), iae.getMessage()});
				}
			}
		}
		throw new MethodNotFoundException(dataService.getClass() + "." + message.getOperation());
	}

	private Object convertArgument(String arg, Type param) throws IllegalArgumentException {
		Object result = null;
		logger.trace("Try to convert {} : param = {} : {}", new Object[]{arg, param, param.getClass()});
		try {
			ObjectMapper mapper = new ObjectMapper();
			if(ParameterizedType.class.isInstance(param)) {
				JavaType javaType = getJavaType(param);
				logger.trace("Try to convert '{}' to JavaType : '{}'", arg, param);
				result = mapper.readValue(arg, javaType);
				logger.trace("Conversion of '{}' to '{}' : OK", arg, param);
			} else if(Class.class.isInstance(param)) {
				Class cls = (Class) param;
				logger.trace("Try to convert '{}' to Class '{}'", arg, param);
				if (cls.equals(String.class) && (!arg.startsWith("\"") || !arg.endsWith("\""))) { // on cherche une string
					throw new IOException();
				}
				if (!cls.equals(String.class) && arg.startsWith("\"") && arg.endsWith("\"")) { // on a une string
					throw new IOException();
				}
				result = mapper.readValue(arg, cls);
				logger.trace("Conversion of '{}' to '{}' : OK", arg, param);
			}
		} catch (IOException ex) {
			logger.trace("Conversion of '{}' to '{}' failed", arg, param);
			throw new IllegalArgumentException(param.getTypeName());
		}
		return result;
	}

	private JavaType getJavaType(Type type) {
		Class clazz;
		logger.trace("Computing type of {} - {}", type.getClass(), type.getTypeName());
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

	/**
	 * Methode permettant de nettoyer les arguments des attributs ajoutés par les framework web, 
	 * par exemple angularjs rajoute des variables commencant par $$
	 * à remplacer : ,"$$hashKey":"object:\d"
	 *
	 * @param arg
	 * @return
	 */
	private String cleanArg(String arg) {
		String angularvar = "(,\"\\$\\$\\w+\":\".*\")";
		return arg.replaceAll(angularvar, "");
	}

}
