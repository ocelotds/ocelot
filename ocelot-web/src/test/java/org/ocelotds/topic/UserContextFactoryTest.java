/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ocelotds.topic;

import java.security.Principal;
import java.util.Map;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.security.UserContext;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class UserContextFactoryTest {

	@InjectMocks
	@Spy
	UserContextFactory instance;

	/**
	 * Test of getMap method, of class UserContextFactory.
	 */
	@Test
	public void testGetMap() {
		System.out.println("getMap");
		Map<String, UserContext> result = instance.getMap();
		assertThat(result).isNotNull();
	}

	/**
	 * Test of addSession method, of class UserContextFactory.
	 */
	@Test
	public void test_createUserContext() {
		System.out.println("createUserContext");
		HandshakeRequest request = mock(HandshakeRequest.class);
		instance.createUserContext(request, "ID");
		assertThat(instance.getMap()).containsKeys("ID");
		instance.getMap().remove("ID");
	}

	@Test
	public void test_createUserContextRequestNull() {
		System.out.println("createUserContext");
		Session session = mock(Session.class);
		when(session.getId()).thenReturn("ID");
		instance.createUserContext(null, "ID");
		assertThat(instance.getMap()).isEmpty();
		verify(instance).destroyUserContext(anyString());
	}

	@Test
	public void test_createUserContextNull() {
		System.out.println("createUserContext");
		HandshakeRequest request = mock(HandshakeRequest.class);
		instance.createUserContext(request, null);
		assertThat(instance.getMap()).isEmpty();
		verify(instance, never()).destroyUserContext(anyString());
	}

	@Test
	public void test_createUserContextNullRequestNull() {
		System.out.println("createUserContext");
		instance.createUserContext(null, null);
		assertThat(instance.getMap()).isEmpty();
		verify(instance).destroyUserContext(anyString());
	}

	/**
	 * Test of removeSession method, of class UserContextFactory.
	 */
	@Test
	public void test_destroyUserContext() {
		System.out.println("removeSession");
		instance.getMap().clear();
		instance.getMap().put("ID1", mock(UserContext.class));
		instance.getMap().put("ID2", mock(UserContext.class));
		instance.getMap().put("ID3", mock(UserContext.class));
		instance.destroyUserContext("ID2");
		assertThat(instance.getMap()).hasSize(2);
		instance.destroyUserContext("ID4");
		assertThat(instance.getMap()).hasSize(2);
	}

	/**
	 * Test of getUserContext method, of class UserContextFactory.
	 */
	@Test
	public void testGetUserContext() {
		System.out.println("getUserContext");
		UserContext uc = mock(UserContext.class);
		instance.getMap().put("ID1", uc);
		Principal principal = mock(Principal.class);
		when(uc.getPrincipal()).thenReturn(principal);
		when(principal.getName()).thenReturn("NAME");
		UserContext result = instance.getUserContext("ID1");
		assertThat(result).isNotNull();
		assertThat(result.getPrincipal()).isNotNull();
		assertThat(result.getPrincipal().getName()).isEqualTo("NAME");
	}

}