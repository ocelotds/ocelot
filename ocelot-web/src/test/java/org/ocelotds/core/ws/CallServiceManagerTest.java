/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.ws;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import org.ocelotds.messaging.MessageFromClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.Spy;
import org.ocelotds.messaging.MessageToClient;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.ocelotds.core.mtc.WSMessageToClientManager;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class CallServiceManagerTest {

	@Mock
	private Logger logger;

	@Mock
	private WSMessageToClientManager messageToClientService;

	@Spy
	@InjectMocks
	private CallServiceManager instance;

	/**
	 * Test of sendMessageToClient method, of class CallServiceManager.
	 */
	@Test
	public void testSendMessageToClient() {
		System.out.println("sendMessageToClient");
		Session client = mock(Session.class);
		MessageToClient mtc = mock(MessageToClient.class);
		RemoteEndpoint.Async async = mock(RemoteEndpoint.Async.class);
		when(client.getAsyncRemote()).thenReturn(async);
		when(messageToClientService.createMessageToClient(any(MessageFromClient.class), any(Session.class))).thenReturn(mtc).thenReturn(null);
		
		boolean result = instance.sendMessageToClient(new MessageFromClient(), client);
		assertThat(result).isTrue();
		
		result = instance.sendMessageToClient(new MessageFromClient(), client);
		assertThat(result).isFalse();

		ArgumentCaptor<MessageToClient> captureMsg = ArgumentCaptor.forClass(MessageToClient.class);
		verify(async).sendObject(captureMsg.capture());
		
		assertThat(captureMsg.getValue()).isEqualTo(mtc);
	}
}
