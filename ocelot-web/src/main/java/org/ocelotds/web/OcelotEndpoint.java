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
import org.ocelotds.i18n.ThreadLocalContextHolder;
import java.io.IOException;
import java.util.List;
import org.ocelotds.messaging.MessageFromClient;
import java.util.Locale;
import java.util.Objects;
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
		Locale locale = (Locale) session.getUserProperties().get(Constants.LOCALE);
		if (Objects.isNull(locale)) {
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
		Locale locale = (Locale) client.getUserProperties().get(Constants.LOCALE);
		if (Objects.nonNull(locale)) {
			logger.debug("Locale is set in session : {}", locale);
			ThreadLocalContextHolder.put(Constants.LOCALE, locale);
		}
		MessageFromClient message = MessageFromClient.createFromJson(json);
		logger.debug("Receive call message '{}' for session '{}'", message.getId(), client.getId());
		getCallServiceManager().sendMessageToClient(message, client);
	}

	private SessionManager getSessionManager() {
		if (Objects.isNull(sessionManager)) {
			sessionManager = getBean(SessionManager.class);
		}
		return sessionManager;
	}

	private CallServiceManager getCallServiceManager() {
		if (Objects.isNull(callServiceManager)) {
			callServiceManager = getBean(CallServiceManager.class);
		}
		return callServiceManager;
	}

}
