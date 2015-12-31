/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.context;

import java.security.Principal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class OcelotContextTest {

	@Mock
	private Logger logger;
	
	@InjectMocks
	@Spy
	private OcelotContext instance;
	
	@Mock
	private Session session;

	@Mock
	private HandshakeRequest handshakeRequest;

	/**
	 * Test of getLocale method, of class OcelotContext.
	 */
	@Test
	public void testGetLocale() {
		System.out.println("getLocale");
		Map<String, Object> map = new HashMap<>();

		when(session.getUserProperties()).thenReturn(map);
		when(instance.getSession()).thenReturn(session).thenReturn(session).thenReturn(null);

		Locale result = instance.getLocale();
		assertThat(result).isEqualTo(Locale.US);

		map.put(Constants.LOCALE, Locale.FRANCE);
		result = instance.getLocale();
		assertThat(result).isEqualTo(Locale.FRANCE);

		result = instance.getLocale();
		assertThat(result).isEqualTo(Locale.US);
	}

	/**
	 * Test of setLocale method, of class OcelotContext.
	 */
	@Test
	public void testSetLocale() {
		Map<String, Object> map = new HashMap<>();

		when(session.getUserProperties()).thenReturn(map);
		when(instance.getSession()).thenReturn(session).thenReturn(session).thenReturn(null);
		
		instance.setLocale(Locale.ITALY);
		assertThat(map.get(Constants.LOCALE)).isEqualTo(Locale.ITALY);
		
		instance.setLocale(Locale.FRANCE);
		assertThat(map.get(Constants.LOCALE)).isEqualTo(Locale.FRANCE);

		instance.setLocale(Locale.CHINA);
		assertThat(map.get(Constants.LOCALE)).isEqualTo(Locale.FRANCE);
	}
	/**
	 * Test of getPrincipal method, of class OcelotContext.
	 */
	@Test
	public void testGetPrincipal() {
		System.out.println("getPrincipal");
		Principal p = mock(Principal.class);
		String expResult = "username";

		when(p.getName()).thenReturn(expResult);
		when(session.getUserPrincipal()).thenReturn(null).thenReturn(p);
		when(instance.getSession()).thenReturn(session);

		String result = instance.getPrincipal().getName();
		assertThat(result).isEqualTo(Constants.ANONYMOUS);

		result = instance.getPrincipal().getName();
		assertThat(result).isEqualTo(expResult);
	}

	@Test
	public void testIsUserInRole() {
		when(instance.getHandshakeRequest()).thenReturn(null).thenReturn(handshakeRequest);
		when(handshakeRequest.isUserInRole("OK")).thenReturn(Boolean.TRUE);
		when(handshakeRequest.isUserInRole("NOK")).thenReturn(Boolean.FALSE);
		boolean result = instance.isUserInRole("OK");
		assertThat(result).isEqualTo(Boolean.FALSE);
		result = instance.isUserInRole("OK");
		assertThat(result).isEqualTo(Boolean.TRUE);
		result = instance.isUserInRole("NOK");
		assertThat(result).isEqualTo(Boolean.FALSE);
	}
}