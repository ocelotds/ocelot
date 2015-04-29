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
import java.lang.reflect.Parameter;
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
				logger.debug("Traitement de la methode {}", method.getName());
				try {
					Parameter[] params = method.getParameters();
					logger.debug("On a trouvé une methode avec le bon nombre d'arguments, on essaye de les unmarshaller.");
					int idx = 0;
					for (Parameter param : params) {
						String arg = cleanArg(parameters.get(idx));

						logger.debug("Récupération de l'argument ({}) {} : {}.", new Object[]{idx, param.getName(), arg});
						arguments[idx++] = convertArgument(arg, param);
					}
					logger.debug("Méthode {}.{} avec tous les arguments du même type trouvé.", dataService.getClass(), message.getOperation());
					return method;
				} catch (IllegalArgumentException iae) {
					logger.debug("Méthode {}.{} non retenue car {} ne colle pas, mauvais type.", new Object[]{dataService.getClass(), message.getOperation(), iae.getMessage()});
				}
			}
		}
		throw new MethodNotFoundException(dataService.getClass() + "." + message.getOperation());
	}

	private Object convertArgument(String arg, Parameter param) throws IllegalArgumentException {
		Object result = null;
		logger.debug("Tentative de conversion de {}", arg);
		try {
			ObjectMapper mapper = new ObjectMapper();
			if (Collection.class.isAssignableFrom(param.getType()) || Map.class.isAssignableFrom(param.getType())) {
				ParameterizedType pt = (ParameterizedType) param.getParameterizedType();
				JavaType javaType = getJavaType(pt);
				logger.debug("Tentative de conversion de {} vers JavaType : {}", arg, javaType);
				result = mapper.readValue(arg, javaType);
				logger.debug("Conversion de {} vers {} : OK", arg, param.getType());
			} else if (param.getType().isArray()) {
				JavaType javaType = getJavaType(param.getType());
				logger.debug("Tentative de conversion de {} vers {}", arg, param.getType());
				result = mapper.readValue(arg, javaType);
				logger.debug("Conversion de {} vers {} : OK", arg, param.getType());
			} else {
				logger.debug("Tentative de conversion de {} vers {}", arg, param.getType());
				if (param.getType().equals(String.class) && (!arg.startsWith("\"") || !arg.endsWith("\""))) { // on cherche une string
					throw new IOException();
				}
				if (!param.getType().equals(String.class) && arg.startsWith("\"") && arg.endsWith("\"")) { // on a une string
					throw new IOException();
				}
				result = mapper.readValue(arg, param.getType());
				logger.debug("Conversion de conversion de {} vers {} : OK", arg, param.getType());
			}
		} catch (IOException ex) {
			logger.debug("Echec de tentative de conversion de {} vers {}", arg, param.getType());
			throw new IllegalArgumentException(param.getName());
		}
		return result;
	}

	private JavaType getJavaType(Type type) {
		Class clazz;
		logger.debug("Détermination du type {} - {}", type.getClass(), type.getTypeName());
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
