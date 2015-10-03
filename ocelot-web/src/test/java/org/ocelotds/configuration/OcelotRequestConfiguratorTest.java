/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.ws.rs.core.HttpHeaders;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
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
	private OcelotRequestConfigurator ocelotRequestConfigurator;
	
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
		
		doReturn(null).when(ocelotRequestConfigurator).getSecurityContext();
		ocelotRequestConfigurator.modifyHandshake(sec, request, response);
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
		doReturn(null).when(ocelotRequestConfigurator).getSecurityContext();
		
		ocelotRequestConfigurator.modifyHandshake(sec, request, response);
		Locale result = (Locale) sec.getUserProperties().get(Constants.LOCALE);
		assertThat(result).isEqualTo(new Locale("fr", "FR"));
	}
}
