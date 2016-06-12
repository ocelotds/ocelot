/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.dashboard.decorators;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.dashboard.services.HttpSessionManager;
import org.ocelotds.web.ws.IWSController;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class IWSControllerMonitorTest {

	@InjectMocks
	IWSControllerMonitor instance = spy(new IWSControllerMonitorImpl());

	@Mock
	IWSController iwse;
	
	@Mock
	private Logger logger;

	@Mock
	HttpSessionManager httpSessionManager;

	/**
	 * Test of handleOpenConnexion method, of class IWSControllerMonitor.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testHandleOpenConnexion() throws Exception {
		System.out.println("handleOpenConnexion");
		Session session = mock(Session.class);
		EndpointConfig config = mock(EndpointConfig.class);
		HandshakeRequest handshakeRequest = mock(HandshakeRequest.class);
		HttpSession httpSession = mock(HttpSession.class);
		Map<String, Object> configProperties = mock(Map.class);
		
		when(config.getUserProperties()).thenReturn(configProperties);
		when(handshakeRequest.getHttpSession()).thenReturn(httpSession);
		when(httpSession.getId()).thenReturn("SESSIONID");
		when(configProperties.get(eq(Constants.HANDSHAKEREQUEST))).thenReturn(handshakeRequest);
		
		instance.handleOpenConnexion(session, config);
		
		verify(httpSessionManager).addSession(eq(session), eq("SESSIONID"));
		verify(iwse).handleOpenConnexion(eq(session), eq(config));
	}

	/**
	 * Test of handleClosedConnexion method, of class IWSControllerMonitor.
	 */
	@Test
	public void testHandleClosedConnection() {
		System.out.println("handleClosedConnection");
		Session session = mock(Session.class);
		CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "");
		instance.handleClosedConnexion(session, closeReason);
		verify(httpSessionManager).removeSession(eq(session));
		verify(iwse).handleClosedConnexion(eq(session), eq(closeReason));
	}

	public class IWSControllerMonitorImpl extends IWSControllerMonitor {

		@Override
		public void onError(Session session, Throwable t) {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public void receiveCommandMessage(Session client, String json) {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
	}

}