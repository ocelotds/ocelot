/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.web.ws;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.core.CdiBeanResolver;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class WSEndpointTest {

	@InjectMocks
	@Spy
	WSEndpoint instance;

	/**
	 * Test of handleOpenConnexion method, of class WSEndpoint.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testHandleOpenConnexion() throws Exception {
		System.out.println("handleOpenConnexion");
		Session session = mock(Session.class);
		EndpointConfig config = mock(EndpointConfig.class);
		WSController wSController = mock(WSController.class);
		doReturn(wSController).when(instance).getWSController();

		instance.handleOpenConnexion(session, config);
		verify(wSController).handleOpenConnexion(eq(session), eq(config));
	}

	/**
	 * Test of onError method, of class WSEndpoint.
	 */
	@Test
	public void testOnError() {
		System.out.println("onError");
		Session session = mock(Session.class);
		Throwable t = mock(Throwable.class);
		WSController wSController = mock(WSController.class);
		doReturn(wSController).when(instance).getWSController();

		instance.onError(session, t);
		verify(wSController).onError(eq(session), eq(t));
	}

	/**
	 * Test of handleClosedConnexion method, of class WSEndpoint.
	 */
	@Test
	public void testHandleClosedConnexion() {
		System.out.println("handleClosedConnexion");
		Session session = mock(Session.class);
		CloseReason closeReason = mock(CloseReason.class);
		WSController wSController = mock(WSController.class);
		doReturn(wSController).when(instance).getWSController();

		instance.handleClosedConnexion(session, closeReason);
		verify(wSController).handleClosedConnexion(eq(session), eq(closeReason));
	}

	/**
	 * Test of receiveCommandMessage method, of class WSEndpoint.
	 */
	@Test
	public void testReceiveCommandMessage() {
		System.out.println("receiveCommandMessage");
		Session session = mock(Session.class);
		WSController wSController = mock(WSController.class);
		doReturn(wSController).when(instance).getWSController();

		instance.receiveCommandMessage(session, "JSON");
		verify(wSController).receiveCommandMessage(eq(session), eq("JSON"));
		
	}

//	/**
//	 * Test of getWSController method, of class WSEndpoint.
//	 */
//	@Test
//	public void testGetWSController() {
//		System.out.println("getWSController");
//		CdiBeanResolver cdiBeanResolver = mock(CdiBeanResolver.class);
//		WSController wSController = mock(WSController.class);
//		doReturn(cdiBeanResolver).when(instance).getCdiBeanResolver();
//		when(cdiBeanResolver.getBean(eq(WSController.class))).thenReturn(wSController);
//		WSController result = instance.getWSController();
//		assertThat(result).isEqualTo(wSController);
//	}
//
//	/**
//	 * Test of getCdiBeanResolver method, of class WSEndpoint.
//	 */
//	@Test
//	public void testGetCdiBeanResolver() {
//		System.out.println("getCdiBeanResolver");
//		CdiBeanResolver result = instance.getCdiBeanResolver();
//		assertThat(result).isInstanceOf(CdiBeanResolver.class);
//	}

}