/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import java.util.HashMap;
import java.util.Map;
import javax.websocket.Session;
import org.junit.Test;
import org.mockito.Mock;
import org.ocelotds.i18n.Locale;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.core.SessionManager;
import org.ocelotds.core.UpdatedCacheManager;
import org.ocelotds.i18n.ThreadLocalContextHolder;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class OcelotServicesTest {
	
	private final Map<String, Object> userProperties = new HashMap<>();
	
	@Mock
	private UpdatedCacheManager updatedCacheManager;
	
	@Mock
	private SessionManager sessionManager;

	@InjectMocks
	private final OcelotServices ocelotServices = new OcelotServices();

	/**
	 * Test of setLocale method, of class OcelotServices.
	 */
	@Test
	public void testSetLocale_Locale() {
		System.out.println("setLocale");
		ocelotServices.setLocale(null);
	}

	private Session setLocale(Locale locale) {
		Session session = mock(Session.class);
		when(session.getUserProperties()).thenReturn(userProperties);
		ocelotServices.setLocale(locale, session);
		return session;
	}
	
	/**
	 * Test of setLocale method, of class OcelotServices.
	 */
	@Test
	public void testSetLocale_Locale_Session() {
		System.out.println("setLocale");
		Locale l = new Locale();
		l.setCountry("FR");
		l.setLanguage("fr");
		setLocale(l);
		java.util.Locale result = (java.util.Locale) userProperties.get(Constants.LOCALE);
		assertThat(result.getLanguage()).isEqualTo(l.getLanguage());
		assertThat(result.getCountry()).isEqualTo(l.getCountry());
		result = (java.util.Locale) ThreadLocalContextHolder.get(Constants.LOCALE);
		assertThat(result.getLanguage()).isEqualTo(l.getLanguage());
		assertThat(result.getCountry()).isEqualTo(l.getCountry());
	}

	/**
	 * Test of getLocale method, of class OcelotServices.
	 */
	@Test
	public void testGetLocale_0args() {
		System.out.println("getLocale");
		Locale result = ocelotServices.getLocale();
		assertThat(result).isNull();
	}

	/**
	 * Test of getLocale method, of class OcelotServices.
	 */
	@Test
	public void testGetLocale_Session() {
		System.out.println("getLocale");
		Locale l = new Locale();
		l.setLanguage("en");
		l.setCountry("US");
		
		Session session = setLocale(l);

		Locale result = ocelotServices.getLocale(session);
		assertThat(result.getLanguage()).isEqualTo(l.getLanguage());
		assertThat(result.getCountry()).isEqualTo(l.getCountry());
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
