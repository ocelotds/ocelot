/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.mtc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.core.CacheManager;
import org.ocelotds.core.services.ArgumentServices;
import org.ocelotds.core.services.ConstraintServices;
import org.ocelotds.core.services.FaultServices;
import org.ocelotds.core.services.MethodServices;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.resolvers.DataServiceResolverIdLitteral;
import org.ocelotds.spi.DataServiceException;
import org.ocelotds.spi.IDataServiceResolver;
import org.ocelotds.spi.Scope;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 * @param <T> Session or HttpSession
 */
public abstract class MessageToClientManager<T> {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	@Any
	private Instance<IDataServiceResolver> resolvers;

	@Inject
	private CacheManager cacheManager;

	@Inject
	private MethodServices methodServices;

	@Inject
	private ArgumentServices argumentServices;

	@Inject
	private FaultServices faultServices;

	@Inject
	private ConstraintServices constraintServices;

	public abstract Map<String, Object> getSessionBeans(T session);

	IDataServiceResolver getResolver(String type) {
		return resolvers.select(new DataServiceResolverIdLitteral(type)).get();
	}

	/**
	 * Get Dataservice, store dataservice in session if session scope.<br>
	 *
	 * @param session
	 * @param cls
	 * @return
	 * @throws DataServiceException
	 */
	Object getDataService(T session, Class cls) throws DataServiceException {
		String dataServiceClassName = cls.getName();
		logger.debug("Looking for dataservice : {}", dataServiceClassName);
		if (cls.isAnnotationPresent(DataService.class)) {
			return _getDataService(session, cls);
		} else {
			throw new DataServiceException(dataServiceClassName +" is not annotated with @"+DataService.class.getSimpleName());
		}
	}

	/**
	 * Get Dataservice, store dataservice in session if session scope.<br>
	 *
	 * @param session
	 * @param cls
	 * @return
	 * @throws DataServiceException
	 */
	Object _getDataService(T session, Class cls) throws DataServiceException {
		String dataServiceClassName = cls.getName();
		DataService dataServiceAnno = (DataService) cls.getAnnotation(DataService.class);
		IDataServiceResolver resolver = getResolver(dataServiceAnno.resolver());
		Scope scope = resolver.getScope(cls);
		Object dataService = null;
		Map<String, Object> sessionBeans = getSessionBeans(session);
		logger.debug("{} : scope : {}", dataServiceClassName, scope);
		if (scope.equals(Scope.SESSION)) {
			dataService = sessionBeans.get(dataServiceClassName);
			logger.debug("{} : scope : session is in session : {}", dataServiceClassName, (dataService != null));
		}
		if (dataService == null) {
			dataService = resolver.resolveDataService(cls);
			if (scope.equals(Scope.SESSION)) {
				logger.debug("Store {} scope session in session", dataServiceClassName);
				sessionBeans.put(dataServiceClassName, dataService);
			}
		}
		return dataService;
	}

	/**
	 * Create a MessageToClient from MessageFromClient for session
	 *
	 * @param message
	 * @param session
	 * @return
	 */
	public MessageToClient createMessageToClient(MessageFromClient message, T session) {
		return _createMessageToClient(message, session);
	}

	/**
	 * Create a MessageToClient from MessageFromClient for session Only for available test use case
	 *
	 * @param message
	 * @param session
	 * @return
	 */
	MessageToClient _createMessageToClient(MessageFromClient message, T session) {
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(message.getId());
		try {
			Class cls = Class.forName(message.getDataService());
			Object dataService = this.getDataService(session, cls);
			logger.debug("Process message {}", message);
			List<Object> arguments = getArrayList();
			Method method = methodServices.getMethodFromDataService(cls, message, arguments);
			messageToClient.setResult(method.invoke(dataService, arguments.toArray()));
			if (method.isAnnotationPresent(JsonMarshaller.class)) {
				messageToClient.setJson(argumentServices.getJsonResultFromSpecificMarshaller(method.getAnnotation(JsonMarshaller.class), messageToClient.getResponse()));
			}
			try {
				Method nonProxiedMethod = methodServices.getNonProxiedMethod(cls, method.getName(), method.getParameterTypes());
				messageToClient.setDeadline(cacheManager.processCacheAnnotations(nonProxiedMethod, message.getParameterNames(), message.getParameters()));
			} catch (NoSuchMethodException ex) {
				logger.error("Fail to process extra annotations (JsCacheResult, JsCacheRemove) for method : " + method.getName(), ex);
			}
			logger.debug("Method {} proceed messageToClient : {}.", method.getName(), messageToClient);
		} catch (InvocationTargetException ex) {
			Throwable cause = ex.getCause();
			if (ConstraintViolationException.class.isInstance(cause)) {
				messageToClient.setConstraints(constraintServices.extractViolations((ConstraintViolationException) cause, message.getParameterNames()));
			} else {
				messageToClient.setFault(faultServices.buildFault(cause));
			}
		} catch (Throwable ex) {
			messageToClient.setFault(faultServices.buildFault(ex));
		}
		return messageToClient;
	}

	List<Object> getArrayList() {
		return new ArrayList<>();
	}
}
