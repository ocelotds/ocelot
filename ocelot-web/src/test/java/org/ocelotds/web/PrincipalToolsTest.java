/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.web;

import java.security.Principal;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class PrincipalToolsTest {

	@InjectMocks
	@Spy
	PrincipalTools instance;

	/**
	 * Test of getPrincipal method, of class PrincipalTools.
	 */
	@Test
	public void testGetPrincipal_HandshakeRequest() {
		System.out.println("getPrincipal");
		HandshakeRequest handshakeRequest = mock(HandshakeRequest.class);
		Principal principal = mock(Principal.class);
		when(principal.getName()).thenReturn("FOO");
		when(handshakeRequest.getUserPrincipal()).thenReturn(null).thenReturn(principal);
		Principal result = instance.getPrincipal(handshakeRequest);
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(Constants.ANONYMOUS);

		result = instance.getPrincipal(handshakeRequest);
		assertThat(result).isEqualTo(principal);
		assertThat(result.getName()).isEqualTo("FOO");
		
		handshakeRequest = null;
		result = instance.getPrincipal(handshakeRequest);
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(Constants.ANONYMOUS);
	}

	/**
	 * Test of getPrincipal method, of class PrincipalTools.
	 */
	@Test
	public void testGetPrincipal_Session() {
		System.out.println("getPrincipal");
		Session session = mock(Session.class);
		Principal principal = mock(Principal.class);
		when(principal.getName()).thenReturn("FOO");
		when(session.getUserPrincipal()).thenReturn(null).thenReturn(principal);
		Principal result = instance.getPrincipal(session);
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(Constants.ANONYMOUS);

		result = instance.getPrincipal(session);
		assertThat(result).isEqualTo(principal);
		assertThat(result.getName()).isEqualTo("FOO");
		
		session = null;
		result = instance.getPrincipal(session);
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(Constants.ANONYMOUS);
	}
}