/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.hhdev.ocelot.annotations.DataService;
import fr.hhdev.ocelot.annotations.JsCacheRemove;
import fr.hhdev.ocelot.annotations.JsCacheRemoveAll;
import fr.hhdev.ocelot.annotations.JsCacheRemoves;
import fr.hhdev.ocelot.annotations.JsCacheResult;
import fr.hhdev.ocelot.encoders.CommandDecoder;
import fr.hhdev.ocelot.spi.Scope;
import fr.hhdev.ocelot.encoders.MessageToClientEncoder;
import fr.hhdev.ocelot.messaging.Command;
import fr.hhdev.ocelot.messaging.Fault;
import fr.hhdev.ocelot.messaging.MessageEvent;
import fr.hhdev.ocelot.messaging.MessageFromClient;
import fr.hhdev.ocelot.messaging.MessageToClient;
import fr.hhdev.ocelot.spi.DataServiceException;
import fr.hhdev.ocelot.spi.IDataServiceResolver;
import fr.hhdev.ocelot.resolvers.DataServiceResolverIdLitteral;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.el.MethodNotFoundException;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket endpoint
 *
 * @author hhfrancois
 */
@ServerEndpoint(value = "/endpoint", encoders = {MessageToClientEncoder.class}, decoders = {CommandDecoder.class}, configurator = OcelotConfigurator.class)
public class OcelotEndpoint extends AbstractOcelotDataService {

	private final static Logger logger = LoggerFactory.getLogger(OcelotEndpoint.class);

	@Inject
	@MessageEvent
	Event<MessageToClient> wsEvent;

	@Inject
	private SessionManager sessionManager;

	@Inject
	@Any
	private Instance<IDataServiceResolver> resolvers;

	protected IDataServiceResolver getResolver(String type) {
		return resolvers.select(new DataServiceResolverIdLitteral(type)).get();
	}

	@OnOpen
	public void handleOpenConnexion(Session session, EndpointConfig config) throws IOException {
		Map<String, Object> headers = config.getUserProperties();
		logger.trace("Open connexion for session '{}'", session.getId());
	}

	@OnError
	public void onError(Session session, Throwable t) {
		logger.error("Unknow error for session " + session.getId(), t);
	}

	/**
	 * Ferme une session
	 *
	 * @param session
	 * @param closeReason
	 */
	@OnClose
	public void handleClosedConnection(Session session, CloseReason closeReason) {
		logger.trace("Close connexion for session '{}' : '{}'", session.getId(), closeReason.getCloseCode());
		if (session.isOpen()) {
			try {
				session.close();
			} catch (IOException ex) {
			}
			sessionManager.removeSession(session);
		}
	}

