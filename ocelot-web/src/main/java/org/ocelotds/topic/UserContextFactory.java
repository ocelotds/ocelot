/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.topic;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.server.HandshakeRequest;
import org.ocelotds.objects.WsUserContext;
import org.ocelotds.security.UserContext;

/**
 * Link wsSession with wsRequest
 *
 * @author hhfrancois
 */
@ApplicationScoped
public class UserContextFactory {

	private final Map<String, UserContext> map = new HashMap<>();

	Map<String, UserContext> getMap() {
		return map;
	}

	public void createUserContext(HandshakeRequest request, String wsid) {
		if (request != null) {
			if (wsid != null) {
				map.put(wsid, new WsUserContext(request));
			}
		} else {
			destroyUserContext(wsid);
		}
	}

	public void destroyUserContext(String wsid) {
		if(containsKey(wsid)) {
			map.remove(wsid);
		}
	}

	public UserContext getUserContext(String wsid) {
		if(containsKey(wsid)) {
			return map.get(wsid);
		}
		return  null;
	}
	
	boolean containsKey(String wsid) {
		return null != wsid && map.containsKey(wsid);
	}
}
