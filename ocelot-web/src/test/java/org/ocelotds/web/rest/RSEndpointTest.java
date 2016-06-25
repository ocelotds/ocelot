/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web.rest;

import org.ocelotds.topic.UserContextFactory;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.ocelotds.context.ThreadLocalContextHolder;
import org.ocelotds.core.mtc.RSMessageToClientManager;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.messaging.MessageToClient;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class RSEndpointTest {
	

	@InjectMocks
	@Spy
	private RSEndpoint instance;
	
	@Mock
	private HttpServletRequest request;

	@Mock
	private RSMessageToClientManager messageToClientService;
	
	@Mock
	private UserContextFactory userContextFactory;
	
	@Mock
	private Logger logger;

	/**
	 * Test of getMessageToClient method, of class RSEndpoint.
	 */
	@Test
	public void testGetMessageToClient() {
		System.out.println("getMessageToClient");
		MessageToClient mtc = mock(MessageToClient.class);
		HttpSession session = mock(HttpSession.class);
		when(request.getSession()).thenReturn(session);
		doNothing().when(instance).setContext(any(HttpSession.class));
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s}",
				  Constants.Message.ID, UUID.randomUUID().toString(),
				  Constants.Message.DATASERVICE, "DataServiceClassName",
				  Constants.Message.OPERATION, "methodName",
				  Constants.Message.ARGUMENTS, "[\"arg\"]");
		
		when(messageToClientService.createMessageToClient(any(MessageFromClient.class), any(HttpSession.class))).thenReturn(mtc);
		when(mtc.toJson()).thenReturn("RESULT");
		String result = instance.getMessageToClient(json);
		assertThat(result).isEqualTo("RESULT");
		verify(messageToClientService).createMessageToClient(any(MessageFromClient.class), any(HttpSession.class));
	}

	/**
	 * Test of getMessageToClient method, of class RSEndpoint.
	 */
	@Test
	public void testSetContext() {
		System.out.println("setContext");
		HttpSession session = mock(HttpSession.class);
		instance.setContext(session);
		instance.setContext(session);
		assertThat(ThreadLocalContextHolder.get(Constants.HTTPSESSION)).isEqualTo(session);
		assertThat(ThreadLocalContextHolder.get(Constants.HTTPREQUEST)).isEqualTo(request);
	}
}
