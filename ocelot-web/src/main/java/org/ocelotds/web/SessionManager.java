/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;
import org.ocelotds.web.ws.predicates.IsOpenAndDifferentPredicate;
import org.ocelotds.web.ws.predicates.IsOpenPredicate;

/**
 *
 * @author hhfrancois
 */
@ApplicationScoped
public class SessionManager {
	Map<String, Collection<Session>> httpid_wss = new HashMap<>();
	Map<String, String> wsid_httpid = new HashMap<>();
	
	Predicate<Session> ISOPEN = new IsOpenPredicate();

	public void linkWsToHttp(Session ws, String httpid) {
		if(ws != null && httpid != null) {
			Collection<Session> wss = getValidSessionsElseRemove(httpid, ISOPEN);
			wss.add(ws);
			wsid_httpid.put(ws.getId(), httpid);
			httpid_wss.put(httpid, wss);
		}
	}

	public void unlinkWs(String wsid) {
		if(wsid != null && wsid_httpid.containsKey(wsid)) {
			String httpid = wsid_httpid.remove(wsid);
			getValidSessionsElseRemove(httpid, new IsOpenAndDifferentPredicate(wsid));
		}
	}
	
	/**
	 * Return all sessions for httpid and for the predicate
	 * if some session negate the predicate, its are removed
	 * @param httpid
	 * @param predicat
	 * @return 
	 */
	Collection<Session> getValidSessionsElseRemove(String httpid, Predicate<Session> predicat) {
		Collection<Session> wss = new ArrayList();
		if(httpid_wss.containsKey(httpid)) {
			Collection<Session> befores = httpid_wss.get(httpid);
			for (Session before : befores) {
				if(predicat.test(before)) {
					wss.add(before);
					wsid_httpid.put(before.getId(), httpid);
				} else {
					wsid_httpid.remove(before.getId());
				}
			}
			if(wss.isEmpty()) {
				httpid_wss.remove(httpid);
			} else {
				httpid_wss.put(httpid, wss);
			}
		}
		return wss;
	}
	
	public Collection<Session> getUserSessions(String httpid) {
		return Collections.unmodifiableCollection(getValidSessionsElseRemove(httpid, ISOPEN));
	}
	
	
}
