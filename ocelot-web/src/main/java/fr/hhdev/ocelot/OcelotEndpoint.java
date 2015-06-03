/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.hhdev.ocelot.annotations.DataService;
import fr.hhdev.ocelot.annotations.JsCacheResult;
import fr.hhdev.ocelot.encoders.CommandDecoder;
import fr.hhdev.ocelot.spi.Scope;
import fr.hhdev.ocelot.encoders.MessageToClientEncoder;
import fr.hhdev.ocelot.messaging.Command;
import fr.hhdev.ocelot.messaging.Fault;
import fr.hhdev.ocelot.messaging.MessageFromClient;
import fr.hhdev.ocelot.messaging.MessageToClient;
import fr.hhdev.ocelot.spi.DataServiceException;
import fr.hhdev.ocelot.spi.IDataServiceResolver;
import fr.hhdev.ocelot.resolvers.DataServiceResolverIdLitteral;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import javax.el.MethodNotFoundException;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
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
@ServerEndpoint(value = "/endpoint", encoders = {MessageToClientEncoder.class}, decoders = {CommandDecoder.class})
public class OcelotEndpoint extends AbstractOcelotDataService {

	private final static Logger logger = LoggerFactory.getLogger(OcelotEndpoint.class);

	@Inject
	private SessionManager sessionManager;

	@Inject
	@Any
	private Instance<IDataServiceResolver> resolvers;

	protected IDataServiceResolver getResolver(String type) {
		return resolvers.select(new DataServiceResolverIdLitteral(type)).get();
	}

	@OnOpen
	public void handleOpenConnexion(Session session) throws IOException {
		logger.debug("OPEN CONNEXION FOR SESSION '{}'", session.getId());
	}

	@OnError
	public void onError(Session session, Throwable t) {
		logger.error("UNKNOW ERROR FOR SESSION " + session.getId(), t);
	}

	/**
	 * Ferme une session
	 *
	 * @param session
	 * @param closeReason
	 */
	@OnClose
	public void handleClosedConnection(Session session, CloseReason closeReason) {
		logger.debug("CLOSE CONNEXION FOR SESSION '{}' : '{}'", session.getId(), closeReason.getCloseCode());
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
		logger.debug("RECEIVE MESSAGE FROM CLIENT '{}'", command);
		if (null != command.getCommand()) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				String topic;
				switch (command.getCommand()) {
					case Constants.Command.Value.SUBSCRIBE:
						topic = mapper.readValue(command.getMessage(), String.class);
						logger.debug("SUBSCRIBE TOPIC '{}' FOR SESSION '{}'", topic, client.getId());
						sessionManager.registerTopicSession(topic, client);
						break;
					case Constants.Command.Value.UNSUBSCRIBE:
						topic = mapper.readValue(command.getMessage(), String.class);
						logger.debug("UNSUBSCRIBE TOPIC '{}' FOR SESSION '{}'", topic, client.getId());
						sessionManager.unregisterTopicSession(topic, client);
						break;
					case Constants.Command.Value.CALL:
						MessageFromClient message = MessageFromClient.createFromJson(command.getMessage());
						logger.debug("RECEIVE CALL MESSAGE '{}' FOR SESSION '{}'", message.getId(), client.getId());
						MessageToClient messageToClient = getMesssageToClient(client, message);
						try {
							client.getBasicRemote().sendObject(messageToClient);
							break;
						} catch (IOException | EncodeException ex) {
							logger.error("FAIL TO SENT " + messageToClient.toJson(), ex);
						}
				}
			} catch (IOException ex) {
			}
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
		logger.debug("Dataservice : {}", dataServiceClassName);
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
	 * Construction du message response suite à un appel de type call
	 *
	 * @param client
	 * @param message
	 * @return
	 */
	private MessageToClient getMesssageToClient(Session client, MessageFromClient message) {
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(message.getId());
		try {
			Class cls = Class.forName(message.getDataService());
			Object dataService = getDataService(client, cls);
			logger.debug("Excecution de la methode pour le message {}", message);
			try {
				logger.debug("Invocation de  : {}", message.getOperation());
				Object[] arguments = new Object[message.getParameters().size()];
				logger.debug("Réceptacle de {} argument(s) typé(s).", arguments.length);
				Method method = getMethodFromDataService(dataService, message, arguments);
				logger.debug("Excecution de la méthode {}.", method.getName());
				Object result = method.invoke(dataService, arguments);
				messageToClient.setResult(result);
				messageToClient.setDeadline(getJsCacheResultDeadline(cls, method));
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
		return messageToClient;
	}

	/**
	 * Récupere la date d'expiration du service via l'annotation sur la methode directement sur la classe, car sinon cela peut être un proxy
	 *
	 * @param cls
	 * @param method
	 * @return
	 */
	private long getJsCacheResultDeadline(Class cls, Method method) {
		try {
			Method m = cls.getMethod(method.getName(), method.getParameterTypes());
			boolean cached = m.isAnnotationPresent(JsCacheResult.class);
			logger.debug("La résultat de la méthode {} sera mis en cache sur le client {}", method.getName(), cached);
			if (cached) { // Ce service doit être mis en cache sur le client
				JsCacheResult jcr = m.getAnnotation(JsCacheResult.class);
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
		} catch (NoSuchMethodException | SecurityException ex) {
			logger.error("Methode "+ method.getName()+" non trouvé sur "+cls.getName());
		}
		return 0;
	}
}
