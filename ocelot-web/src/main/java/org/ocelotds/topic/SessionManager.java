/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.topic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;

/**
 * Link httpSession with wsSession
 * @author hhfrancois
 */
@ApplicationScoped
public class SessionManager {
	private final Map<String, Session> map = new HashMap<>();

	public Map<String, Session> getMap() {
		return map;
	}
	
	public void addSession(String httpid, Session session) {
		removeSession(session);
		map.put(httpid, session);
	}
	
	public void removeSession(Session session) {
		if(map.containsValue(session)) {
			Collection<String> ids = new ArrayList<>();
			Set<Map.Entry<String, Session>> entries = map.entrySet();
			for (Map.Entry<String, Session> entry : entries) {
				if(entry.getValue().equals(session)) {
					ids.add(entry.getKey());
				}
			}
			for (String id : ids) {
				removeSession(id);
			}
		}
	}

	public void removeSession(String httpid) {
		map.remove(httpid);
	}
	public Session getSessionById(String httpid) {
		return map.get(httpid);
	}
	
}
