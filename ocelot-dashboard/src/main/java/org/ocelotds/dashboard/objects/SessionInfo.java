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
	boolean open = false;
	
	public SessionInfo() {
	}
	
	public SessionInfo(String id, String username, boolean open) {
		this.id = id;
		this.username = username;
		this.open = open;
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

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}
}
