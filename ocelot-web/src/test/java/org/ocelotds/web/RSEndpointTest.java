/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.HttpHeaders;
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
import org.ocelotds.configuration.LocaleExtractor;
import org.ocelotds.context.ThreadLocalContextHolder;
import org.ocelotds.core.mtc.RSMessageToClientManager;
import org.ocelotds.exceptions.LocaleNotFoundException;
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
		doNothing().when(instance).setContext(any(HttpSession.class));
		List<String> args = new ArrayList<>();
		args.add("\"arg\"");
		List<String> argNames = new ArrayList<>();
		argNames.add("\"argName\"");
		MessageFromClient mfc = new MessageFromClient();
		mfc.setDataService("DataServiceClassName");
		mfc.setId(UUID.randomUUID().toString());
		mfc.setOperation("methodName");
		mfc.setParameterNames(argNames);
		mfc.setParameters(args);
		
		when(messageToClientService.createMessageToClient(any(MessageFromClient.class), any(HttpSession.class))).thenReturn(mtc);
		when(mtc.toJson()).thenReturn("RESULT");
		String result = instance.getMessageToClient(mfc.toJson());
		assertThat(result).isEqualTo("RESULT");
		ArgumentCaptor<MessageFromClient> argument = ArgumentCaptor.forClass(MessageFromClient.class);
		verify(messageToClientService).createMessageToClient(argument.capture(), any(HttpSession.class));
	}

	/**
	 * Test of getMessageToClient method, of class RSEndpoint.
	 */
	@Test
	public void testSetContext() {
		System.out.println("setContext");
		HttpSession session = mock(HttpSession.class);
		instance.setContext(session);
		assertThat(ThreadLocalContextHolder.get(Constants.HTTPSESSION)).isEqualTo(session);
		assertThat(ThreadLocalContextHolder.get(Constants.HTTPREQUEST)).isEqualTo(request);
	}
}
