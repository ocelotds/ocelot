/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.hhdev.ocelot.encoders.CommandDecoder;
import fr.hhdev.ocelot.encoders.MessageToClientEncoder;
import fr.hhdev.ocelot.messaging.Command;
import fr.hhdev.ocelot.messaging.MessageFromClient;
import java.io.IOException;
import java.util.Map;
import javax.inject.Inject;
import javax.websocket.CloseReason;
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
	private SessionManager sessionManager;

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
}
