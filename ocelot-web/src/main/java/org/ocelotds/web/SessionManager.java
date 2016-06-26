/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;

/**
 *
 * @author hhfrancois
 */
@ApplicationScoped
public class SessionManager {
	Map<String, Collection<Session>> httpid_wss = new HashMap<>();
	Map<String, String> wsid_httpid = new HashMap<>();
	
	public void linkWsToHttp(Session ws, String httpid) {
		if(ws != null && httpid != null) {
			System.out.println("LINK SESSION : "+ws.getId()+" / "+httpid);
			Collection<Session> wss = getOpenSessions(httpid);
			wss.add(ws);
			wsid_httpid.put(ws.getId(), httpid);
			httpid_wss.put(httpid, wss);
		}
	}

	public void unlinkWs(String wsid) {
		if(wsid != null && wsid_httpid.containsKey(wsid)) {
			String httpid = wsid_httpid.get(wsid);
			Collection<Session> wss = getOpenSessions(httpid);
			if(!wss.isEmpty()) {
				httpid_wss.put(httpid, wss);
			} else {
				httpid_wss.remove(httpid);
			}
		}
	}
	
	Collection<Session> getOpenSessions(String httpid) {
		Collection<Session> wss = new ArrayList();
		if(httpid_wss.containsKey(httpid)) {
			Collection<Session> befores = httpid_wss.get(httpid);
			for (Session before : befores) {
				if(before.isOpen()) {
					wss.add(before);
				} else {
					wsid_httpid.remove(before.getId());
				}
			}
		}
		return wss;
	}
	
	public Collection<Session> getUserSessions(String httpid) {
		return Collections.unmodifiableCollection(getOpenSessions(httpid));
	}
	
	
}
