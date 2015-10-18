/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.ws.rs.core.HttpHeaders;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class OcelotRequestConfiguratorTest {

	@Mock
	private Logger logger;

	@InjectMocks
	@Spy
	private OcelotRequestConfigurator instance;

	/**
	 * Test of modifyHandshake method, of class OcelotRequestConfigurator.
	 */
	@Test
	public void testModifyHandshakeWithDefaultAccept() {
		System.out.println("testModifyHandshakeWithDefaultAccept");
		ServerEndpointConfig sec = mock(ServerEndpointConfig.class);
		HandshakeRequest request = mock(HandshakeRequest.class);
		HandshakeResponse response = mock(HandshakeResponse.class);
		Map<String, Object> userProperties = new HashMap<>();
		Map<String, List<String>> headers = new HashMap<>();

		when(sec.getUserProperties()).thenReturn(userProperties);
		when(request.getHeaders()).thenReturn(headers);

		doReturn(null).when(instance).getSecurityContext();
		instance.modifyHandshake(sec, request, response);
		Locale result = (Locale) sec.getUserProperties().get(Constants.LOCALE);
		assertThat(result).isEqualTo(new Locale("en", "US"));
	}

	/**
	 * Test of modifyHandshake method, of class OcelotRequestConfigurator.
	 */
	@Test
	public void testModifyHandshake() {
		System.out.println("testModifyHandshake");
		ServerEndpointConfig sec = mock(ServerEndpointConfig.class);
		Map<String, Object> userProperties = new HashMap<>();
		HandshakeRequest request = mock(HandshakeRequest.class);
		HandshakeResponse response = mock(HandshakeResponse.class);
		Map<String, List<String>> headers = new HashMap<>();
		List<String> accepts = Arrays.asList("fr", "fr-FR;q=1");
		headers.put(HttpHeaders.ACCEPT_LANGUAGE, accepts);

		when(sec.getUserProperties()).thenReturn(userProperties);
		when(request.getHeaders()).thenReturn(headers);
		doReturn(null).when(instance).getSecurityContext();

		instance.modifyHandshake(sec, request, response);
		Locale result = (Locale) sec.getUserProperties().get(Constants.LOCALE);
		assertThat(result).isEqualTo(new Locale("fr", "FR"));
	}

	@Test
	public void testGetSessionBeansMap() {
		HttpSession session = mock(HttpSession.class);
		Object result = instance.getSessionBeansMap(null);
		assertThat(result).isInstanceOf(Map.class);
		result = instance.getSessionBeansMap("");
		assertThat(result).isInstanceOf(Map.class);
		when(session.getAttribute(Constants.SESSION_BEANS)).thenReturn(result).thenReturn(null);
		result = instance.getSessionBeansMap(session);
		assertThat(result).isInstanceOf(Map.class);
		result = instance.getSessionBeansMap(session);

		ArgumentCaptor<Map> captureMap = ArgumentCaptor.forClass(Map.class);
		verify(session).setAttribute(eq(Constants.SESSION_BEANS), captureMap.capture());
		assertThat(result).isInstanceOf(Map.class);
		assertThat(result).isEqualTo(captureMap.getValue());
	}
}
