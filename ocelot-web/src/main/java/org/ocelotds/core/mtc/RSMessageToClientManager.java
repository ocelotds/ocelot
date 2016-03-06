/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.mtc;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.ocelotds.Constants;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.messaging.MessageToClient;

/**
 *
 * @author hhfrancois
 */
public class RSMessageToClientManager extends MessageToClientManager<HttpSession> implements RSMessageToClientService {

	@Override
	public Map<String, Object> getSessionBeans(HttpSession session) {
		Map<String, Object> result = (Map<String, Object>) session.getAttribute(Constants.SESSION_BEANS);
		if (result == null) {
			result = new HashMap<>();
			session.setAttribute(Constants.SESSION_BEANS, result);
		}
		return result;
	}
	
	/**
	 * TODO Je surcharge la methode ici, car sinon le decorator n'est pas appliqué
	 * A remonter comme un bug probable
	 * @param message
	 * @param client
	 * @return 
	 */
	@Override
	public MessageToClient createMessageToClient(MessageFromClient message, HttpSession client) {
		return super.createMessageToClient(message, client);
	}

}
