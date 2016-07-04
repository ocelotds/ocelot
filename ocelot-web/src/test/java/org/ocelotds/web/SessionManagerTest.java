/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.web;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;
import javax.websocket.Session;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.web.ws.predicates.IsOpenAndDifferentPredicate;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionManagerTest {

	@InjectMocks
	@Spy
	SessionManager instance;
	
	@Mock
	Map<String, String> wsid_httpid;
	
	@Mock
	Map<String, Collection<Session>> httpid_wss;

	/**
	 * Test of linkWsToHttp method, of class SessionManager.
	 */
	@Test
	public void testLinkWsToHttpNull() {
		System.out.println("linkWsToHttp");
		Session ws = getMockWs();
		when(ws.getId()).thenReturn("WSID");
		String httpid = "HTTPID";
		Collection wss = mock(Collection.class);
		doReturn(wss).when(instance).getValidSessionsElseRemove(eq(httpid), any(Predicate.class));
		instance.linkWsToHttp(null, httpid);
		instance.linkWsToHttp(ws, null);
		instance.linkWsToHttp(null, null);
		verify(wss, never()).add(eq(ws));
		verify(wsid_httpid, never()).put(anyString(), anyString());
		verify(httpid_wss, never()).put(anyString(), eq(wss));
	}

	/**
	 * Test of linkWsToHttp method, of class SessionManager.
	 */
	@Test
	public void testLinkWsToHttp() {
		System.out.println("linkWsToHttp");
		Session ws = getMockWs();
		when(ws.getId()).thenReturn("WSID");
		String httpid = "HTTPID";
		Collection wss = mock(Collection.class);
		doReturn(wss).when(instance).getValidSessionsElseRemove(eq(httpid), any(Predicate.class));
		instance.linkWsToHttp(ws, httpid);
		verify(wss).add(eq(ws));
		verify(wsid_httpid).put(eq("WSID"), eq("HTTPID"));
		verify(httpid_wss).put(eq("HTTPID"), eq(wss));
	}

	/**
	 * Test of unlinkWs method, of class SessionManager.
	 */
	@Test
	public void testUnlinkWsNull() {
		System.out.println("unlinkWs");
		String wsid = "WSID";
		String httpid = "HTTPID";
		when(wsid_httpid.containsKey(eq("WSID"))).thenReturn(Boolean.FALSE);
		instance.unlinkWs(null);
		instance.unlinkWs(wsid);
		verify(wsid_httpid, never()).remove(eq(wsid));
		verify(instance, never()).getValidSessionsElseRemove(eq(httpid), any(Predicate.class));
	}

	/**
	 * Test of unlinkWs method, of class SessionManager.
	 */
	@Test
	public void testUnlinkWs() {
		System.out.println("unlinkWs");
		String wsid = "WSID";
		String httpid = "HTTPID";
		when(wsid_httpid.containsKey(eq("WSID"))).thenReturn(Boolean.TRUE);
		when(wsid_httpid.remove(eq(wsid))).thenReturn(httpid);
		instance.unlinkWs(wsid);
		verify(wsid_httpid).remove(eq(wsid));
		verify(instance).getValidSessionsElseRemove(eq(httpid), any(IsOpenAndDifferentPredicate.class));
	}

	/**
	 * Test of getValidSessionsElseRemove method, of class SessionManager.
	 */
	@Test
	public void getValidSessionsElseRemoveNoHttpid() {
		System.out.println("getValidSessionsElseRemove");
		String httpid = "HTTPID";
		Predicate predicate = mock(Predicate.class);
		when(httpid_wss.containsKey(eq(httpid))).thenReturn(Boolean.FALSE);
		Collection<Session> result = instance.getValidSessionsElseRemove(httpid, predicate);
		assertThat(result).isEmpty();
	}

	/**
	 * Test of getValidSessionsElseRemove method, of class SessionManager.
	 */
	@Test
	public void getValidSessionsElseRemoveResultEmpty() {
		System.out.println("getValidSessionsElseRemove");
		String httpid = "HTTPID";
		Predicate predicate = mock(Predicate.class);
		Collection<Session> befores = Collections.EMPTY_LIST;
		when(httpid_wss.containsKey(eq(httpid))).thenReturn(Boolean.TRUE);
		when(httpid_wss.get(httpid)).thenReturn(befores);
		Collection<Session> result = instance.getValidSessionsElseRemove(httpid, predicate);
		assertThat(result).isEmpty();
		verify(httpid_wss).remove(eq(httpid));
	}

	/**
	 * Test of getValidSessionsElseRemove method, of class SessionManager.
	 */
	@Test
	public void getValidSessionsElseRemove() {
		System.out.println("getValidSessionsElseRemove");
		String httpid = "HTTPID";
		Predicate predicate = mock(Predicate.class);
		Collection<Session> befores = Arrays.asList(getMockWs(), getMockWs(), getMockWs());
		when(httpid_wss.containsKey(eq(httpid))).thenReturn(Boolean.TRUE);
		when(httpid_wss.get(httpid)).thenReturn(befores);
		when(predicate.test(any(Session.class))).thenReturn(Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
		Collection<Session> result = instance.getValidSessionsElseRemove(httpid, predicate);
		assertThat(result).hasSize(1);
		verify(httpid_wss).put(eq(httpid), anyList());
	}

	/**
	 * Test of getUserSessions method, of class SessionManager.
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void testGetUserSessions() {
		System.out.println("getUserSessions");
		String httpid = "HTTPID";
		Collection wss = mock(Collection.class);
		doReturn(wss).when(instance).getValidSessionsElseRemove(eq(httpid), any(Predicate.class));
		Collection<Session> result = instance.getUserSessions(httpid);
		result.add(getMockWs());
	}
	
	Session getMockWs() {
		Session session = mock(Session.class);
		when(session.getId()).thenReturn("WSID");
		return session;
	}

}