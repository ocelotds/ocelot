/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

import java.util.Arrays;
import java.util.Collections;
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
	public void testModifyHandshake() {
		System.out.println("testModifyHandshake");
		ServerEndpointConfig sec = mock(ServerEndpointConfig.class);
		Map<String, Object> userProperties = new HashMap<>();
		HandshakeRequest request = mock(HandshakeRequest.class);
		HandshakeResponse response = mock(HandshakeResponse.class);
		Map<String, List<String>> headers = new HashMap<>();
		List<String> accepts = Arrays.asList("fr", "fr-FR;q=1");
		headers.put(HttpHeaders.ACCEPT_LANGUAGE, accepts);
		Map map = new HashMap<>();

		when(sec.getUserProperties()).thenReturn(userProperties);
		when(request.getHeaders()).thenReturn(headers);
		doReturn(map).when(instance).getSessionBeansMap(anyObject());
		doReturn(Locale.FRANCE).when(instance).getLocale(any(HandshakeRequest.class));
		doReturn(true).when(instance).isMonitored(any(HandshakeRequest.class));

		instance.modifyHandshake(sec, request, response);

		assertThat(sec.getUserProperties().get(Constants.SESSION_BEANS)).isEqualTo(map);
		assertThat(sec.getUserProperties().get(Constants.HANDSHAKEREQUEST)).isEqualTo(request);
		assertThat(sec.getUserProperties().get(Constants.LOCALE)).isEqualTo(Locale.FRANCE);
		assertThat(sec.getUserProperties().get(Constants.Options.MONITOR)).isEqualTo(Boolean.TRUE);
	}

	/**
	 * Test of getLocale method, of class OcelotRequestConfigurator.
	 */
	@Test
	public void testGetLocale() {
		System.out.println("testGetLocale");
		HandshakeRequest request = mock(HandshakeRequest.class);
		Map<String, List<String>> map = mock(Map.class);
		List<String> with = Arrays.asList("fr", "fr-FR;q=1");

		when(map.get(eq(HttpHeaders.ACCEPT_LANGUAGE))).thenReturn(null).thenReturn(Collections.EMPTY_LIST).thenReturn(with);
		when(request.getHeaders()).thenReturn(map);

		Locale result = instance.getLocale(request);
		assertThat(result).isEqualTo(Locale.US);
		result = instance.getLocale(request);
		assertThat(result).isEqualTo(Locale.US);
		result = instance.getLocale(request);
		assertThat(result).isEqualTo(Locale.FRANCE);
	}

	/**
	 * Test of isMonitored method, of class OcelotRequestConfigurator.
	 */
	@Test
	public void testIsMonitored() {
		System.out.println("testIsMonitored");
		HandshakeRequest request = mock(HandshakeRequest.class);
		Map<String, List<String>> map = mock(Map.class);
		List<String> with = Arrays.asList(Constants.Options.MONITOR);

		when(request.getParameterMap()).thenReturn(map);
		when(map.get(eq("option"))).thenReturn(null).thenReturn(Collections.EMPTY_LIST).thenReturn(with);

		boolean result = instance.isMonitored(request);
		assertThat(result).isFalse();
		result = instance.isMonitored(request);
		assertThat(result).isFalse();
		result = instance.isMonitored(request);
		assertThat(result).isTrue();
	}

	/**
	 * Test of getSessionBeansMap method, of class OcelotRequestConfigurator.
	 */
	@Test
	public void testGetSessionBeansMap() {
		System.out.println("testGetSessionBeansMap");
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
