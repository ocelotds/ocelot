/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.objects;

import java.security.Principal;
import javax.websocket.server.HandshakeRequest;
import org.ocelotds.security.UserContext;
import org.ocelotds.web.PrincipalTools;

/**
 *
 * @author hhfrancois
 */
public class WsUserContext implements UserContext {

	Principal principal = null;
	HandshakeRequest handshakeRequest;
	PrincipalTools principalTools;

	public WsUserContext(HandshakeRequest handshakeRequest) {
		this.handshakeRequest = handshakeRequest;
		this.principalTools = new PrincipalTools();
	}

	@Override
	public Principal getPrincipal() {
		if (null == principal) {
			principal = principalTools.getPrincipal(handshakeRequest);
		}
		return principal;
	}

	@Override
	public boolean isUserInRole(String role) {
		if (null != handshakeRequest) {
			return handshakeRequest.isUserInRole(role);
		}
		return false;
	}
}
