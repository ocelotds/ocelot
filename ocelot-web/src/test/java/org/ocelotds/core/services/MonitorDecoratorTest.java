/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.core.services;

import java.util.HashMap;
import java.util.Map;
import javax.websocket.Session;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ocelotds.Constants;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.messaging.MessageToClient;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class MonitorDecoratorTest {
	
	private final static long WAIT = 1000;

	@Mock
	private Logger logger;

	@InjectMocks
	private MonitorDecorator instance = new MonitorDecoratorImpl();

	@Mock
	private MessageToClientService messageToClientService;
	
	/**
	 * Prepare mocks 
	 */
	@Before
	public void prepareMocks() {
		when(messageToClientService.createMessageToClient(any(MessageFromClient.class), any(Session.class))).then(new Answer<MessageToClient>() {
			@Override
			public MessageToClient answer(InvocationOnMock invocation) throws Throwable {
				Thread.sleep(WAIT);
				return new MessageToClient();
			}
		});
	}
	
	/**
	 * Test of createMessageToClient method, of class MonitorDecorator.
	 */
	@Test
	public void testCreateMessageToClientMonitor() {
		System.out.println("createMessageToClient");
		MessageFromClient message = mock(MessageFromClient.class);
		Session client = mock(Session.class);
		Map<String, Object> map = new HashMap<>();

		when(client.getUserProperties()).thenReturn(map);

		map.put(Constants.Options.MONITOR, true);
		MessageToClient result = instance.createMessageToClient(message, client);
		assertThat(result.getTime()).isGreaterThanOrEqualTo(WAIT);
		assertThat(result.getTime()).isLessThan(WAIT+5);
	}

	/**
	 * Test of createMessageToClient method, of class MonitorDecorator.
	 */
	@Test
	public void testCreateMessageToClientNoMonitor() {
		System.out.println("createMessageToClient");
		MessageFromClient message = mock(MessageFromClient.class);
		Session client = mock(Session.class);
		Map<String, Object> map = new HashMap<>();

		when(client.getUserProperties()).thenReturn(map);

		map.put(Constants.Options.MONITOR, false);
		MessageToClient result = instance.createMessageToClient(message, client);
		assertThat(result.getTime()).isZero();
	}
	/**
	 * Test of createMessageToClient method, of class MonitorDecorator.
	 */
	@Test
	public void testCreateMessageToClientFail() {
		System.out.println("createMessageToClient");
		MessageToClient result = instance.createMessageToClient(null, null);
		assertThat(result).isNull();
	}

	public class MonitorDecoratorImpl extends MonitorDecorator {

	}
}