/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.dashboard.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author hhfrancois
 */
@ApplicationScoped
public class MonitorSessionManager {
	
	private final Set<String> httpids = new HashSet<>();
	
	public void setMonitored(String httpid) {
		this.httpids.add(httpid);
	}
	
	public void unsetMonitored(String httpid) {
		if(isMonitored(httpid)) {
			this.httpids.remove(httpid);
		}
	}

	public boolean isMonitored(String httpid) {
		if(httpid == null) {
			return false;
		}
		return this.httpids.contains(httpid);
	}
	
	
	
}
