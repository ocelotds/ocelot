/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.ocelot;

import fr.hhdev.ocelot.encoders.MessageToClientEncoder;
import fr.hhdev.ocelot.messaging.Command;
import fr.hhdev.ocelot.messaging.MessageFromClient;
import fr.hhdev.ocelot.messaging.MessageToClient;
import fr.hhdev.ocelot.messaging.MessageEvent;
import fr.hhdev.ocelot.resolvers.DataServiceResolver;
import fr.hhdev.ocelot.resolvers.DataServiceResolverIdLitteral;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.ejb.Stateless;
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
 *
 * @author hhfrancois
 */
@ServerEndpoint(value = "/endpoint", encoders = {MessageToClientEncoder.class})
@Stateless
public class OcelotEndpoint extends AbstractOcelotDataService {

	private final static Logger logger = LoggerFactory.getLogger(OcelotEndpoint.class);

	@Inject
	private SessionManager sessionManager;
	
	@Inject
	@MessageEvent
	private Event<MessageToClient> wsEvent;

	@Inject
	@Any
	private Instance<DataServiceResolver> resolvers;

	private DataServiceResolver getResolver(String type) {
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
	 * @param commandMessage
	 */
	@OnMessage
	public void receiveCommandMessage(Session client, String commandMessage) {
		logger.debug("RECEIVE MESSAGE FROM CLIENT '{}'", commandMessage);
		Command command = Command.createFromJson(commandMessage);
		logger.debug("RECEIVE COMMAND FROM CLIENT '{}'", command.getCommand());
		if (null != command.getCommand()) {
			switch (command.getCommand()) {
				case Constants.Command.Value.SUBSCRIBE:
					logger.debug("SUBSCRIBE TOPIC '{}' FOR SESSION", command.getTopic());
					sessionManager.registerTopicSession(command.getTopic(), client);
					break;
				case Constants.Command.Value.UNSUBSCRIBE:
					logger.debug("UNSUBSCRIBE TOPIC '{}' FOR SESSION", command.getTopic());
					sessionManager.unregisterTopicSession(command.getTopic(), client);
					break;
				case Constants.Command.Value.CALL:
					MessageFromClient message = MessageFromClient.createFromJson(command.getMessage());
					logger.debug("ASSOCIATE ID '{}' WITH SESSION", message.getId());
					sessionManager.registerMsgSession(message.getId(), client); // on enregistre le message pour re-router le résultat vers le bon client
					ExecutorService executorService = Executors.newSingleThreadExecutor();
					executorService.execute(new OcelotDataService(getResolver(command.getTopic()), wsEvent, message));
					executorService.shutdown();
					break;
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
}
