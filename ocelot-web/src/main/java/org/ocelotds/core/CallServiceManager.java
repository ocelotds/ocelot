/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.core;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.SimpleType;
import org.ocelotds.configuration.OcelotConfiguration;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.JsCacheResult;
import org.ocelotds.messaging.Fault;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.resolvers.DataServiceResolverIdLitteral;
import org.ocelotds.spi.DataServiceException;
import org.ocelotds.spi.IDataServiceResolver;
import org.ocelotds.spi.Scope;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.websocket.Session;
import org.ocelotds.logger.OcelotLogger;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.marshalling.annotations.JsonUnmarshaller;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;
import org.ocelotds.security.CallService;
import org.slf4j.Logger;

/**
 * Abstract class of OcelotDataService
 *
 * @author hhfrancois
 */
public class CallServiceManager {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	private Cleaner cleaner;

	@Inject
	@Any
	private Instance<IDataServiceResolver> resolvers;

	@Inject
	private OcelotConfiguration configuration;

	@Inject
	private CacheManager cacheManager;

	IDataServiceResolver getResolver(String type) {
		return resolvers.select(new DataServiceResolverIdLitteral(type)).get();
	}

	/**
	 * Get pertinent method and fill the argument list from message arguments
	 *
	 * @param dsClass
	 * @param message
	 * @param arguments
	 * @return
	 * @throws java.lang.NoSuchMethodException
	 */
	Method getMethodFromDataService(final Class dsClass, final MessageFromClient message, Object[] arguments) throws NoSuchMethodException {
		logger.debug("Try to find method {} on class {}", message.getOperation(), dsClass);
		List<String> parameters = message.getParameters();
		for (Method method : dsClass.getMethods()) {
			if (method.getName().equals(message.getOperation()) && method.getParameterTypes().length == parameters.size()) {
				logger.debug("Process method {}", method.getName());
				try {
					Type[] paramTypes = method.getGenericParameterTypes();
					Annotation[][] parametersAnnotations = method.getParameterAnnotations();
					int idx = 0;
					for (Type paramType : paramTypes) {
						String jsonArg = cleaner.cleanArg(parameters.get(idx));
						arguments[idx] = convertJsonToJava(idx, jsonArg, paramType, parametersAnnotations[idx]);
						idx++;
					}
					logger.debug("Method {}.{} with good signature found.", dsClass, message.getOperation());
					return method;
				} catch (JsonUnmarshallingException | IllegalArgumentException iae) {
					logger.debug("Method {}.{} not found. Arguments didn't match. {}.", new Object[]{dsClass, message.getOperation(), iae.getMessage()});
				}
			}
		}
		throw new NoSuchMethodException(dsClass + "." + message.getOperation());
	}

	/**
	 * Get pertinent method and fill the argument list from message arguments This method inject an additional argument, the client session
	 *
	 * @param session
	 * @param dsClass
	 * @param message
	 * @param arguments
	 * @return
	 * @throws java.lang.NoSuchMethodException
	 */
	Method getMethodFromDataServiceWithSessionInjection(final Session session, final Class dsClass, final MessageFromClient message, Object[] arguments) throws NoSuchMethodException {
		logger.debug("Try to find method with session {} on class {}", message.getOperation(), dsClass);
		List<String> parameters = message.getParameters();
		for (Method method : dsClass.getMethods()) {
			int nbParamater = parameters.size() + 1;
			if (method.getName().equals(message.getOperation()) && method.getParameterTypes().length == nbParamater) {
				logger.debug("Process method {}", method.getName());
				try {
					Type[] paramTypes = method.getGenericParameterTypes();
					Annotation[][] parametersAnnotations = method.getParameterAnnotations();
					int idx = 0, pidx = 0;
					for (Type paramType : paramTypes) {
						if (idx < nbParamater) {
							if (Session.class.equals(paramType)) {
								arguments[idx++] = session;
							} else {
								String jsonArg = cleaner.cleanArg(parameters.get(pidx++));
								arguments[idx] = convertJsonToJava(idx, jsonArg, paramType, parametersAnnotations[idx]);
								idx++;
							}
						}
					}
					logger.debug("Method {}.{} with good signature with injected session found.", dsClass, message.getOperation());
					return method;
				} catch (JsonUnmarshallingException | IllegalArgumentException iae) {
					logger.debug("Method {}.{} with injected not found. Arguments didn't match. {}.", new Object[]{dsClass, message.getOperation(), iae.getMessage()});
				}
			}
		}
		throw new NoSuchMethodException(dsClass + "." + message.getOperation());
	}

