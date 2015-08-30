/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.ws.rs.core.HttpHeaders;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 *
 * @author hhfrancois
 */
public class OcelotRequestConfiguratorTest {
	
	/**
	 * Test of modifyHandshake method, of class OcelotRequestConfigurator.
	 */
	@Test
	public void testModifyHandshakeWithDefaultAccept() {
		System.out.println("testModifyHandshakeWithDefaultAccept");
		ServerEndpointConfig sec = mock(ServerEndpointConfig.class);
		Map<String, Object> userProperties = new HashMap<>();
		when(sec.getUserProperties()).thenReturn(userProperties);
		
		HandshakeRequest request = mock(HandshakeRequest.class);
		Map<String, List<String>> headers = new HashMap<>();
		when(request.getHeaders()).thenReturn(headers);
		
		HandshakeResponse response = mock(HandshakeResponse.class);

		OcelotRequestConfigurator instance = new OcelotRequestConfigurator();
		instance.modifyHandshake(sec, request, response);
		List<String> result = (List) sec.getUserProperties().get(HttpHeaders.ACCEPT_LANGUAGE);
		assertThat(result).hasSize(1);
		assertThat(result).contains("en-US;q=1");
	}
	
	/**
	 * Test of modifyHandshake method, of class OcelotRequestConfigurator.
	 */
	@Test
	public void testModifyHandshake() {
		System.out.println("testModifyHandshake");
		ServerEndpointConfig sec = mock(ServerEndpointConfig.class);
		Map<String, Object> userProperties = new HashMap<>();
		when(sec.getUserProperties()).thenReturn(userProperties);
		
		HandshakeRequest request = mock(HandshakeRequest.class);
		Map<String, List<String>> headers = new HashMap<>();
		List<String> accept = Arrays.asList(new String[]{"fr-FR;q=1"});
		headers.put(HttpHeaders.ACCEPT_LANGUAGE, accept);
		when(request.getHeaders()).thenReturn(headers);
		
		HandshakeResponse response = mock(HandshakeResponse.class);

		OcelotRequestConfigurator instance = new OcelotRequestConfigurator();
		instance.modifyHandshake(sec, request, response);
		List<String> result = (List) sec.getUserProperties().get(HttpHeaders.ACCEPT_LANGUAGE);
		assertThat(result).hasSize(1);
		assertThat(result).contains("fr-FR;q=1");
	}
}
