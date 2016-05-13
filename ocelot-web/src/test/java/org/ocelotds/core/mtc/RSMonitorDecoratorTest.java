/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.core.mtc;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ocelotds.Constants;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.objects.Options;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class RSMonitorDecoratorTest {
	
	private final static long WAIT = 1000;

	@Mock
	private Logger logger;

	@InjectMocks
	private RSMonitorDecorator instance = new MonitorDecoratorImpl();

	@Mock
	private RSMessageToClientService messageToClientService;
	
	/**
	 * Prepare mocks 
	 */
	@Before
	public void prepareMocks() {
		when(messageToClientService.createMessageToClient(any(MessageFromClient.class), any(HttpSession.class))).then(new Answer<MessageToClient>() {
			@Override
			public MessageToClient answer(InvocationOnMock invocation) throws Throwable {
				Thread.sleep(WAIT);
				return new MessageToClient();
			}
		});
	}
	
	/**
	 * Test of createMessageToClient method, of class WSMonitorDecorator.
	 */
	@Test
	public void testCreateMessageToClientMonitor() {
		System.out.println("createMessageToClient");
		MessageFromClient message = mock(MessageFromClient.class);
		HttpSession session = mock(HttpSession.class);
		Options options = new Options();
		options.setMonitor(true);
		when(session.getAttribute(Constants.Options.OPTIONS)).thenReturn(options);

		MessageToClient result = instance.createMessageToClient(message, session);
		assertThat(result.getTime()).isGreaterThanOrEqualTo(WAIT);
	}

	/**
	 * Test of createMessageToClient method, of class WSMonitorDecorator.
	 */
	@Test
	public void testCreateMessageToClientNoMonitor() {
		System.out.println("createMessageToClient");
		MessageFromClient message = mock(MessageFromClient.class);
		HttpSession session = mock(HttpSession.class);
		Map<String, Object> map = new HashMap<>();

		Options options = new Options();
		options.setMonitor(false);
		when(session.getAttribute(Constants.Options.OPTIONS)).thenReturn(options);

		MessageToClient result = instance.createMessageToClient(message, session);
		assertThat(result.getTime()).isZero();
	}
	/**
	 * Test of createMessageToClient method, of class WSMonitorDecorator.
	 */
	@Test
	public void testCreateMessageToClientFail() {
		System.out.println("createMessageToClient");
		MessageToClient result = instance.createMessageToClient(null, null);
		assertThat(result.getTime()).isZero();
	}

	public class MonitorDecoratorImpl extends RSMonitorDecorator {

	}
}