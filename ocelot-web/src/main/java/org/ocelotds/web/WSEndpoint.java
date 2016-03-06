/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.web;

import org.ocelotds.core.SessionManager;
import org.ocelotds.Constants;
import org.ocelotds.configuration.OcelotRequestConfigurator;
import org.ocelotds.core.ws.CallServiceManager;
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
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpoint;
import org.ocelotds.core.CdiBeanResolver;
import org.ocelotds.annotations.OcelotLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket endpoint
 *
 * @author hhfrancois
 */
@ServerEndpoint(value = "/ocelot-endpoint", encoders = {MessageToClientEncoder.class}, configurator = OcelotRequestConfigurator.class)
public class WSEndpoint {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	private SessionManager sessionManager;

	@Inject
	private CallServiceManager callServiceManager;
	
	@Inject
	private RequestManager requestManager;

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
		// Get infos from config and set in session, only one time by connexion
		HandshakeRequest request = (HandshakeRequest) configProperties.get(Constants.HANDSHAKEREQUEST);
		getRequestManager().addSession(request, session);
	}

	@OnError
	public void onError(Session session, Throwable t) {
		getLogger().error("Unknow error for session " + session.getId(), t);
		if (!session.isOpen()) {
			getRequestManager().removeSession(session);
			getSessionManager().removeSessionToTopics(session);
		}
	}

	/**
	 * Close a session
	 *
	 * @param session
	 * @param closeReason
	 */
	@OnClose
	public void handleClosedConnection(Session session, CloseReason closeReason) {
		getLogger().debug("Close connexion for session '{}' : '{}'", session.getId(), closeReason.getCloseCode());
		if (session.isOpen()) {
			try {
				session.close();
			} catch (IllegalStateException | IOException ex) {
			}
		}
		getRequestManager().removeSession(session);
		getSessionManager().removeSessionToTopics(session);
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
		getLogger().debug("Receive call message '{}' for session '{}'", message.getId(), client.getId());
		getCallServiceManager().sendMessageToClient(message, client);
	}

	Logger getLogger() {
		if (null == logger) {
			logger = LoggerFactory.getLogger(WSEndpoint.class);
		}
		return logger;
	}

	SessionManager getSessionManager() {
		if (null == sessionManager) {
			sessionManager = getCdiBeanResolver().getBean(SessionManager.class);
		}
		return sessionManager;
	}

	RequestManager getRequestManager() {
		if (null == requestManager) {
			requestManager = getCdiBeanResolver().getBean(RequestManager.class);
		}
		return requestManager;
	}

	CallServiceManager getCallServiceManager() {
		if (null == callServiceManager) {
			callServiceManager = getCdiBeanResolver().getBean(CallServiceManager.class);
		}
		return callServiceManager;
	}

	CdiBeanResolver getCdiBeanResolver() {
		return new CdiBeanResolver();  // Tomcat exploit
	}

}
