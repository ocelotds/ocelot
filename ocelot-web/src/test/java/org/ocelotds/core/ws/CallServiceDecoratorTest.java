/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.core.ws;

import java.util.HashMap;
import java.util.Map;
import javax.websocket.Session;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.ocelotds.context.ThreadLocalContextHolder;
import org.ocelotds.messaging.MessageFromClient;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class CallServiceDecoratorTest {

	@Mock
	private Logger logger;

	@Mock
	private CallService callSercice;
	
	@InjectMocks
	private CallServiceDecorator instance = new CallServiceDecoratorImpl();

	/**
	 * Test of decorate method sendMessageToClient, of class CallServiceDecorator.
	 */
	@Test
	public void testDecorator() {
		System.out.println("Decorate sendMessageToClient");
		Session session = mock(Session.class);
		when(callSercice.sendMessageToClient(any(MessageFromClient.class), any(Session.class))).thenReturn(Boolean.TRUE);
		
		boolean result = instance.sendMessageToClient(null, session);
		assertThat(result).isTrue();
		assertThat(ThreadLocalContextHolder.get(Constants.SESSION)).isEqualTo(session);
	}

	/**
	 * Test of decorate method sendMessageToClient, of class CallServiceDecorator.
	 */
	@Test
	public void testDecoratorDoNothing() {
		System.out.println("Decorate sendMessageToClient");
		instance.sendMessageToClient(null, null);
		boolean result = instance.sendMessageToClient(null, null);
		assertThat(result).isFalse();
	}

	private static class CallServiceDecoratorImpl extends CallServiceDecorator {
		
	}

}