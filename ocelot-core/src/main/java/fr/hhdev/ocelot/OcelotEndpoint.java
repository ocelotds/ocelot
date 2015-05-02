/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.hhdev.ocelot.annotations.DataService;
import fr.hhdev.ocelot.encoders.CommandDecoder;
import fr.hhdev.ocelot.spi.Scope;
import fr.hhdev.ocelot.encoders.MessageToClientEncoder;
import fr.hhdev.ocelot.messaging.Command;
import fr.hhdev.ocelot.messaging.Fault;
import fr.hhdev.ocelot.messaging.MessageFromClient;
import fr.hhdev.ocelot.messaging.MessageToClient;
import fr.hhdev.ocelot.messaging.MessageEvent;
import fr.hhdev.ocelot.spi.DataServiceException;
import fr.hhdev.ocelot.spi.IDataServiceResolver;
import fr.hhdev.ocelot.resolvers.DataServiceResolverIdLitteral;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
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
public class OcelotEndpoint {

	private final static Logger logger = LoggerFactory.getLogger(OcelotEndpoint.class);

	@Inject
	private SessionManager sessionManager;

	@Inject
	@MessageEvent
	private Event<MessageToClient> wsEvent;

	@Inject
	@Any
	private Instance<IDataServiceResolver> resolvers;

	protected IDataServiceResolver getResolver(String type) {
		return resolvers.select(new DataServiceResolverIdLitteral(type)).get();
	}

	/**
	 * Réponse asynchrone d'un requete initiée par le client Utilise l'encoder positionné sur le endpoint
	 *
	 * @param msg
	 */
	public void sendPushMessageToClient(@Observes @MessageEvent MessageToClient msg) {
		logger.debug("SENDING MESSAGE/RESPONSE TO CLIENT {}", msg.toJson());
		try {
			if (sessionManager.existsMsgSessionForId(msg.getId())) {
				logger.debug("SEND MESSAGE TO CLIENT {}", msg.toJson());
				Session session = sessionManager.getAndRemoveMsgSessionForId(msg.getId());
				if (session.isOpen()) {
					session.getBasicRemote().sendObject(msg);
				}
			} else if (sessionManager.existsTopicSessionForId(msg.getId())) {
				Collection<Session> sessions = sessionManager.getTopicSessionsForId(msg.getId());
				if (sessions != null && !sessions.isEmpty()) {
					logger.debug("SEND MESSAGE TO '{}' TOPIC {} CLIENT(s) : {}", new Object[]{msg.getId(), sessions.size(), msg.toJson()});
					for (Session session : sessions) {
						if (session.isOpen()) {
							session.getBasicRemote().sendObject(msg);
						}
					}
				} else {
					logger.debug("NO CLIENT FOR TOPIC '{}'", msg.getId());
				}
			} else {
				logger.debug("NO CLIENT FOR MSGID '{}'", msg.getId());
			}
		} catch (IOException | EncodeException ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	/**
	 * Ferme une session
	 *
	 * @param session
	 * @param closeReason
	 */
	@OnClose
	public void handleClosedConnection(Session session, CloseReason closeReason) {
		if (session.isOpen()) {
			try {
				session.close();
			} catch (IOException ex) {
			}
			sessionManager.removeSession(session);
		}
	}

	/**
	 * Recevoir un message correspond à la demande d'execution d'un service
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
						logger.debug("SUBSCRIBE TOPIC '{}' FOR SESSION", topic);
						sessionManager.registerTopicSession(topic, client);
						break;
					case Constants.Command.Value.UNSUBSCRIBE:
						topic = mapper.readValue(command.getMessage(), String.class);
						logger.debug("UNSUBSCRIBE TOPIC '{}' FOR SESSION", topic);
						sessionManager.unregisterTopicSession(topic, client);
						break;
					case Constants.Command.Value.CALL:
						MessageFromClient message = MessageFromClient.createFromJson(command.getMessage());
						try {
							logger.debug("ASSOCIATE ID '{}' WITH SESSION", message.getId());
							sessionManager.registerMsgSession(message.getId(), client); // on enregistre le message pour re-router le résultat vers le bon client
							ExecutorService executorService = Executors.newFixedThreadPool(1000); // TODO externaliser ce parametre dans un fichier de config
							Object dataService = getDataService(client, message.getDataService());
							executorService.execute(new OcelotDataService(dataService, wsEvent, message));
							executorService.shutdown();
							break;
						} catch (ClassNotFoundException | DataServiceException ex) {
							MessageToClient messageToClient = new MessageToClient();
							messageToClient.setId(message.getId());
							messageToClient.setFault(new Fault(ex));
							this.wsEvent.fire(messageToClient);
						}
				}
			} catch (IOException ex) {

			}
		}
	}

	@OnOpen
	public void handleOpenConnection(Session session) throws IOException {
	}

	@OnError
	public void onError(Session session, Throwable t) {
		System.out.println("ERROR");
		t.printStackTrace();
	}

	/**
	 * Get Dataservice, maybe in session if session scope and stored J'aimerais bien faire cela avec un interceptor/decorator, mais comment passer la session à
	 * celui ci ?
	 *
	 * @param client
	 * @param dataServiceClassName
	 * @return
	 * @throws ClassNotFoundException
	 * @throws DataServiceException
	 */
	protected Object getDataService(Session client, String dataServiceClassName) throws ClassNotFoundException, DataServiceException {
		Class cls = Class.forName(dataServiceClassName);
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
	}

}
