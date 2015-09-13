/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import org.ocelotds.context.ThreadLocalContextHolder;
import java.security.Principal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.websocket.Session;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.core.SessionManager;
import org.ocelotds.core.UpdatedCacheManager;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class OcelotServicesTest {
	
	private final Map<String, Object> userProperties = new HashMap<>();

	@Mock
	private Logger logger;
	
	@Mock
	private UpdatedCacheManager updatedCacheManager;
	
	@Mock
	private SessionManager sessionManager;

	@InjectMocks
	private OcelotServices ocelotServices;

	/**
	 * Test of getLocale method, of class OcelotServices.
	 */
	@Test
	public void testGetLocale() {
		System.out.println("getLocale");
		Locale l = new Locale("fr", "FR");
		ThreadLocalContextHolder.put(Constants.LOCALE, l);
		Locale result = ocelotServices.getLocale();
		assertThat(result).isEqualTo(l);
		l = new Locale("en", "US");
		ThreadLocalContextHolder.put(Constants.LOCALE, l);
		result = ocelotServices.getLocale();
		assertThat(result).isEqualTo(l);
	}

	/**
	 * Test of setLocale method, of class OcelotServices.
	 */
	@Test
	public void testSetLocale() {
		System.out.println("setLocale");
		Locale l = new Locale("fr", "FR");
		ocelotServices.setLocale(l);
		Locale result = ocelotServices.getLocale();
		assertThat(result).isEqualTo(l);
		l = new Locale("en", "US");
		ocelotServices.setLocale(l);
		result = ocelotServices.getLocale();
		assertThat(result).isEqualTo(l);
	}

	/**
	 * Test of getUsername method, of class OcelotServices.
	 */
	@Test
	public void testGetUsername() {
		System.out.println("getUsername");
		String result = ocelotServices.getUsername();
		assertThat(result).isNull();
		
		Session session = mock(Session.class);
		Principal p = mock(Principal.class);
		when(p.getName()).thenReturn("username");
		when(session.getUserPrincipal()).thenReturn(p);
		result = ocelotServices.getUsername(session);
		assertThat(result).isEqualTo("username");
		
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
	public void testSubscribe_String() {
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
		Integer result = ocelotServices.subscribe(mock(Session.class), "TOPIC");
		assertThat(result).isEqualTo(1);
	}

	/**
	 * Test of unsubscribe method, of class OcelotServices.
	 */
	@Test
	public void testUnsubscribe_Session_String() {
		System.out.println("unsubscribe");
		when(sessionManager.unregisterTopicSession(anyString(), any(Session.class))).thenReturn(0);
		Integer result = ocelotServices.unsubscribe(mock(Session.class), "TOPIC");
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
