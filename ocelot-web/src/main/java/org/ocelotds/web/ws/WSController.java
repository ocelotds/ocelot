/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web.ws;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.ws.rs.core.HttpHeaders;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.configuration.LocaleExtractor;
import org.ocelotds.context.ThreadLocalContextHolder;
import org.ocelotds.core.ws.CallServiceManager;
import org.ocelotds.exceptions.LocaleNotFoundException;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.topic.TopicManager;
import org.ocelotds.topic.UserContextFactory;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
public class WSController implements IWSController {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	private TopicManager topicManager;

	@Inject
	private CallServiceManager callServiceManager;
	
	@Inject
	private UserContextFactory userContextFactory;
	
	@Inject
	private LocaleExtractor localeExtractor;
	
	
	@Override
	public void handleOpenConnexion(Session session, EndpointConfig config) throws IOException {
		logger.debug("Open connexion for session '{}'", session.getId());
		Map<String, Object> configProperties = config.getUserProperties();
		// Get infos from config and set in session, only one time by connexion
		HandshakeRequest request = (HandshakeRequest) configProperties.get(Constants.HANDSHAKEREQUEST);
		setContext(request);
		userContextFactory.createUserContext(request, session.getId());
	}

	@Override
	public void onError(Session session, Throwable t) {
		logger.error("Unknow error for session " + session.getId()+" : "+t.getMessage());
		if (!session.isOpen()) {
			userContextFactory.destroyUserContext(session.getId());
			topicManager.removeSessionToTopics(session);
		}
	}

	@Override
	public void handleClosedConnexion(Session session, CloseReason closeReason) {
		logger.debug("Close connexion for session '{}' : '{}'", session.getId(), closeReason);
		if (session.isOpen()) {
			try {
				session.close();
			} catch (IllegalStateException | IOException ex) {
			}
		}
		userContextFactory.destroyUserContext(session.getId());
		topicManager.removeSessionToTopics(session);
	}

	/**
	 * A message is a call service request or subscribe/unsubscribe topic
	 *
	 * @param client
	 * @param json
	 */
	@Override
	public void receiveCommandMessage(Session client, String json) {
		MessageFromClient message = MessageFromClient.createFromJson(json);
		logger.debug("Receive call message in websocket '{}' for session '{}'", message.getId(), client.getId());
		callServiceManager.sendMessageToClient(message, client);
	}

	/**
	 * 
	 * @param request 
	 */
	void setContext(HandshakeRequest request) {
		HttpSession httpSession = (HttpSession) request.getHttpSession();
		ThreadLocalContextHolder.put(Constants.HTTPSESSION, httpSession);
		if(null == httpSession.getAttribute(Constants.LOCALE)) {
			httpSession.setAttribute(Constants.LOCALE, getLocale(request));
		}
	}
	
	/**
	 * Return locale of client
	 *
	 * @param request
	 * @return
	 */
	Locale getLocale(HandshakeRequest request) {
		if(null != request) {
			Map<String, List<String>> headers = request.getHeaders();
			if(null != headers) {
				List<String> accepts = headers.get(HttpHeaders.ACCEPT_LANGUAGE);
				logger.debug("Get accept-language from client headers : {}", accepts);
				if (null != accepts) {
					for (String accept : accepts) {
						try {
							return localeExtractor.extractFromAccept(accept);
						} catch (LocaleNotFoundException ex) {
						}
					}
				}
			}
		}
		return Locale.US;
	}
}

