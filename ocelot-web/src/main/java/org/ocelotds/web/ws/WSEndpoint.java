/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.web.ws;

import org.ocelotds.configuration.OcelotRequestConfigurator;
import org.ocelotds.encoders.MessageToClientEncoder;
import java.io.IOException;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.ocelotds.core.CdiBeanResolver;

/**
 * WebSocket endpoint
 *
 * @author hhfrancois
 */
@ServerEndpoint(value = "/ocelot-endpoint", encoders = {MessageToClientEncoder.class}, configurator = OcelotRequestConfigurator.class)
public class WSEndpoint {

	@Inject
	private WSController controller;

	@OnOpen
	public void handleOpenConnexion(Session session, EndpointConfig config) throws IOException {
		getWSController().handleOpenConnexion(session, config);
	}

	@OnError
	public void onError(Session session, Throwable t) {
		getWSController().onError(session, t);
	}

	@OnClose
	public void handleClosedConnection(Session session, CloseReason closeReason) {
		getWSController().handleClosedConnection(session, closeReason);
	}

	@OnMessage
	public void receiveCommandMessage(Session client, String json) {
		getWSController().receiveCommandMessage(client, json);
	}
	
	
	/**
	 * Fix OpenWebBean issues
	 * @return 
	 */
	WSController getWSController() {
		if (null == controller) {
			controller = getCdiBeanResolver().getBean(WSController.class);
		}
		return controller;
	}

	CdiBeanResolver getCdiBeanResolver() {
		return new CdiBeanResolver();
	}

}
