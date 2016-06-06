/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import java.security.Principal;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import org.ocelotds.Constants;

/**
 *
 * @author hhfrancois
 */
public class PrincipalTools {
	
	private final Principal ANONYMOUS = new Principal() {
		@Override
		public String getName() {
			return Constants.ANONYMOUS;
		}
	};

	public Principal getPrincipal(HandshakeRequest handshakeRequest) {
		if (null != handshakeRequest && handshakeRequest.getUserPrincipal() != null) {
			return handshakeRequest.getUserPrincipal();
		} else {
			return ANONYMOUS;
		}
	}

	public Principal getPrincipal(Session session) {
		if (null != session && session.getUserPrincipal() != null) {
			return session.getUserPrincipal();
		} else {
			return ANONYMOUS;
		}
	}

}
