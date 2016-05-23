/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.topic;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import javax.websocket.Session;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionManagerImplTest {

	@InjectMocks
	@Spy
	SessionManagerImpl instance;

	/**
	 * Test of getMap method, of class SessionManager.
	 */
	@Test
	public void testGetMap() {
		System.out.println("getMap");
		Map<String, Session> result = instance.getMap();
		assertThat(result).isNotNull();
	}

	/**
	 * Test of addSession method, of class SessionManager.
	 */
	@Test
	public void testAddSession() {
		System.out.println("addSession");
		instance.getMap().clear();
		String id = "ID0";
		Session session = mock(Session.class);
		instance.addSession(id, session);
		assertThat(instance.getMap()).isNotEmpty();
		instance.getMap().clear();
	}
	
	/**
	 * Test of closeOldSessionForHttp method, of class.
	 */
	@Test
	public void closeOldSessionForHttpTest() {
		System.out.println("closeOldSessionForHttp");
		instance.getMap().clear();
		Session session = mock(Session.class);
		String id = "ID0";
		instance.getMap().put(id, session);
		Session closed = instance.closeOldSessionForHttp(id);
		try {
			verify(session).close();
		} catch (IOException ex) {
		}
		assertThat(closed).isNotNull();
	}

	/**
	 * Test of closeOldSessionForHttp method, of class.
	 */
	@Test
	public void closeOldSessionForHttpTestFailed() throws IOException {
		System.out.println("closeOldSessionForHttp");
		instance.getMap().clear();
		Session session = mock(Session.class);
		doThrow(IOException.class).when(session).close();
		String id = "ID0";
		instance.getMap().put(id, session);
		Session closed = instance.closeOldSessionForHttp(id);
		verify(session).close();
		assertThat(closed).isNotNull();
	}

	/**
	 * Test of removeSession method, of class SessionManager.
	 */
	@Test
	public void testRemoveSession_Session() {
		System.out.println("removeSession");
		instance.getMap().clear();
		Session session = mock(Session.class);
		instance.getMap().put("ID0", session);
		instance.getMap().put("ID1", session);

		Collection<String> removeds = instance.removeSession(session);

		verify(instance, times(2)).removeSession(any(String.class));
		
		assertThat(removeds).contains("ID0", "ID1");
	}

	/**
	 * Test of removeSession method, of class SessionManager.
	 */
	@Test
	public void testRemoveSession_String() {
		System.out.println("removeSession");
		instance.getMap().clear();
		String id = "ID0";
		Session session = mock(Session.class);
		instance.addSession(id, session);
		assertThat(instance.getMap()).isNotEmpty();
		instance.removeSession(id);
		assertThat(instance.getMap()).isEmpty();
	}

	/**
	 * Test of getSessionById method, of class SessionManager.
	 */
	@Test
	public void testGetSessionById() {
		System.out.println("getSessionById");
		instance.getMap().clear();
		String id = "ID0";
		Session session = mock(Session.class);
		instance.addSession(id, session);
		assertThat(instance.getMap()).isNotEmpty();

		Session result = instance.getSessionById(id);
		assertThat(result).isEqualTo(session);
	}
	
	/**
	 * Test of getUsername method, of class.
	 */
	@Test
	public void getUsernameTest() {
		System.out.println("getUsername");
		Session session = mock(Session.class);
		Principal p = mock(Principal.class);
		when(session.getUserPrincipal()).thenReturn(null).thenReturn(p);
		when(p.getName()).thenReturn("USERNAME");
		
		String result = instance.getUsername(null);
		assertThat(result).isEqualTo(Constants.ANONYMOUS);
		result = instance.getUsername(session);
		assertThat(result).isEqualTo(Constants.ANONYMOUS);
		result = instance.getUsername(session);
		assertThat(result).isEqualTo("USERNAME");

	}

}