/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.ws.rs.core.HttpHeaders;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.ocelotds.core.CallServiceManager;
import org.ocelotds.core.SessionManager;
import org.ocelotds.i18n.ThreadLocalContextHolder;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class OcelotEndpointTest {
	
	@Mock
	private SessionManager sessionManager;

	@Mock
	private CallServiceManager callServiceManager;
	
	@InjectMocks
	private OcelotEndpoint ocelotEndpoint;

	/**
	 * Test of handleOpenConnexion method, of class OcelotEndpoint.
	 * @throws java.io.IOException
	 */
	@Test
	public void testHandleOpenConnexion() throws IOException  {
		System.out.println("handleOpenConnexion");
		Session session = mock(Session.class);
		Map<String, Object> result = new HashMap<>();
		when(session.getUserProperties()).thenReturn(result);
		EndpointConfig config = mock(EndpointConfig.class);
		Map<String, Object> map = new HashMap<>();
		List<String> accepts = new ArrayList<>();
		accepts.add("fr-FR,fr;q=0.8,en-US;q=0.6,en;q=0.4");
		map.put(HttpHeaders.ACCEPT_LANGUAGE, accepts);
		when(config.getUserProperties()).thenReturn(map);
		ocelotEndpoint.handleOpenConnexion(session, config);
		
		Locale locale = (Locale) result.get(Constants.LOCALE);
		assertThat(locale.getCountry()).isEqualTo("FR");
		assertThat(locale.getLanguage()).isEqualTo("fr");
		locale = (Locale) ThreadLocalContextHolder.get(Constants.LOCALE);
		assertThat(locale.getCountry()).isEqualTo("FR");
		assertThat(locale.getLanguage()).isEqualTo("fr");
	}

	/**
	 * Test of onError method, of class OcelotEndpoint.
	 */
	@Test
	public void testOnError() {
		System.out.println("onError");
		Session session = mock(Session.class);
		when(session.getId()).thenReturn(UUID.randomUUID().toString());
		Throwable t = new Exception();
		ocelotEndpoint.onError(session, t);
	}

	/**
	 * Test of handleClosedConnection method, of class OcelotEndpoint.
	 */
	@Test
	public void testHandleClosedConnection() {
		System.out.println("handleClosedConnection");
		Session session = mock(Session.class);
		when(session.getId()).thenReturn(UUID.randomUUID().toString());
		when(session.isOpen()).thenReturn(true);
		CloseReason closeReason = mock(CloseReason.class);
		when(closeReason.getCloseCode()).thenReturn(CloseReason.CloseCodes.TOO_BIG);
		ocelotEndpoint.handleClosedConnection(session, closeReason);
	}

	/**
	 * Test of receiveCommandMessage method, of class OcelotEndpoint.
	 */
//	@Test
	public void testReceiveCommandMessage() {
		System.out.println("receiveCommandMessage");
		Session client = null;
		String json = "";
		ocelotEndpoint.receiveCommandMessage(client, json);
		// TODO review the generated test code and remove the default call to fail.
	}
	
}
