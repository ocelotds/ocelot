/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.dashboard.services;

import java.util.Objects;
import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author hhfrancois
 */
@ApplicationScoped
public class MonitorSessionManager {
	
	private String httpid = null;
	
	public void setMonitored(String httpid) {
		this.httpid = httpid;
	}
	
	public void unsetMonitored(String httpid) {
		if(isMonitored(httpid)) {
			this.httpid = null;
		}
	}

	public boolean isMonitored(String httpid) {
		if(httpid == null) {
			return false;
		}
		return httpid.equals(this.httpid);
	}
	
	
	
}
