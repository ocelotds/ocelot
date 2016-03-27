/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import org.ocelotds.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extract info in request, and expose them to userProperties ServerEnpoint
 *
 * @author hhfrancois
 */
public class OcelotRequestConfigurator extends ServerEndpointConfig.Configurator {

	private final Logger logger = LoggerFactory.getLogger(OcelotRequestConfigurator.class);

	/**
	 * Set user information from open websocket
	 *
	 * @param sec
	 * @param request
	 * @param response
	 */
	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		sec.getUserProperties().put(Constants.HTTPSESSION, request.getHttpSession());
		sec.getUserProperties().put(Constants.HANDSHAKEREQUEST, request);
		super.modifyHandshake(sec, request, response);
	}
}
