/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.services;

import javax.websocket.Session;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.messaging.MessageToClient;

/**
 *
 * @author hhfrancois
 */
public interface MessageToClientService {

	/**
	 * Create a MessageToClient from MessageFromClient for client
	 * @param message
	 * @param client
	 * @return
	 */
	MessageToClient createMessageToClient(MessageFromClient message, Session client);
}
