/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.core.mtc;

import java.util.Map;
import javax.servlet.http.HttpSession;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.messaging.MessageToClient;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class RSMessageToClientManagerTest {
	
	@InjectMocks
	@Spy
	private RSMessageToClientManager instance ;
	
	/**
	 * Test of getSessionBeans method, of class WSMessageToClientManager.
	 */
	@Test
	public void testGetSessionBeans() {
		System.out.println("getSessionBeans");
		HttpSession session = mock(HttpSession.class);
		Map<String, Object> map = mock(Map.class);
		when(session.getAttribute(eq(Constants.SESSION_BEANS))).thenReturn(null).thenReturn(map);
		Map<String, Object> result = instance.getSessionBeans(session);
		assertThat(result).isNotNull();
		assertThat(result).isNotSameAs(map);
		
		result = instance.getSessionBeans(session);
		verify(session, times(1)).setAttribute(eq(Constants.SESSION_BEANS), any(Map.class));
		assertThat(result).isEqualTo(map);
	}
	
	/**
	 * Test of createMessageToClient method, of class WSMessageToClientManager.
	 */
	@Test
	public void testCreateMessageToClient() {
		System.out.println("createMessageToClient");
		HttpSession session = mock(HttpSession.class);
		MessageFromClient mfc = mock(MessageFromClient.class);
		doReturn(null).when((MessageToClientManager) instance)._createMessageToClient(any(MessageFromClient.class), any(HttpSession.class));
		MessageToClient mtc = instance.createMessageToClient(mfc, session);
		assertThat(mtc).isNull();
	}
}
