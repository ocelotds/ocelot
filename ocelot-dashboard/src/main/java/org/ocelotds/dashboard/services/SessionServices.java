/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.dashboard.services;

import org.ocelotds.dashboard.objects.SessionInfo;
import java.util.Collection;
import javax.inject.Inject;
import org.ocelotds.annotations.DashboardOnDebug;
import org.ocelotds.annotations.DataService;
import org.ocelotds.dashboard.security.DashboardSecureProvider;
import org.ocelotds.security.OcelotSecured;

/**
 *
 * @author hhfrancois
 */
@DataService
@DashboardOnDebug
@OcelotSecured(provider = DashboardSecureProvider.class)
public class SessionServices {
	
	@Inject
	HttpSessionManager httpSessionManager;
	
	@Inject
	MonitorSessionManager monitorSessionManager;
	
	
	/**
	 * Get sessions information
	 * @return 
	 */
	public Collection<SessionInfo> getSessionInfos() {
		return httpSessionManager.getSessionInfos();
	}
	
	public void monitorSession(String httpid) {
		monitorSessionManager.setMonitored(httpid);
	}
	
	public void unmonitorSession(String httpid) {
		monitorSessionManager.unsetMonitored(httpid);
	}
}
