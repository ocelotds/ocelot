/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot.core;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.SimpleType;
import fr.hhdev.ocelot.configuration.OcelotConfiguration;
import fr.hhdev.ocelot.annotations.DataService;
import fr.hhdev.ocelot.annotations.JsCacheResult;
import fr.hhdev.ocelot.messaging.Fault;
import fr.hhdev.ocelot.messaging.MessageFromClient;
import fr.hhdev.ocelot.messaging.MessageToClient;
import fr.hhdev.ocelot.messaging.MessageType;
import fr.hhdev.ocelot.resolvers.DataServiceResolverIdLitteral;
import fr.hhdev.ocelot.spi.DataServiceException;
import fr.hhdev.ocelot.spi.IDataServiceResolver;
import fr.hhdev.ocelot.spi.Scope;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.el.MethodNotFoundException;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class of OcelotDataService
 *
 * @author hhfrancois
 */
public class CallServiceManager {

	private static final Logger logger = LoggerFactory.getLogger(CallServiceManager.class);

	@Inject
	private Cleaner cleaner;

	@Inject
	@Any
	private Instance<IDataServiceResolver> resolvers;

	@Inject
	private OcelotConfiguration configuration;

	@Inject
	private CacheManager cacheManager;

	protected IDataServiceResolver getResolver(String type) {
		return resolvers.select(new DataServiceResolverIdLitteral(type)).get();
	}

	/**
	 * Get pertinent method and fill the argument list from message arguments
	 *
	 * @param dsClass
	 * @param message
	 * @param arguments
	 * @return
	 */
	protected Method getMethodFromDataService(final Class dsClass, final MessageFromClient message, Object[] arguments) throws MethodNotFoundException {
		logger.debug("Try to find method {} on class {}", message.getOperation(), dsClass);
		List<String> parameters = message.getParameters();
		for (Method method : dsClass.getMethods()) {
			if (method.getName().equals(message.getOperation()) && method.getParameterTypes().length == parameters.size()) {
				logger.debug("Process method {}", method.getName());
				try {
					Type[] params = method.getGenericParameterTypes();
					int idx = 0;
					for (Type param : params) {
						String arg = cleaner.cleanArg(parameters.get(idx));
						logger.debug("Get argument ({}) {} : {}.", new Object[]{idx, param.toString(), arg});
						arguments[idx++] = convertArgument(arg, param);
					}
					logger.debug("Method {}.{} with good signature found.", dsClass, message.getOperation());
					return method;
				} catch (IllegalArgumentException iae) {
					logger.debug("Method {}.{} not found. Arguments didn't match. {}.", new Object[]{dsClass, message.getOperation(), iae.getMessage()});
				}
			}
		}
		throw new MethodNotFoundException(dsClass + "." + message.getOperation());
	}

	/**
	 * Get pertinent method and fill the argument list from message arguments This method inject an additional argument, the client session
	 *
	 * @param session
	 * @param dsClass
	 * @param message
	 * @param arguments
	 * @return
	 */
	protected Method getMethodFromDataServiceWithSessionInjection(final Session session, final Class dsClass, final MessageFromClient message, Object[] arguments) throws MethodNotFoundException {
		logger.debug("Try to find method with session {} on class {}", message.getOperation(), dsClass);
		List<String> parameters = message.getParameters();
		for (Method method : dsClass.getMethods()) {
			int nbParamater = parameters.size() + 1;
			if (method.getName().equals(message.getOperation()) && method.getParameterTypes().length == nbParamater) {
				logger.debug("Process method {}", method.getName());
				try {
					Type[] params = method.getGenericParameterTypes();
					int idx = 0, pidx = 0;
					for (Type param : params) {
						if (idx < nbParamater) {
							if(Session.class.equals(param)) {
								arguments[idx++] = session;
							} else {
								String jsonArg = parameters.get(pidx++);
								String arg = cleaner.cleanArg(jsonArg);
								logger.debug("Get argument ({}) {} : {}.", new Object[]{idx, param.toString(), arg});
								arguments[idx++] = convertArgument(arg, param);
							}
						}
					}
					logger.debug("Method {}.{} with good signature found.", dsClass, message.getOperation());
					return method;
				} catch (IllegalArgumentException iae) {
					logger.debug("Method {}.{} not found. Arguments didn't match. {}.", new Object[]{dsClass, message.getOperation(), iae.getMessage()});
				}
			}
		}
		throw new MethodNotFoundException(dsClass + "." + message.getOperation());
	}

