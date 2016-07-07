/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.dashboard.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;
import org.ocelotds.Constants;
import org.ocelotds.annotations.JsTopicEvent;
import org.ocelotds.dashboard.objects.SessionInfo;
import org.ocelotds.web.PrincipalTools;

/**
 *
 * @author hhfrancois
 */
@ApplicationScoped
public class HttpSessionManager {
	Map<String, SessionInfo> httpidSessioninfos = new HashMap<>();
	Map<String, String> wsSessionHttpid = new HashMap<>();
	
	@Inject
	@JsTopicEvent("sessioninfo-add")
	Event<SessionInfo> addSessionInfo;

	@Inject
	@JsTopicEvent("sessioninfo-remove")
	Event<SessionInfo> removeSessionInfo;

	@Inject
	@JsTopicEvent("sessioninfo-update")
	Event<SessionInfo> updateSessionInfo;
	
	@Inject
	PrincipalTools principalTools;

	public Collection<SessionInfo> getSessionInfos() {
		return httpidSessioninfos.values();
	}
	
	public void addSession(Session session, String httpid) {
		if(session != null) {
			Event<SessionInfo> topic = updateSessionInfo;
			SessionInfo sessionInfo = httpidSessioninfos.get(httpid);
			if(sessionInfo==null) {
				sessionInfo = new SessionInfo(httpid, Constants.ANONYMOUS);
				topic = addSessionInfo;
			}
			sessionInfo.incWsNumber();
			sessionInfo.setUsername(principalTools.getPrincipal(session).getName());
			wsSessionHttpid.put(session.getId(), httpid);
			httpidSessioninfos.put(httpid, sessionInfo);
			topic.fire(sessionInfo);
		}
	}

	public void removeSession(Session session) {
		if(session != null && wsSessionHttpid.containsKey(session.getId())) {
			String httpid = wsSessionHttpid.remove(session.getId());
			SessionInfo sessionInfo = httpidSessioninfos.get(httpid);
			sessionInfo.decWsNumber();
			if(sessionInfo.getWsNumber()>0) {
				updateSessionInfo.fire(sessionInfo);
			} else {
				httpidSessioninfos.remove(httpid);
				removeSessionInfo.fire(sessionInfo);
			}
		}
	}
}
