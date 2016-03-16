/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.objects;

import java.security.Principal;
import javax.websocket.server.HandshakeRequest;
import org.ocelotds.Constants;
import org.ocelotds.security.UserContext;

/**
 *
 * @author hhfrancois
 */
public class WsUserContext implements UserContext {

	private final Principal ANONYMOUS = new Principal() {
		@Override
		public String getName() {
			return Constants.ANONYMOUS;
		}
	};

	Principal principal;
	HandshakeRequest handshakeRequest;

	public WsUserContext(HandshakeRequest handshakeRequest) {
		this.handshakeRequest = handshakeRequest;
		if (null != handshakeRequest) {
			this.principal = handshakeRequest.getUserPrincipal();
		} else {
			this.principal = ANONYMOUS;
		}
	}

	@Override
	public Principal getPrincipal() {
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
