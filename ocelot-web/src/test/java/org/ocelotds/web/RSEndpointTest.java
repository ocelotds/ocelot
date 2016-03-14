/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.web;

import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
	private RequestManager requestManager;
	
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
		doNothing().when(instance).setContext(any(HttpSession.class), anyBoolean());
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s,\"%s\":%s}",
				  Constants.Message.ID, UUID.randomUUID().toString(),
				  Constants.Message.DATASERVICE, "DataServiceClassName",
				  Constants.Message.OPERATION, "methodName",
				  Constants.Message.ARGUMENTNAMES, "[\"argName\"]",
				  Constants.Message.ARGUMENTS, "[\"arg\"]");
		
		when(messageToClientService.createMessageToClient(any(MessageFromClient.class), any(HttpSession.class))).thenReturn(mtc);
		when(mtc.toJson()).thenReturn("RESULT");
		String result = instance.getMessageToClient(json, true);
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
		instance.setContext(session, true);
		instance.setContext(session, false);
		ArgumentCaptor<Boolean> argument = ArgumentCaptor.forClass(Boolean.class);
		verify(session, times(2)).setAttribute(eq(Constants.Options.MONITOR), argument.capture());
		assertThat(ThreadLocalContextHolder.get(Constants.HTTPSESSION)).isEqualTo(session);
		assertThat(ThreadLocalContextHolder.get(Constants.HTTPREQUEST)).isEqualTo(request);
		List<Boolean> monitors = argument.getAllValues();
		assertThat(monitors.get(0)).isEqualTo(true);
		assertThat(monitors.get(1)).isEqualTo(false);
	}
}
