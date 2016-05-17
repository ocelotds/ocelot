/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

import java.util.HashMap;
import java.util.Map;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
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

		when(sec.getUserProperties()).thenReturn(userProperties);

		instance.modifyHandshake(sec, request, response);

		assertThat(userProperties.get(Constants.HANDSHAKEREQUEST)).isEqualTo(request);
	}
}
