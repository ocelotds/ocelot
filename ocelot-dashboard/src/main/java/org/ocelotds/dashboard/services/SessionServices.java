/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.dashboard.services;

import java.security.Principal;
import org.ocelotds.dashboard.objects.SessionInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.inject.Inject;
import javax.websocket.Session;
import org.ocelotds.Constants;
import org.ocelotds.annotations.DashboardOnDebug;
import org.ocelotds.annotations.DataService;
import org.ocelotds.topic.SessionManager;

/**
 *
 * @author hhfrancois
 */
@DataService
@DashboardOnDebug
public class SessionServices {
	
	@Inject
	SessionManager sessionManager;
	
	@Inject
	MonitorSessionManager monitorSessionManager;
	
	
	/**
	 * Get sessions information
	 * @return 
	 */
	public Collection<SessionInfo> getSessionInfos() {
		Map<String, Session> map = sessionManager.getMap();
		Collection<SessionInfo> result = new ArrayList<>();
		for (Map.Entry<String, Session> entry : map.entrySet()) {
			Session session = entry.getValue();
			String username = sessionManager.getUsername(session);
			result.add(new SessionInfo(entry.getKey(), username, session.isOpen()));
		}
		return result;
	}
	
	public void monitorSession(String httpid) {
		monitorSessionManager.setMonitored(httpid);
	}
	
	public void unmonitorSession(String httpid) {
		monitorSessionManager.unsetMonitored(httpid);
	}
}
