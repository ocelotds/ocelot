/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.web.ws;

import java.util.Map;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.ocelotds.web.SessionManager;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class IWSDecoratorTest {

	@InjectMocks
	IWSDecorator instance = spy(new IWSDecoratorImpl());
	
	@Mock
	Logger logger;
	
	@Mock
	SessionManager sessionManager;
	
	@Mock
	IWSController iwse;

	/**
	 * Test of handleOpenConnexion method, of class IWSDecorator.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testHandleOpenConnexion() throws Exception {
		System.out.println("handleOpenConnexion");
		Session session = mock(Session.class);
		EndpointConfig config = mock(EndpointConfig.class);
		Map map = mock(Map.class);
		HandshakeRequest request = mock(HandshakeRequest.class);
		HttpSession httpSession = mock(HttpSession.class);
		when(session.getId()).thenReturn("WSID");
		when(config.getUserProperties()).thenReturn(map);
		when(map.get(eq(Constants.HANDSHAKEREQUEST))).thenReturn(request);
		when(request.getHttpSession()).thenReturn(httpSession);
		when(httpSession.getId()).thenReturn("HTTPID");
		instance.handleOpenConnexion(session, config);
		verify(sessionManager).linkWsToHttp(eq(session), eq("HTTPID"));
		verify(iwse).handleOpenConnexion(eq(session), eq(config));
	}

	/**
	 * Test of handleClosedConnexion method, of class IWSDecorator.
	 */
	@Test
	public void testHandleClosedConnexion() {
		System.out.println("handleClosedConnexion");
		Session session = mock(Session.class);
		when(session.getId()).thenReturn("WSID");
		CloseReason closeReason = mock(CloseReason.class);
		instance.handleClosedConnexion(session, closeReason);
		verify(sessionManager).unlinkWs(eq("WSID"));
		verify(iwse).handleClosedConnexion(eq(session), eq(closeReason));
	}

	public class IWSDecoratorImpl extends IWSDecorator {

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