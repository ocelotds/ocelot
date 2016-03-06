/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.ws;

import javax.websocket.Session;
import org.ocelotds.messaging.MessageFromClient;

/**
 *
 * @author hhfrancois
 */
public interface CallService {
	boolean sendMessageToClient(MessageFromClient message, Session client);
}
