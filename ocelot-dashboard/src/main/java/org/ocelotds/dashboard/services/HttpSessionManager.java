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

	public void addHttpSession(HttpSession httpSession) {
		if(httpSession != null && !httpidSessioninfos.containsKey(httpSession.getId())) {
			SessionInfo sessionInfo = new SessionInfo(httpSession.getId(), Constants.ANONYMOUS, true);
			httpidSessioninfos.put(httpSession.getId(), sessionInfo);
			addSessionInfo.fire(sessionInfo);
		}
	}
	
	public void removeHttpSession(HttpSession httpSession) {
		SessionInfo sessionInfo = httpidSessioninfos.remove(httpSession.getId());
		removeSessionInfo.fire(sessionInfo);
	}
	
	public Collection<SessionInfo> getSessionInfos() {
		return httpidSessioninfos.values();
	}
	
	public void addSession(Session session, String httpid) {
		if(session != null) {
			SessionInfo sessionInfo = httpidSessioninfos.get(httpid);
			sessionInfo.setOpen(true);
			sessionInfo.setUsername(principalTools.getPrincipal(session).getName());
			updateSessionInfo.fire(sessionInfo);
			wsSessionHttpid.put(session.getId(), httpid);
		}
	}

	public void removeSession(Session session) {
		if(session != null && wsSessionHttpid.containsKey(session.getId())) {
			String httpid = wsSessionHttpid.remove(session.getId());
			if(!wsSessionHttpid.containsValue(httpid)) {
				SessionInfo sessionInfo = httpidSessioninfos.get(httpid);
				sessionInfo.setOpen(false);
				updateSessionInfo.fire(sessionInfo);
			}
		}
	}
}
