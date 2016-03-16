/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.web;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.security.UserContext;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class RequestManagerTest {
	
	@InjectMocks
	@Spy
	private RequestManager instance;
	
	@Mock
	private Map<HandshakeRequest, Session> sessionsByRequest;

	
	
	/**
	 * Test of addSession method, of class RequestManager.
	 */
	@Test
	public void testGetSessionsByRequest() {
		Map<HandshakeRequest, Session> result = instance.getSessionByRequest();
		assertThat(result).isInstanceOf(Map.class);
	}
	/**
	 * Test of addSession method, of class RequestManager.
	 */
	@Test
	public void testAddSessionOk() {
		System.out.println("addSession");
		doReturn(sessionsByRequest).when(instance).getSessionByRequest();

		HandshakeRequest request = mock(HandshakeRequest.class);
		Session session = mock(Session.class);
		instance.addSession(request, session);
		verify(sessionsByRequest).put(any(HandshakeRequest.class), any(Session.class));
	}

	/**
	 * Test of addSession method, of class RequestManager.
	 */
	@Test
	public void testAddSessionNOk() {
		System.out.println("addSession");
		doReturn(sessionsByRequest).when(instance).getSessionByRequest();

		HandshakeRequest request = mock(HandshakeRequest.class);
		Session session = mock(Session.class);
		instance.addSession(request, session);
		instance.addSession(null, session);
		instance.addSession(null, null);

		verify(instance, times(2)).removeSession(any(Session.class));
		
	}

	/**
	 * Test of removeSession method, of class RequestManager.
	 */
	@Test
	public void testRemoveSession() {
		System.out.println("removeSession");
		Map<HandshakeRequest, Session> map = new HashMap<>();
		doReturn(map).when(instance).getSessionByRequest();
		Session session1 = mock(Session.class);
		Session session2 = mock(Session.class);
		Session session3 = mock(Session.class);
		HandshakeRequest request1 = mock(HandshakeRequest.class);
		HandshakeRequest request2 = mock(HandshakeRequest.class);
		HandshakeRequest request3 = mock(HandshakeRequest.class);
		map.put(request1, session1);
		map.put(request2, session2);
		map.put(request3, session3);
		instance.removeSession(session1);
		assertThat(map).hasSize(2);
		instance.removeSession(session1);
		assertThat(map).hasSize(2);
		instance.removeSession(session2);
		assertThat(map).hasSize(1);
		instance.removeSession(session3);
		assertThat(map).isEmpty();
	}

	/**
	 * Test of getSessionByHttpSession method, of class RequestManager.
	 */
	@Test
	public void testGetSessionByHttpSessionOpen() {
		System.out.println("getSessionByHttpSession");
		Map<HandshakeRequest, Session> map = new HashMap<>();
		doReturn(map).when(instance).getSessionByRequest();

		Session session = mock(Session.class);
		when(session.isOpen()).thenReturn(Boolean.TRUE);
		HandshakeRequest request = mock(HandshakeRequest.class);
		map.put(request, session);
		HttpSession httpSession = mock(HttpSession.class);
		when(request.getHttpSession()).thenReturn(httpSession);

		Session result = instance.getSessionByHttpSession(httpSession);
		assertThat(result).isEqualTo(session);
	}
	
	/**
	 * Test of getSessionByHttpSession method, of class RequestManager.
	 */
	@Test
	public void testGetSessionByHttpSessionClose() {
		System.out.println("getSessionByHttpSession");
		Map<HandshakeRequest, Session> map = new HashMap<>();
		doReturn(map).when(instance).getSessionByRequest();

		Session session = mock(Session.class);
		when(session.isOpen()).thenReturn(Boolean.FALSE);
		HandshakeRequest request = mock(HandshakeRequest.class);
		map.put(request, session);
		HttpSession httpSession = mock(HttpSession.class);
		when(request.getHttpSession()).thenReturn(httpSession);

		Session result = instance.getSessionByHttpSession(httpSession);
		assertThat(result).isNull();
	}

	/**
	 * Test of getSessionByHttpSession method, of class RequestManager.
	 */
	@Test
	public void testGetSessionByHttpSessionNone() {
		System.out.println("getSessionByHttpSession");
		Map<HandshakeRequest, Session> map = new HashMap<>();
		doReturn(map).when(instance).getSessionByRequest();

		HttpSession httpSession = mock(HttpSession.class);

		Session result = instance.getSessionByHttpSession(httpSession);
		assertThat(result).isNull();
	}
	
	@Test
	public void testGetHandshakeRequest() {
		System.out.println("getHandshakeRequest");
		Session session = mock(Session.class);
		HandshakeRequest handshakeRequest = mock(HandshakeRequest.class);
		Map map = mock(Map.class);
		doReturn(map).when(instance).getRequestBySessionId();
		when(map.get(anyString())).thenReturn(handshakeRequest);
		HandshakeRequest result = instance.getHandshakeRequest(session);
		assertThat(result).isEqualTo(handshakeRequest);

		result = instance.getHandshakeRequest(null);
		assertThat(result).isNull();
	}
	/**
	 * Test of getUserContext method, of class.
	 */
	@Test
	public void getUserContextTest() {
		System.out.println("getUserContext");
		Session session = mock(Session.class);
		doReturn(null).when(instance).getHandshakeRequest(any(Session.class));
		
		UserContext result = instance.getUserContext(session);
		assertThat(result).isNotNull();
	}
}