	/**
	 * Recevoir un message correspond à la demande d'execution d'un service ou à l'abonnement à un topic
	 *
	 * @param client
	 * @param command
	 */
	@OnMessage
	public void receiveCommandMessage(Session client, Command command) {
		if (null != command.getCommand()) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				String topic;
				switch (command.getCommand()) {
					case Constants.Command.Value.SUBSCRIBE:
						topic = mapper.readValue(command.getMessage(), String.class);
						logger.debug("Subscribe client '{}' to topic '{}'", client.getId(), topic);
						sessionManager.registerTopicSession(topic, client);
						break;
					case Constants.Command.Value.UNSUBSCRIBE:
						topic = mapper.readValue(command.getMessage(), String.class);
						logger.debug("Unsubscribe client '{}' to topic '{}'", client.getId(), topic);
						sessionManager.unregisterTopicSession(topic, client);
						break;
					case Constants.Command.Value.CALL:
						MessageFromClient message = MessageFromClient.createFromJson(command.getMessage());
						logger.debug("Receive call message '{}' for session '{}'", message.getId(), client.getId());
						sendMessageToClients(client, message);
						break;
				}
			} catch (IOException ex) {
			}
		} else {
			logger.warn("Receive unsupported message '{}' from client '{}'", command, client.getId());
		}
	}

	/**
	 * Get Dataservice, maybe in session if session scope and stored J'aimerais bien faire cela avec un interceptor/decorator, mais comment passer la session à
	 * celui ci ?
	 *
	 * @param client
	 * @param cls
	 * @return
	 * @throws DataServiceException
	 */
	protected Object getDataService(Session client, Class cls) throws DataServiceException {
		String dataServiceClassName = cls.getName();
		logger.trace("Looking for dataservice : {}", dataServiceClassName);
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
	 * Construction et envoi des messages response suite à un appel de type call
	 *
	 * @param client
	 * @param message
	 */
	private void sendMessageToClients(Session client, MessageFromClient message) {
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(message.getId());
		try {
			Class cls = Class.forName(message.getDataService());
			Object dataService = getDataService(client, cls);
			logger.trace("Process message {}", message);
			try {
				logger.trace("Invocation of : {}", message.getOperation());
				Object[] arguments = new Object[message.getParameters().size()];
				Method method = getMethodFromDataService(dataService, message, arguments);
				logger.trace("Process method {}.", method.getName());
				Object result = method.invoke(dataService, arguments);
				messageToClient.setResult(result);
				try {
					Method nonProxiedMethod = getNonProxiedMethod(cls, method.getName(), method.getParameterTypes());
					messageToClient.setDeadline(getJsCacheResultDeadline(nonProxiedMethod));
					processCleanCacheAnnotations(nonProxiedMethod, message.getParameters());
				} catch (NoSuchMethodException ex) {
					logger.error("Fail to process extra annotations (JsCacheResult, JsCacheRemove) for method : " + method.getName(), ex);
				}
			} catch (MethodNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				Throwable cause = ex;
				if (InvocationTargetException.class.isInstance(ex)) {
					cause = ex.getCause();
				}
				messageToClient.setFault(new Fault(cause));
			}
		} catch (ClassNotFoundException | DataServiceException ex) {
			messageToClient.setFault(new Fault(ex));
		}
		try {
			client.getBasicRemote().sendObject(messageToClient);
		} catch (IOException | EncodeException ex) {
			logger.error("Fail to send : " + messageToClient.toJson(), ex);
		}
	}

	/**
	 * Récupere la date d'expiration du service via l'annotation sur la methode directement sur la classe, car sinon cela peut être un proxy
	 * Eventuellement traite les annotation spécifiant que l'execution de la méthode doit supprimer un cache
	 *
	 * @param cls
	 * @param method
	 * @return
	 */
	private long getJsCacheResultDeadline(Method nonProxiedMethod) {
		boolean cached = nonProxiedMethod.isAnnotationPresent(JsCacheResult.class);
		if (cached) { // Ce service doit être mis en cache sur le client
			logger.debug("The result of the method {} will be cached on client side.", nonProxiedMethod.getName());
			JsCacheResult jcr = nonProxiedMethod.getAnnotation(JsCacheResult.class);
			Calendar deadline = Calendar.getInstance();
			deadline.add(Calendar.YEAR, jcr.year());
			deadline.add(Calendar.MONTH, jcr.month());
			deadline.add(Calendar.DATE, jcr.day());
			deadline.add(Calendar.HOUR, jcr.hour());
			deadline.add(Calendar.MINUTE, jcr.minute());
			deadline.add(Calendar.SECOND, jcr.second());
			deadline.add(Calendar.MILLISECOND, jcr.millisecond());
			return deadline.getTime().getTime();
		}
		return 0;
	}
	
	/**
	 * Traite les annotations JsCacheRemove et JsCacheRemoves
	 * @param cls
	 * @param method
	 * @param jsonArgs
	 * @param javaArgs 
	 */
	private void processCleanCacheAnnotations(Method nonProxiedMethod, List<String> jsonArgs) {
		boolean cleanAllCache = nonProxiedMethod.isAnnotationPresent(JsCacheRemoveAll.class);
		if(cleanAllCache) {
			JsCacheRemoveAll jcra = nonProxiedMethod.getAnnotation(JsCacheRemoveAll.class);
			processJsCacheRemoveAll(jcra);
		}
		boolean simpleCleancache = nonProxiedMethod.isAnnotationPresent(JsCacheRemove.class);
		if(simpleCleancache) {
			JsCacheRemove jcr = nonProxiedMethod.getAnnotation(JsCacheRemove.class);
			processJsCacheRemove(jcr, jsonArgs);
		}
		boolean multiCleancache = nonProxiedMethod.isAnnotationPresent(JsCacheRemoves.class);
		if(multiCleancache) {
			JsCacheRemoves jcrs = nonProxiedMethod.getAnnotation(JsCacheRemoves.class);
			for (JsCacheRemove jcr : jcrs.value()) {
				processJsCacheRemove(jcr, jsonArgs);
			}
		}
		if(simpleCleancache || multiCleancache) {
			logger.debug("The method {} will remove caches entries on clients side.", nonProxiedMethod.getName());
		}
	}

	/**
	 * Traite l'annotation JsCacheRemoveAll et envoi un message de suppression de tous le cache
	 * @param jcr
	 */
	private void processJsCacheRemoveAll(JsCacheRemoveAll jcra) {
		logger.trace("Process JsCacheRemoveAll annotation : {}", jcra);
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(Constants.Cache.CLEANCACHE_TOPIC);
		messageToClient.setResult(Constants.Cache.ALL);
		wsEvent.fire(messageToClient);
	}
	
	/**
	 * Traite une annotation JsCacheRemove et envoi un message de suppression de cache
	 * @param jcr
	 * @param jsonArgs
	 */
	private void processJsCacheRemove(JsCacheRemove jcr, List<String> jsonArgs) {
		logger.trace("Process JsCacheRemove annotation : {}", jcr);
		StringBuilder sb = new StringBuilder(jcr.cls().getName()).append(".").append(jcr.methodName());
		MessageToClient messageToClient = new MessageToClient();
		String[] args = new String[jsonArgs.size()];
		KeySelector keySelector = new KeySelector(jcr.keys());
		int index = 0;
		int newindex;
		String[] orders = jcr.orderKeys().split(",");
		for (String arg : jsonArgs) {
			newindex = index;
			if(orders.length>index) {
				try {
					newindex = Integer.parseInt(orders[index]);
				} catch(NumberFormatException nfe) {}
			} 
			args[newindex] = keySelector.nextJSValue(arg);
			index++;
		}
		sb.append("([");
		for (index = 0; index<args.length; index++) {
			sb.append(args[index]);
			if((index+1)<args.length) {
				sb.append(",");
			}
		}
		sb.append("])");
		messageToClient.setId(Constants.Cache.CLEANCACHE_TOPIC);
		String value = sb.toString();
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] bytes = value.getBytes();
			md.update(bytes, 0, bytes.length);
			String md5 = new BigInteger(1, md.digest()).toString(16);
			messageToClient.setResult(md5);
			wsEvent.fire(messageToClient);
		} catch (NoSuchAlgorithmException ex) {
		}
	}

	/**
	 * Récupere la methode sur la classe d'origine en ignorant les eventuels proxies
	 * @param cls
	 * @param methodName
	 * @param parameterTypes
	 * @throws NoSuchMethodException
	 * @return 
	 */
	private Method getNonProxiedMethod(Class cls, String methodName,  Class<?>[] parameterTypes) throws NoSuchMethodException {
		try {
			return cls.getMethod(methodName, parameterTypes);
		} catch (SecurityException ex) {
		}
		throw new NoSuchMethodException(methodName);
	}
}
