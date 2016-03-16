/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ocelotds.objects;

import java.security.Principal;
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

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class WsUserContextTest {

	@Mock
	Principal principal;
	

	/**
	 * Test of getPrincipal method, of class WsUserContext.
	 */
	@Test
	public void testGetPrincipal() {
		System.out.println("getPrincipal");
		HandshakeRequest handshakeRequest = mock(HandshakeRequest.class);
		when(handshakeRequest.getUserPrincipal()).thenReturn(principal);
		WsUserContext instance = new WsUserContext(handshakeRequest);
		Principal result = instance.getPrincipal();
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(principal);
	}

	/**
	 * Test of getPrincipal method, of class WsUserContext.
	 */
	@Test
	public void testGetAnonymous() {
		System.out.println("getPrincipal");
		WsUserContext instance = new WsUserContext(null);
		Principal result = instance.getPrincipal();
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("ANONYMOUS");
	}

	/**
	 * Test of isUserInRole method, of class WsUserContext.
	 */
	@Test
	public void testIsUserInRole() {
		System.out.println("isUserInRole");
		HandshakeRequest handshakeRequest = mock(HandshakeRequest.class);
		when(handshakeRequest.isUserInRole(eq("OK"))).thenReturn(true);
		when(handshakeRequest.isUserInRole(eq("NOK"))).thenReturn(false);
		WsUserContext instance = new WsUserContext(handshakeRequest);
		boolean result = instance.isUserInRole("OK");
		assertThat(result).isTrue();
		result = instance.isUserInRole("NOK");
		assertThat(result).isFalse();
	}

	/**
	 * Test of isUserInRole method, of class WsUserContext.
	 */
	@Test
	public void testIsUserInRoleNoHandshakeRequest() {
		System.out.println("isUserInRole");
		WsUserContext instance = new WsUserContext(null);
		boolean result = instance.isUserInRole("OK");
		assertThat(result).isFalse();
		result = instance.isUserInRole("NOK");
		assertThat(result).isFalse();
	}
}