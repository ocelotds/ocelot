/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.topic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;
import org.ocelotds.Constants;

/**
 * Link httpSession with wsSession
 * @author hhfrancois
 */
@ApplicationScoped
public class SessionManagerImpl implements SessionManager {
	private final Map<String, Session> map = new HashMap<>();

	public Map<String, Session> getMap() {
		return map;
	}
	
	@Override
	public void addSession(String httpid, Session session) {
		map.put(httpid, session);
	}
	
	@Override
	public Session closeOldSessionForHttp(String httpid) {
		Session s = null;
		if(map.containsKey(httpid)) {
			try {
				s = map.get(httpid);
				s.close();
			} catch (IOException ex) {
			}
		}
		return s;
	}
	
	@Override
	public Session getSessionById(String httpid) {
		return map.get(httpid);
	}
	
	@Override
	public Collection<String> removeSession(Session session) {
		Collection<String> ids = new ArrayList<>();
		if(map.containsValue(session)) {
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
		return ids;
	}

	void removeSession(String httpid) {
		map.remove(httpid);
	}

	@Override
	public String getUsername(Session session) {
		String username = Constants.ANONYMOUS;
		if(null != session && null != session.getUserPrincipal()) {
			username = session.getUserPrincipal().getName();
		}
		return username;
	}
}
