/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.context.OcelotContext;
import org.ocelotds.topic.TopicManager;
import org.ocelotds.core.UpdatedCacheManager;
import org.slf4j.Logger;
import org.ocelotds.objects.Options;

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
	private TopicManager topicManager;
	
	@Mock
	private OcelotContext ocelotContext;

	@InjectMocks
	@Spy
	private OcelotServices instance;

	/**
	 * Test of initCore method, of class.
	 */
	@Test
	public void test_getInitCore() {
		System.out.println("initCore");
		Options options = mock(Options.class);
		HttpSession httpSession = mock(HttpSession.class);
		instance.initCore(options, httpSession);
		verify(httpSession).setAttribute(eq(Constants.Options.OPTIONS), eq(options));
	}

	/**
	 * Test of getLocale method, of class OcelotServices.
	 */
	@Test
	public void testGetLocale() {
		System.out.println("getLocale");
		Locale l = Locale.FRANCE;
		Locale l2 = Locale.US;

		when(ocelotContext.getLocale()).thenReturn(l).thenReturn(l2);

		assertThat(instance.getLocale()).isEqualTo(l);
		assertThat(instance.getLocale()).isEqualTo(l2);
	}

	/**
	 * Test of setLocale method, of class OcelotServices.
	 */
	@Test
	public void testSetLocale() {
		System.out.println("setLocale");
		Locale l = Locale.FRANCE;
		Locale l2 = Locale.US;

		instance.setLocale(l);
		instance.setLocale(l2);

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

		assertThat(instance.getUsername()).isEqualTo(u1);
		assertThat(instance.getUsername()).isEqualTo(u2);
	}

	/**
	 * Test of getOutDatedCache method, of class OcelotServices.
	 */
	@Test
	public void testGetOutDatedCache() {
		System.out.println("getOutDatedCache");
		Map<String, Long> states = new HashMap<>();
		when(updatedCacheManager.getOutDatedCache(any(Map.class))).thenReturn(Collections.EMPTY_LIST);
		Collection<String> result = instance.getOutDatedCache(states);
		assertThat(result).isInstanceOf(Collection.class);
	}

	/**
	 * Test of subscribe method, of class OcelotServices.
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testSubscribe_String() throws IllegalAccessException {
		System.out.println("subscribe");
		instance.subscribe("TOPIC", null);
		verify(topicManager).registerTopicSession(eq("TOPIC"), any(Session.class));
	}

	/**
	 * Test of unsubscribe method, of class OcelotServices.
	 */
	@Test
	public void testUnsubscribe_String() {
		System.out.println("unsubscribe");
		instance.unsubscribe("TOPIC", null);
		verify(topicManager).unregisterTopicSession(eq("TOPIC"), any(Session.class));
	}

	/**
	 * Test of subscribe method, of class OcelotServices.
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testSubscribe_Session_String() throws IllegalAccessException {
		System.out.println("subscribe");
		when(topicManager.registerTopicSession(anyString(), any(Session.class))).thenReturn(1);
		Integer result = instance.subscribe("TOPIC", null);
		assertThat(result).isEqualTo(1);
	}

	/**
	 * Test of unsubscribe method, of class OcelotServices.
	 */
	@Test
	public void testUnsubscribe_Session_String() {
		System.out.println("unsubscribe");
		when(topicManager.unregisterTopicSession(anyString(), any(Session.class))).thenReturn(0);
		Integer result = instance.unsubscribe("TOPIC", null);
		assertThat(result).isEqualTo(0);
	}

	/**
	 * Test of getNumberSubscribers method, of class OcelotServices.
	 */
	@Test
	public void testGetNumberSubscribers() {
		System.out.println("getNumberSubscribers");
		when(topicManager.getNumberSubscribers(anyString())).thenReturn(0);
		Integer result = instance.getNumberSubscribers("TOPIC");
		assertThat(result).isEqualTo(0);
	}
}
