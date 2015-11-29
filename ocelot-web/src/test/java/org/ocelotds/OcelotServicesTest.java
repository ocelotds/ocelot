/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import org.ocelotds.context.ThreadLocalContextHolder;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.websocket.Session;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.context.OcelotContext;
import org.ocelotds.core.SessionManager;
import org.ocelotds.core.UpdatedCacheManager;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class OcelotServicesTest {
	
	@Mock
	private Logger logger;
	
	@Mock
	private UpdatedCacheManager updatedCacheManager;
	
	@Mock
	private SessionManager sessionManager;

	@Mock
	private Session session;

	@Mock
	private OcelotContext ocelotContext;

	@InjectMocks
	private OcelotServices ocelotServices;

	/**
	 * Test of getLocale method, of class OcelotServices.
	 */
	@Test
	public void testGetLocale() {
		System.out.println("getLocale");
		Locale l = new Locale("fr", "FR");
		Locale l2 = new Locale("en", "US");

		when(ocelotContext.getLocale()).thenReturn(l).thenReturn(l2);

		assertThat(ocelotServices.getLocale()).isEqualTo(l);
		assertThat(ocelotServices.getLocale()).isEqualTo(l2);
	}

	/**
	 * Test of setLocale method, of class OcelotServices.
	 */
	@Test
	public void testSetLocale() {
		System.out.println("setLocale");
		Locale l = new Locale("fr", "FR");
		Locale l2 = new Locale("en", "US");

		ocelotServices.setLocale(l);
		ocelotServices.setLocale(l2);

		ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
		verify(ocelotContext, times(2)).setLocale(localeCaptor.capture());
		List<Locale> allValues = localeCaptor.getAllValues();
		assertThat(allValues.get(0)).isEqualTo(l);
		assertThat(allValues.get(1)).isEqualTo(l2);
	}

	/**
	 * Test of getUsername method, of class OcelotServices.
	 */
	@Test
	public void testGetUsername() {
		System.out.println("getUsername");
		Principal p1 = mock(Principal.class);
		Principal p2 = mock(Principal.class);
		String u1 = Constants.ANONYMOUS;
		String u2 = "username";

		when(p1.getName()).thenReturn(u1);
		when(p2.getName()).thenReturn(u2);
		when(ocelotContext.getPrincipal()).thenReturn(p1).thenReturn(p2);

		assertThat(ocelotServices.getUsername()).isEqualTo(u1);
		assertThat(ocelotServices.getUsername()).isEqualTo(u2);
	}

	/**
	 * Test of getOutDatedCache method, of class OcelotServices.
	 */
	@Test
	public void testGetOutDatedCache() {
		System.out.println("getOutDatedCache");
		Map<String, Long> states = new HashMap<>();
		ocelotServices.getOutDatedCache(states);
	}

	/**
	 * Test of subscribe method, of class OcelotServices.
	 */
	@Test
	public void testSubscribe_String() throws IllegalAccessException {
		System.out.println("subscribe");
		ocelotServices.subscribe("TOPIC");
	}

	/**
	 * Test of unsubscribe method, of class OcelotServices.
	 */
	@Test
	public void testUnsubscribe_String() {
		System.out.println("unsubscribe");
		ocelotServices.unsubscribe("TOPIC");
	}

	/**
	 * Test of subscribe method, of class OcelotServices.
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testSubscribe_Session_String() throws IllegalAccessException {
		System.out.println("subscribe");
		when(sessionManager.registerTopicSession(anyString(), any(Session.class))).thenReturn(1);
		Integer result = ocelotServices.subscribe("TOPIC");
		assertThat(result).isEqualTo(1);
	}

	/**
	 * Test of unsubscribe method, of class OcelotServices.
	 */
	@Test
	public void testUnsubscribe_Session_String() {
		System.out.println("unsubscribe");
		when(sessionManager.unregisterTopicSession(anyString(), any(Session.class))).thenReturn(0);
		Integer result = ocelotServices.unsubscribe("TOPIC");
		assertThat(result).isEqualTo(0);
	}

	/**
	 * Test of getNumberSubscribers method, of class OcelotServices.
	 */
	@Test
	public void testGetNumberSubscribers() {
		System.out.println("getNumberSubscribers");
		when(sessionManager.getNumberSubscribers(anyString())).thenReturn(0);
		Integer result = ocelotServices.getNumberSubscribers("TOPIC");
		assertThat(result).isEqualTo(0);
	}
	
}
