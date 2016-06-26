/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web.ws;

import java.io.IOException;
import java.util.Map;
import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.web.SessionManager;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
/**
 * Decorate websocket
 * @author hhfrancois
 */
@Decorator 
@Priority(0)
public abstract class IWSDecorator implements IWSController {
	@Inject
	@Delegate
	@Any
	IWSController iwse;
	
	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	SessionManager sessionManager;

	@Override
	public void handleOpenConnexion(Session session, EndpointConfig config) throws IOException {
		logger.debug("Decorate websocket, open connexion '{}'", session.getId());
		Map<String, Object> configProperties = config.getUserProperties();
		HandshakeRequest request = (HandshakeRequest) configProperties.get(Constants.HANDSHAKEREQUEST);
		sessionManager.linkWsToHttp(session, ((HttpSession) request.getHttpSession()).getId());
		iwse.handleOpenConnexion(session, config);
	}

	@Override
	public void handleClosedConnexion(Session session, CloseReason closeReason) {
		logger.debug("Decorate websocket, close connexion '{}'", session.getId());
		sessionManager.unlinkWs(session.getId());
		iwse.handleClosedConnexion(session, closeReason);
	}
	
}
