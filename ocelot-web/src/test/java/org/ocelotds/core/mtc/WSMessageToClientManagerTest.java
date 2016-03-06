/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.core.mtc;

import java.util.Map;
import javax.websocket.Session;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class WSMessageToClientManagerTest {

	@InjectMocks
	private WSMessageToClientManager instance ;
	
	/**
	 * Test of getSessionBeans method, of class WSMessageToClientManager.
	 */
	@Test
	public void testGetSessionBeans() {
		System.out.println("getSessionBeans");
		Session session = mock(Session.class);
		Map<String, Object> result = instance.getSessionBeans(session);
		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
	}
	
}
