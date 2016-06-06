/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.web.ws;

import java.io.IOException;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

/**
 * WebSocket endpoint
 *
 * @author hhfrancois
 */
public interface IWSController {

	/**
	 * A connection is open
	 *
	 * @param session
	 * @param config
	 * @throws IOException
	 */
	void handleOpenConnexion(Session session, EndpointConfig config) throws IOException;

	/**
	 * Error on session
	 * @param session
	 * @param t 
	 */
	void onError(Session session, Throwable t);

	/**
	 * Close a session
	 *
	 * @param session
	 * @param closeReason
	 */
	void handleClosedConnection(Session session, CloseReason closeReason);

	/**
	 * A message is a call service request or subscribe/unsubscribe topic
	 *
	 * @param client
	 * @param json
	 */
	void receiveCommandMessage(Session client, String json);
}
