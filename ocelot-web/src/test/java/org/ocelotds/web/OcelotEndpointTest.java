/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.ocelotds.core.CallServiceManager;
import org.ocelotds.core.SessionManager;
import org.ocelotds.context.ThreadLocalContextHolder;
import org.ocelotds.messaging.MessageFromClient;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class OcelotEndpointTest {

	@Mock
	private Logger logger;

	@Mock
	private SessionManager sessionManager;

	@Mock
	private CallServiceManager callServiceManager;

	@InjectMocks
	private OcelotEndpoint ocelotEndpoint;
	
	/**
	 * Test of handleOpenConnexion method, of class OcelotEndpoint.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testHandleOpenConnexionFromBrowser() throws IOException {
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

		Locale locale = (Locale) ThreadLocalContextHolder.get(Constants.LOCALE);
		assertThat(locale.getCountry()).isEqualTo("FR");
		assertThat(locale.getLanguage()).isEqualTo("fr");
	}

	/**
	 * Test of handleOpenConnexion method, of class OcelotEndpoint.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testHandleOpenConnexionNotFromBrowser() throws IOException {
		System.out.println("handleOpenConnexion");
		Session session = mock(Session.class);
		Map<String, Object> result = new HashMap<>();
		when(session.getUserProperties()).thenReturn(result);
		EndpointConfig config = mock(EndpointConfig.class);
		Map<String, Object> map = new HashMap<>();
		List<String> accepts = new ArrayList<>();
		map.put(HttpHeaders.ACCEPT_LANGUAGE, accepts);
		when(config.getUserProperties()).thenReturn(map);
		ocelotEndpoint.handleOpenConnexion(session, config);

		Locale locale = (Locale) ThreadLocalContextHolder.get(Constants.LOCALE);
		assertThat(locale.getCountry()).isEqualTo("US");
		assertThat(locale.getLanguage()).isEqualTo("en");

		map.put(HttpHeaders.ACCEPT_LANGUAGE, null);
		ocelotEndpoint.handleOpenConnexion(session, config);
		locale = (Locale) ThreadLocalContextHolder.get(Constants.LOCALE);
		assertThat(locale.getCountry()).isEqualTo("US");
		assertThat(locale.getLanguage()).isEqualTo("en");
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
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testHandleClosedConnection() throws IOException {
		System.out.println("handleClosedConnection");
		Session session = mock(Session.class);
		when(session.getId()).thenReturn(UUID.randomUUID().toString());
		when(session.isOpen()).thenReturn(true);
		CloseReason closeReason = mock(CloseReason.class);
		when(closeReason.getCloseCode()).thenReturn(CloseReason.CloseCodes.TOO_BIG);
		ocelotEndpoint.handleClosedConnection(session, closeReason);
		doThrow(IOException.class).when(session).close();
		ocelotEndpoint.handleClosedConnection(session, closeReason);
		doThrow(IllegalStateException.class).when(session).close();
		ocelotEndpoint.handleClosedConnection(session, closeReason);
		when(session.isOpen()).thenReturn(false);
		ocelotEndpoint.handleClosedConnection(session, closeReason);
	}

	/**
	 * Test of receiveCommandMessage method, of class OcelotEndpoint.
	 */
	@Test
	public void testReceiveCommandMessage() {
		System.out.println("receiveCommandMessage");
		Session client = mock(Session.class);
		String[] parameterNames = new String[]{"\"a\"", "\"b\"", "\"c\""};
		String[] parameters = new String[]{"\"toto\"", "5", "true"};
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s,\"%s\":%s}",
				  Constants.Message.ID, "111",
				  Constants.Message.DATASERVICE, "ClassName",
				  Constants.Message.OPERATION, "methodName",
				  Constants.Message.ARGUMENTNAMES, Arrays.toString(parameterNames),
				  Constants.Message.ARGUMENTS, Arrays.toString(parameters));

		ocelotEndpoint.receiveCommandMessage(client, json);

		ArgumentCaptor<MessageFromClient> captureMsg = ArgumentCaptor.forClass(MessageFromClient.class);
		ArgumentCaptor<Session> captureSession = ArgumentCaptor.forClass(Session.class);
		verify(callServiceManager, times(1)).sendMessageToClient(captureMsg.capture(), captureSession.capture());

		MessageFromClient result = captureMsg.getValue();
		assertThat(result.getId()).isEqualTo("111");
		assertThat(result.getDataService()).isEqualTo("ClassName");
		assertThat(result.getOperation()).isEqualTo("methodName");
		assertThat(result.getParameterNames()).containsExactly("\"a\"", "\"b\"", "\"c\"");
		assertThat(result.getParameters()).containsExactly("\"toto\"", "5", "true");
	}

}