	/**
	 * Convert json to Java
	 *
	 * @param idx
	 * @param jsonArg
	 * @param paramType
	 * @param parameterAnnotations
	 * @return
	 * @throws JsonUnmarshallingException
	 */
	Object convertJsonToJava(int idx, String jsonArg, Type paramType, Annotation[] parameterAnnotations) throws JsonUnmarshallingException {
		Class<? extends org.ocelotds.marshalling.JsonUnmarshaller> unmarshaller = getUnMarshallerAnnotation(parameterAnnotations);
		if (null != unmarshaller) {
			try {
				org.ocelotds.marshalling.JsonUnmarshaller newInstance = unmarshaller.newInstance();
				return newInstance.toJava(jsonArg);
			} catch (InstantiationException | IllegalAccessException ex) {
				throw new JsonUnmarshallingException(jsonArg);
			}
		} else {
			logger.debug("Get argument ({}) {} : {}.", new Object[]{idx, paramType.toString(), jsonArg});
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
		if(null==arg || "null".equals(arg)) {
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
				if (cls.equals(String.class) && (!arg.startsWith("\"") || !arg.endsWith("\""))) { // on cherche une string
					throw new IOException();
				}
				if (!cls.equals(String.class) && arg.startsWith("\"") && arg.endsWith("\"")) { // on a une string
					throw new IOException();
				}
				result = mapper.readValue(arg, cls);
				logger.debug("Conversion of '{}' to '{}' : OK", arg, paramType);
			}
		} catch (IOException ex) {
			logger.debug("Conversion of '{}' to '{}' failed", arg, paramType);
			throw new IllegalArgumentException(paramType.toString());
		}
		return result;
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

	/**
	 * Get Dataservice, store dataservice in session if session scope.<br>
	 * TODO I would like to do that from an interceptor, but how give the session to it ?
	 *
	 * @param client
	 * @param cls
	 * @return
	 * @throws DataServiceException
	 */
	Object getDataService(Session client, Class cls) throws DataServiceException {
		String dataServiceClassName = cls.getName();
		logger.debug("Looking for dataservice : {}", dataServiceClassName);
		if (cls.isAnnotationPresent(DataService.class)) {
			DataService dataServiceAnno = (DataService) cls.getAnnotation(DataService.class);
			IDataServiceResolver resolver = getResolver(dataServiceAnno.resolver());
			Scope scope = resolver.getScope(cls);
			Object dataService = null;
			if (scope.equals(Scope.SESSION)) {
				dataService = client.getUserProperties().get(dataServiceClassName);
			}
			if (dataService == null) {
				dataService = resolver.resolveDataService(cls);
				if (scope.equals(Scope.SESSION)) {
					client.getUserProperties().put(dataServiceClassName, dataService);
				}
			}
			return dataService;
		} else {
			throw new DataServiceException(dataServiceClassName);
		}
	}

	/**
	 * Build and send response messages after call request
	 *
	 * @param message
	 * @param client
	 */
	@CallService
	public void sendMessageToClient(MessageFromClient message, Session client) {
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(message.getId());
		try {
			Class cls = Class.forName(message.getDataService());
			Object dataService = this.getDataService(client, cls);
			logger.debug("Process message {}", message);
			int nbParam = message.getParameters().size();
			Object[] arguments = new Object[nbParam];
			Method method = this.getMethodFromDataService(cls, message, arguments);
			if (!method.isAnnotationPresent(MethodWithSessionInjection.class)) {
				messageToClient.setResult(method.invoke(dataService, arguments));
			} else {
				arguments = new Object[nbParam + 1];
				method = this.getMethodFromDataServiceWithSessionInjection(client, cls, message, arguments);
				messageToClient.setResult(method.invoke(dataService, arguments));
			}
			if (method.isAnnotationPresent(JsonMarshaller.class)) {
				JsonMarshaller jm = method.getAnnotation(JsonMarshaller.class);
				Class<? extends org.ocelotds.marshalling.JsonMarshaller> marshallerCls = jm.value();
				org.ocelotds.marshalling.JsonMarshaller marshaller = marshallerCls.newInstance();
				String json = marshaller.toJson(messageToClient.getResponse());
				messageToClient.setJson(json);
			}
			try {
				Method nonProxiedMethod = this.getNonProxiedMethod(cls, method.getName(), method.getParameterTypes());
				if (cacheManager.isJsCached(nonProxiedMethod)) {
					JsCacheResult jcr = nonProxiedMethod.getAnnotation(JsCacheResult.class);
					messageToClient.setDeadline(cacheManager.getJsCacheResultDeadline(jcr));
				}
				cacheManager.processCleanCacheAnnotations(nonProxiedMethod, message.getParameterNames(), message.getParameters());
				if (logger.isDebugEnabled()) {
					logger.debug("Method {} proceed messageToClient : {}.", method.getName(), messageToClient.toJson());
				}
			} catch (NoSuchMethodException ex) {
				logger.error("Fail to process extra annotations (JsCacheResult, JsCacheRemove) for method : " + method.getName(), ex);
			}
		} catch (InvocationTargetException ex) {
			messageToClient.setFault(buildFault(ex.getCause()));
		} catch (Throwable ex) {
			messageToClient.setFault(buildFault(ex));
		}
		client.getAsyncRemote().sendObject(messageToClient);
	}

	Fault buildFault(Throwable ex) {
		Fault fault;
		int stacktracelength = configuration.getStacktracelength();
		if (stacktracelength == 0 || logger.isDebugEnabled()) {
			logger.error("Invocation failed", ex);
		}
		fault = new Fault(ex, stacktracelength);
		return fault;
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
	Method getNonProxiedMethod(Class cls, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
		return cls.getMethod(methodName, parameterTypes);
	}
}
