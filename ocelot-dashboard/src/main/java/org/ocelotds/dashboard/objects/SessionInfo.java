/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.dashboard.objects;

/**
 *
 * @author hhfrancois
 */
public class SessionInfo {
	
	String id = null;
	String username = null;
	int wsNumber = 0;

	public SessionInfo() {
	}
	
	public SessionInfo(String id, String username) {
		this.id = id;
		this.username = username;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getWsNumber() {
		return wsNumber;
	}

	public void setWsNumber(int ws) {
		this.wsNumber = ws;
	}
	
	public void incWsNumber() {
		wsNumber++;
	}
	public void decWsNumber() {
		wsNumber--;
	}
	
}
