/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.services;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.marshalling.exceptions.JsonMarshallerException;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;
import org.ocelotds.messaging.MessageFromClient;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
public class MethodServices {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	private ArgumentConvertor argumentConvertor;

	@Inject
	private MethodComparator comparator;

	/**
	 * Get pertinent method and fill the argument list from message arguments
	 *
	 * @param dsClass
	 * @param message
	 * @param arguments
	 * @return
	 * @throws java.lang.NoSuchMethodException
	 */
	public Method getMethodFromDataService(final Class dsClass, final MessageFromClient message, List<Object> arguments) throws NoSuchMethodException {
		logger.debug("Try to find method {} on class {}", message.getOperation(), dsClass);
		List<String> parameters = message.getParameters();
		int nbparam = parameters.size() - getNumberOfNullEnderParameter(parameters); // determine how many parameter is null at the end
		List<Method> candidates = getSortedCandidateMethods(message.getOperation(), dsClass.getMethods()); // take only method with the good name, and orderedby number of arguments
		if (!candidates.isEmpty()) {
			while (nbparam <= parameters.size()) {
				for (Method method : candidates) {
					if (method.getParameterTypes().length == nbparam) {
						logger.debug("Process method {}", method.getName());
						try {
							checkMethod(method, arguments, parameters, nbparam);
							logger.debug("Method {}.{} with good signature found.", dsClass, message.getOperation());
							return method;
						} catch (JsonMarshallerException | JsonUnmarshallingException | IllegalArgumentException iae) {
							logger.debug("Method {}.{} not found. Some arguments didn't match. {}.", new Object[]{dsClass, message.getOperation(), iae.getMessage()});
						}
						arguments.clear();
					}
				}
				nbparam++;
			}
		}
		throw new NoSuchMethodException(dsClass.getName() + "." + message.getOperation());
	}

	/**
	 * Get the method on origin class without proxies
	 *
	 * @param cls
	 * @param methodName
	 * @param parameterTypes
	 * @throws NoSuchMethodException
	 * @return
	 */
	public Method getNonProxiedMethod(Class cls, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
		return cls.getMethod(methodName, parameterTypes);
	}

	/**
	 * Return the number of null parameter at the end of list
	 *
	 * @param parameters
	 * @return
	 */
	int getNumberOfNullEnderParameter(List<String> parameters) {
		int nbnull = 0;
		for (int i = parameters.size() - 1; i >= 0; i--) {
			String parameter = parameters.get(i);
			if (parameter.equals("null")) {
				nbnull++;
			} else {
				break;
			}
		}
		return nbnull;
	}

	/**
	 * Check if for nbparam in parameters the method is correct. If yes, store parameters String converted to Java in arguments list
	 *
	 * @param method
	 * @param arguments
	 * @param parameters
	 * @param nbparam
	 * @throws IllegalArgumentException
	 * @throws JsonUnmarshallingException
	 */
	void checkMethod(Method method, List<Object> arguments, List<String> parameters, int nbparam) throws IllegalArgumentException, JsonUnmarshallingException, JsonMarshallerException {
		Type[] paramTypes = method.getGenericParameterTypes();
		Annotation[][] parametersAnnotations = method.getParameterAnnotations();
		int idx = 0;
		for (Type paramType : paramTypes) {
			logger.debug("Try to convert argument ({}) {} : {}.", new Object[]{idx, paramType, parameters.get(idx)});
			arguments.add(argumentConvertor.convertJsonToJava(parameters.get(idx), paramType, parametersAnnotations[idx]));
			idx++;
			if (idx > nbparam) {
				throw new IllegalArgumentException();
			}
		}
	}

	/**
	 * return candidates method (same name) sort by number of arguments
	 *
	 * @param methodName
	 * @param methods
	 * @return
	 */
	List<Method> getSortedCandidateMethods(String methodName, Method[] methods) {
		List<Method> candidates = new ArrayList<>();
		for (Method method : methods) {
			if (method.getName().equals(methodName)) {
				candidates.add(method);
			}
		}
		Collections.sort(candidates, comparator);
		return candidates;
	}
}