	private Object convertArgument(String arg, Type param) throws IllegalArgumentException {
		Object result = null;
		logger.debug("Try to convert {} : param = {} : {}", new Object[]{arg, param, param.getClass()});
		try {
			ObjectMapper mapper = new ObjectMapper();
			if (ParameterizedType.class.isInstance(param)) {
				JavaType javaType = getJavaType(param);
				logger.debug("Try to convert '{}' to JavaType : '{}'", arg, param);
				result = mapper.readValue(arg, javaType);
				logger.debug("Conversion of '{}' to '{}' : OK", arg, param);
			} else if (Class.class.isInstance(param)) {
				Class cls = (Class) param;
				logger.debug("Try to convert '{}' to Class '{}'", arg, param);
				if (cls.equals(String.class) && (!arg.startsWith("\"") || !arg.endsWith("\""))) { // on cherche une string
					throw new IOException();
				}
				if (!cls.equals(String.class) && arg.startsWith("\"") && arg.endsWith("\"")) { // on a une string
					throw new IOException();
				}
				result = mapper.readValue(arg, cls);
				logger.debug("Conversion of '{}' to '{}' : OK", arg, param);
			}
		} catch (IOException ex) {
			logger.debug("Conversion of '{}' to '{}' failed", arg, param);
			throw new IllegalArgumentException(param.toString());
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
	 * Get Dataservice, maybe in session if session scope and stored J'aimerais bien faire cela avec un interceptor/decorator, mais comment passer la session Ã  celui ci ?
	 *
	 * @param client
	 * @param cls
	 * @return
	 * @throws DataServiceException
	 */
	protected Object getDataService(Session client, Class cls) throws DataServiceException {
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
	 * @param client
	 * @param message
	 */
	public void sendMessageToClients(Session client, MessageFromClient message) {
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(message.getId());
		try {
			Class cls = Class.forName(message.getDataService());
			Object dataService = getDataService(client, cls);
			logger.debug("Process message {}", message);
			logger.debug("Invocation of : {}", message.getOperation());
			int nbParam = message.getParameters().size();
			Object[] arguments = new Object[nbParam];
			Method method = getMethodFromDataService(cls, message, arguments);
			if (!method.isAnnotationPresent(MethodWithSessionInjection.class)) {
				messageToClient.setResult(method.invoke(dataService, arguments));
			} else {
				arguments = new Object[nbParam + 1];
				Method methodWithInjection = getMethodFromDataServiceWithSessionInjection(client, cls, message, arguments);
				messageToClient.setResult(methodWithInjection.invoke(dataService, arguments));
			}
			try {
				Method nonProxiedMethod = getNonProxiedMethod(cls, method.getName(), method.getParameterTypes());
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
		} catch (MethodNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException | DataServiceException ex) {
			int stacktracelength = configuration.getStacktracelength();
			Throwable cause = ex;
			if (InvocationTargetException.class.isInstance(ex)) {
				cause = ex.getCause();
			}
			if (stacktracelength == 0) {
				logger.error("Invocation failed", ex);
			}
			messageToClient.setFault(new Fault(cause, stacktracelength));
		}
		client.getAsyncRemote().sendObject(messageToClient);
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
	private Method getNonProxiedMethod(Class cls, String methodName, Class<?>[] parameterTypes) throws NoSuchMethodException {
		try {
			return cls.getMethod(methodName, parameterTypes);
		} catch (SecurityException ex) {
		}
		throw new NoSuchMethodException(methodName);
	}

	private static class MethodWithSessionInjectionExeption extends Exception {
		private static final long serialVersionUID = 5103504405002719013L;
	}
}
