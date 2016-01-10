/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.websocket.Session;
import org.ocelotds.Constants;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.core.CacheManager;
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
 */
public class MessageToClientManager implements MessageToClientService {
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

	IDataServiceResolver getResolver(String type) {
		return resolvers.select(new DataServiceResolverIdLitteral(type)).get();
	}

	/**
	 * Get Dataservice, store dataservice in session if session scope.<br>
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
			try {
				return _getDataService(client, cls);
			} catch (Exception e) {
				throw new DataServiceException(dataServiceClassName, e);
			}
		} else {
			throw new DataServiceException(dataServiceClassName);
		}
	}

	/**
	 * Get Dataservice, store dataservice in session if session scope.<br>
	 *
	 * @param client
	 * @param cls
	 * @return
	 * @throws DataServiceException
	 */
	Object _getDataService(Session client, Class cls) throws Exception {
		String dataServiceClassName = cls.getName();
		DataService dataServiceAnno = (DataService) cls.getAnnotation(DataService.class);
		IDataServiceResolver resolver = getResolver(dataServiceAnno.resolver());
		Scope scope = resolver.getScope(cls);
		Object dataService = null;
		Map sessionBeans = (Map) client.getUserProperties().get(Constants.SESSION_BEANS);
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
	 * create a MessageToClient from MessageFromClient for client
	 * @param message
	 * @param client
	 * @return 
	 */
	@Override
	public MessageToClient createMessageToClient(MessageFromClient message, Session client) {
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(message.getId());
		try {
			Class cls = Class.forName(message.getDataService());
			Object dataService = this.getDataService(client, cls);
			logger.debug("Process message {}", message);
			List<Object> arguments = getArrayList();
			Method method = methodServices.getMethodFromDataService(cls, message, arguments);
			messageToClient.setResult(method.invoke(dataService, arguments.toArray()));
			if (method.isAnnotationPresent(JsonMarshaller.class)) {
				messageToClient.setJson(argumentServices.getJsonResultFromSpecificMarshaller(method.getAnnotation(JsonMarshaller.class), messageToClient.getResponse()));
			}
			try {
				Method nonProxiedMethod = methodServices.getNonProxiedMethod(cls, method.getName(), method.getParameterTypes());
				messageToClient.setDeadline(cacheManager.processCacheAnnotations(nonProxiedMethod, message.getParameterNames(),  message.getParameters()));
			} catch (NoSuchMethodException ex) {
				logger.error("Fail to process extra annotations (JsCacheResult, JsCacheRemove) for method : " + method.getName(), ex);
			}
			logger.debug("Method {} proceed messageToClient : {}.", method.getName(), messageToClient);
		} catch (InvocationTargetException ex) {
			messageToClient.setFault(faultServices.buildFault(ex.getCause()));
		} catch (Throwable ex) {
			messageToClient.setFault(faultServices.buildFault(ex));
		}
		return messageToClient;
	}
	
	List<Object> getArrayList() {
		return new ArrayList<>();
	}
}
