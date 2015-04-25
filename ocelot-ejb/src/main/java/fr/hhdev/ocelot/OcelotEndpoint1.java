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
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
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
//@Named(value = "OcelotEndpoint1")
//@Stateless
//@ServerEndpoint(value = "/endpoint", encoders = {MessageToClientEncoder.class})
public class OcelotEndpoint1 {

	private static final Logger logger = LoggerFactory.getLogger(OcelotEndpoint1.class);

	private final Map<String, Set<Session>> sessionsByTopic = new HashMap<>();
	private final Map<String, Session> sessionsByMsgId = new HashMap<>();

	@EJB
	private OcelotDataService ods;

	/**
	 * Réponse asynchrone d'un requete initiée par le client Utilise l'encoder positionné sur le endpoint
	 *
	 * @param msg
	 */
	public void sendPushMessageToClient(@Observes @MessageEvent MessageToClient msg) {
		logger.debug("SENDING MESSAGE/RESPONSE TO CLIENT {}", msg.toJson());
		try {
			if (sessionsByMsgId.containsKey(msg.getId())) {
				logger.debug("SEND MESSAGE TO CLIENT {}", msg.toJson());
				Session session = sessionsByMsgId.remove(msg.getId());
				if (session.isOpen()) {
					session.getBasicRemote().sendObject(msg);
				}
			} else if (sessionsByTopic.containsKey(msg.getId())) {
				Set<Session> sessions = sessionsByTopic.get(msg.getId());
				if (sessions != null && !sessions.isEmpty()) {
					logger.debug("SEND MESSAGE TO '{}' TOPIC {} CLIENT(s) : {}", new Object[] {msg.getId(), sessions.size(), msg.toJson()});
					for (Session session : sessions) {
						if (session.isOpen()) {
							session.getBasicRemote().sendObject(msg);
						}
					}
				} else {
					logger.debug("NO CLIENT FOR TOPIC {}", msg.getId());
				}
			} else {
				logger.debug("NO CLIENT FOR MSGID {}", msg.getId());
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
			for (String topic : sessionsByTopic.keySet()) {
				Set<Session> sessions = sessionsByTopic.get(topic);
				if (sessions.contains(session)) {
					sessions.remove(session);
				}
			}
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
					subscribeToTopic(command.getTopic(), client);
					break;
				case Constants.Command.Value.UNSUBSCRIBE:
					unsubscribeToTopic(command.getTopic(), client);
					break;
				case Constants.Command.Value.CALL:
					MessageFromClient message = MessageFromClient.createFromJson(command.getMessage());
					sessionsByMsgId.put(message.getId(), client); // on enregistre le message pour re-router le résultat vers le bon client
					logger.debug("OcelotDataService est injecté : {}", ods!=null);
					ods.excecute(command.getTopic(), message);
					break;
			}
		}
	}

	/**
	 * Enregistre une session correspondant à un topic
	 *
	 * @param topic
	 * @param session
	 */
	private void subscribeToTopic(String topic, Session session) {
		Set<Session> sessions;
		if (sessionsByTopic.containsKey(topic)) {
			sessions = sessionsByTopic.get(topic);
		} else {
			sessions = new HashSet<>();
			sessionsByTopic.put(topic, sessions);
		}
		logger.debug("SUBSCRIPTION TO '{}'", topic);
		sessions.add(session);
	}

	/**
	 * Dés-Enregistre une session correspondant à un topic
	 *
	 * @param topic
	 * @param session
	 */
	private void unsubscribeToTopic(String topic, Session session) {
		logger.debug("UNSUBSCRIPTION TO '{}'", topic);
		Set<Session> sessions = sessionsByTopic.get(topic);
		if (sessions != null && sessions.contains(session)) {
			sessions.remove(session);
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
