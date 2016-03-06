/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.core.ws;

import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.messaging.MessageToClient;
import javax.inject.Inject;
import javax.websocket.Session;
import org.ocelotds.core.mtc.WSMessageToClientManager;

/**
 * Abstract class of OcelotDataService
 *
 * @author hhfrancois
 */
public class CallServiceManager implements CallService {

	@Inject
	private WSMessageToClientManager messageToClientService;
	
	/**
	 * Build and send response messages after call request
	 *
	 * @param message
	 * @param client
	 * @return
	 */
	@Override
	public boolean sendMessageToClient(MessageFromClient message, Session client) {
		MessageToClient mtc = messageToClientService.createMessageToClient(message, client);
		if (mtc != null) {
			client.getAsyncRemote().sendObject(mtc);
			return true;
		}
		return false;
	}
}
