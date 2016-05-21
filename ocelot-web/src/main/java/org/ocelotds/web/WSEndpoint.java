/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.web;

import org.ocelotds.topic.TopicManager;
import org.ocelotds.Constants;
import org.ocelotds.configuration.OcelotRequestConfigurator;
import org.ocelotds.core.ws.CallServiceManager;
import org.ocelotds.encoders.MessageToClientEncoder;
import java.io.IOException;
import org.ocelotds.messaging.MessageFromClient;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
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
import org.ocelotds.topic.UserContextFactory;
import org.ocelotds.topic.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket endpoint
 *
 * @author hhfrancois
 */
@ServerEndpoint(value = "/ocelot-endpoint", encoders = {MessageToClientEncoder.class}, configurator = OcelotRequestConfigurator.class)
public class WSEndpoint {

	private static Logger logger = LoggerFactory.getLogger(WSEndpoint.class);

	@Inject
	private TopicManager topicManager;

	@Inject
	private CallServiceManager callServiceManager;
	
	@Inject
	private UserContextFactory userContextFactory;
	
	@Inject
	private SessionManager sessionManager;

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
		String id = ((HttpSession) request.getHttpSession()).getId();
		getSessionManager().closeOldSessionForHttp(id);
		getSessionManager().addSession(id, session);
		getUserContextFactory().createUserContext(request, session.getId());
	}

	@OnError
	public void onError(Session session, Throwable t) {
		logger.error("Unknow error for session " + session.getId(), t);
		if (!session.isOpen()) {
			getUserContextFactory().destroyUserContext(session.getId());
			getTopicManager().removeSessionToTopics(session);
			getSessionManager().removeSession(session);
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
		logger.debug("Close connexion for session '{}' : '{}'", session.getId(), closeReason.getCloseCode());
		if (session.isOpen()) {
			try {
				session.close();
			} catch (IllegalStateException | IOException ex) {
			}
		}
		getUserContextFactory().destroyUserContext(session.getId());
		getTopicManager().removeSessionToTopics(session);
		getSessionManager().removeSession(session);
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
			sessionManager = getCdiBeanResolver().getBean(SessionManager.class);
		}
		return sessionManager;
	}

	TopicManager getTopicManager() {
		if (null == topicManager) {
			topicManager = getCdiBeanResolver().getBean(TopicManager.class);
		}
		return topicManager;
	}

	UserContextFactory getUserContextFactory() {
		if (null == userContextFactory) {
			userContextFactory = getCdiBeanResolver().getBean(UserContextFactory.class);
		}
		return userContextFactory;
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
