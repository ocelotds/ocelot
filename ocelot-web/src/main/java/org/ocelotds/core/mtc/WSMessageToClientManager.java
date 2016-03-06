/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.mtc;

import java.util.HashMap;
import java.util.Map;
import javax.websocket.Session;

/**
 *
 * @author hhfrancois
 */
public class WSMessageToClientManager extends MessageToClientManager<Session> {

	@Override
	public Map<String, Object> getSessionBeans(Session session) {
		return new HashMap<>();
	}
}
