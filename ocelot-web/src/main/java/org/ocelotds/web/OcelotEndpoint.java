/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.web;

import org.ocelotds.core.SessionManager;
import org.ocelotds.Constants;
import org.ocelotds.configuration.OcelotRequestConfigurator;
import org.ocelotds.core.CallServiceManager;
import org.ocelotds.encoders.MessageToClientEncoder;
import java.io.IOException;
import org.ocelotds.messaging.MessageFromClient;
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
import org.ocelotds.logger.OcelotLogger;
import org.slf4j.Logger;

/**
 * WebSocket endpoint
 *
 * @author hhfrancois
 */
@ServerEndpoint(value = "/ocelot-endpoint", encoders = {MessageToClientEncoder.class}, configurator = OcelotRequestConfigurator.class)
public class OcelotEndpoint extends CdiBootstrap {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	private SessionManager sessionManager;

	@Inject
	private CallServiceManager callServiceManager;

	/**
	 * A connection is open
	 *
	 * @param session
	 * @param config
	 * @throws IOException
	 */
	@OnOpen
	public void handleOpenConnexion(Session session, EndpointConfig config) throws IOException {
		Map<String, Object> configProperties = config.getUserProperties();
		Map<String, Object> sessionProperties = session.getUserProperties();
		// Get subject from config and set in session, only one time by connexion
		sessionProperties.put(Constants.SUBJECT, configProperties.get(Constants.SUBJECT));
		sessionProperties.put(Constants.LOCALE, configProperties.get(Constants.LOCALE));
		sessionProperties.put(Constants.PRINCIPAL, configProperties.get(Constants.PRINCIPAL));
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
			} catch (IllegalStateException | IOException ex) {
			}
			getSessionManager().removeSessionToTopics(session);
		}
	}

	/**
	 * A message is a call service request or subscribe/unsubscribe topic
	 *
	 * @param client
	 * @param json
	 */
	@OnMessage
	public void receiveCommandMessage(Session client, String json) {
		MessageFromClient message = MessageFromClient.createFromJson(json);
		logger.debug("Receive call message '{}' for session '{}'", message.getId(), client.getId());
		getCallServiceManager().sendMessageToClient(message, client);
	}

	SessionManager getSessionManager() {
		if (null == sessionManager) {
			sessionManager = getBean(SessionManager.class);
		}
		return sessionManager;
	}

	CallServiceManager getCallServiceManager() {
		if (null == callServiceManager) {
			callServiceManager = getBean(CallServiceManager.class);
		}
		return callServiceManager;
	}

}
