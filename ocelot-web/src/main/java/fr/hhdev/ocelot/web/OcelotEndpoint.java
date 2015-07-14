/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.configuration.OcelotRequestConfigurator;
import fr.hhdev.ocelot.core.CallServiceManager;
import fr.hhdev.ocelot.core.OcelotServicesManager;
import fr.hhdev.ocelot.encoders.CommandDecoder;
import fr.hhdev.ocelot.encoders.MessageToClientEncoder;
import fr.hhdev.ocelot.i18n.ThreadLocalContextHolder;
import fr.hhdev.ocelot.messaging.Command;
import fr.hhdev.ocelot.messaging.MessageFromClient;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.core.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket endpoint
 *
 * @author hhfrancois
 */
@ServerEndpoint(value = "/ocelot-endpoint", encoders = {MessageToClientEncoder.class}, decoders = {CommandDecoder.class}, configurator = OcelotRequestConfigurator.class)
public class OcelotEndpoint {

	private final static Logger logger = LoggerFactory.getLogger(OcelotEndpoint.class);

	@Inject
	private SessionManager sessionManager;

	@Inject
	private OcelotServicesManager ocelotServicesManager;
	
	@Inject
	private CallServiceManager callServiceManager;

	/**
	 * A connection is open
	 * @param session
	 * @param config
	 * @throws IOException 
	 */
	@OnOpen
	public void handleOpenConnexion(Session session, EndpointConfig config) throws IOException {
		Locale locale = (Locale) session.getUserProperties().get(Constants.LOCALE);
		if (null == locale) {
			logger.debug("Locale is not set in session, get from config...");
			List<String> accepts = (List<String>) config.getUserProperties().get(HttpHeaders.ACCEPT_LANGUAGE);
			locale = new Locale("en", "US");
			for (String accept : accepts) {
				Pattern pattern = Pattern.compile("(\\w\\w)-(\\w\\w).*");
				Matcher matcher = pattern.matcher(accept);
				if (matcher.matches() && matcher.groupCount() == 2) {
					locale = new Locale(matcher.group(1), matcher.group(2));
					break;
				}
			}
			session.getUserProperties().put(Constants.LOCALE, locale);
		}
		ThreadLocalContextHolder.put(Constants.LOCALE, locale);
		logger.debug("Open connexion for session '{}' LOCALE : {}", session.getId(), locale);
	}

	@OnError
	public void onError(Session session, Throwable t) {
		logger.error("Unknow error for session " + session.getId(), t);
	}

	/**
	 * Close a session
	 *
	 * @param session
	 * @param closeReason
	 */
	@OnClose
	public void handleClosedConnection(Session session, CloseReason closeReason) {
		logger.debug("Close connexion for session '{}' : '{}'", session.getId(), closeReason.getCloseCode());
		if (session.isOpen()) {
			try {
				session.close();
			} catch (IOException ex) {
			}
			sessionManager.removeSessionToTopic(session);
		}
	}

	/**
	 * A message is a call service request or subscribe/unsubscribe topic
	 *
	 * @param client
	 * @param command
	 */
	@OnMessage
	public void receiveCommandMessage(Session client, Command command) {
		Locale locale = (Locale) client.getUserProperties().get(Constants.LOCALE);
		if (null != locale) {
			logger.debug("Locale is set in session : {}", locale);
			ThreadLocalContextHolder.put(Constants.LOCALE, locale);
		}
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
						if ("fr.hhdev.ocelot.OcelotServices".equals(message.getDataService())) {
							ocelotServicesManager.processOcelotServices(client, message);
							break;
						}
						logger.debug("Receive call message '{}' for session '{}'", message.getId(), client.getId());
						callServiceManager.sendMessageToClients(client, message);
						break;
					default:
						break;
				}
			} catch (IOException ex) {
			}
		} else {
			logger.warn("Receive unsupported message '{}' from client '{}'", command, client.getId());
		}
	}
}
