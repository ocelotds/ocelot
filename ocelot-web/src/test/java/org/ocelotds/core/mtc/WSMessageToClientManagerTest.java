/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
