/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web.ws;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
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
import org.ocelotds.core.CdiBeanResolver;
import org.ocelotds.core.ws.CallServiceManager;
import org.ocelotds.exceptions.LocaleNotFoundException;
import org.ocelotds.topic.UserContextFactory;
import org.ocelotds.topic.TopicManager;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.topic.TopicManagerImpl;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class WSControllerTest {

	@InjectMocks
	@Spy
	private WSController instance;
	
	@Mock
	private TopicManager topicManager;

	@Mock
	private CallServiceManager callServiceManager;
	
	@Mock
	private UserContextFactory userContextFactory;
	
	@Mock
	private Logger logger;

	@Mock
	private LocaleExtractor localeExtractor;

	/**
	 * Test of handleOpenConnexion method, of class WSEndpoint.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testHandleOpenConnexionFromBrowser() throws IOException {
		System.out.println("handleOpenConnexion");
		HandshakeRequest request = mock(HandshakeRequest.class);
		EndpointConfig config = mock(EndpointConfig.class);
		Map<String, Object> map = new HashMap<>();
		map.put(Constants.HANDSHAKEREQUEST, request);
		Session session = mock(Session.class);

		when(config.getUserProperties()).thenReturn(map);
		when(session.getId()).thenReturn("WSSESSIONID");
		doNothing().when(instance).setContext(eq(request));

		instance.handleOpenConnexion(session, config);

		verify(userContextFactory).createUserContext(any(HandshakeRequest.class), eq("WSSESSIONID"));
	}

	/**
	 * Test of onError method, of class WSEndpoint.
	 */
	@Test
	public void testOnError() {
		System.out.println("onError");
		Session session = mock(Session.class);
		when(session.getId()).thenReturn(UUID.randomUUID().toString());
		Throwable t = new Exception();
		instance.onError(session, t);
	}

	/**
	 * Test of handleClosedConnexion method, of class WSEndpoint.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testHandleClosedConnection() throws IOException {
		System.out.println("handleClosedConnection");
		Session session = mock(Session.class);
		when(session.getId()).thenReturn(UUID.randomUUID().toString());
		when(session.isOpen()).thenReturn(true);
		CloseReason closeReason = mock(CloseReason.class);
		when(closeReason.getCloseCode()).thenReturn(CloseReason.CloseCodes.TOO_BIG);
		instance.handleClosedConnexion(session, closeReason);
		doThrow(IOException.class).when(session).close();
		instance.handleClosedConnexion(session, closeReason);
		doThrow(IllegalStateException.class).when(session).close();
		instance.handleClosedConnexion(session, closeReason);
		when(session.isOpen()).thenReturn(false);
		instance.handleClosedConnexion(session, closeReason);
	}

	/**
	 * Test of receiveCommandMessage method, of class WSEndpoint.
	 */
	@Test
	public void testReceiveCommandMessage() {
		System.out.println("receiveCommandMessage");
		Session client = mock(Session.class);
		String[] parameterNames = new String[]{"\"a\"", "\"b\"", "\"c\""};
		String[] parameters = new String[]{"\"toto\"", "5", "true"};
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s,\"%s\":%s}",
				  Constants.Message.ID, "111",
				  Constants.Message.DATASERVICE, "ClassName",
				  Constants.Message.OPERATION, "methodName",
				  Constants.Message.ARGUMENTNAMES, Arrays.toString(parameterNames),
				  Constants.Message.ARGUMENTS, Arrays.toString(parameters));
		instance.receiveCommandMessage(client, json);

		ArgumentCaptor<MessageFromClient> captureMsg = ArgumentCaptor.forClass(MessageFromClient.class);
		ArgumentCaptor<Session> captureSession = ArgumentCaptor.forClass(Session.class);
		verify(callServiceManager).sendMessageToClient(captureMsg.capture(), captureSession.capture());

		MessageFromClient result = captureMsg.getValue();
		assertThat(result.getId()).isEqualTo("111");
		assertThat(result.getDataService()).isEqualTo("ClassName");
		assertThat(result.getOperation()).isEqualTo("methodName");
		assertThat(result.getParameterNames()).containsExactly("a", "b", "c");
		assertThat(result.getParameters()).containsExactly("\"toto\"", "5", "true");
	}
	
//	public <T> void testGetCDI(Class<T> res, T inst, Method m) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//		WSEndpoint oe = spy(new WSEndpoint());
//		CdiBeanResolver resolver = mock(CdiBeanResolver.class);
//		when(resolver.getBean(eq(res))).thenReturn(inst);
//		doReturn(resolver).when(oe).getCdiBeanResolver();
//
//		Object result = m.invoke(oe);
//
//		assertThat(result).isInstanceOf(res);
//	}
//
//	@Test
//	public void testGetTopicManager() throws Exception {
//		System.out.println("getTopicManager");
//		Method m = WSEndpoint.class.getDeclaredMethod("getTopicManager");
//		testGetCDI(TopicManager.class, new TopicManagerImpl(), m);
//	}
//	
//	@Test
//	public void testGetUserContextFactory() throws Exception {
//		System.out.println("getUserContextFactory");
//		Method m = WSEndpoint.class.getDeclaredMethod("getUserContextFactory");
//		testGetCDI(UserContextFactory.class, new UserContextFactory(), m);
//	}
//
//	@Test
//	public void testGetCallServiceManager() throws Exception {
//		System.out.println("getCallServiceManager");
//		Method m = WSEndpoint.class.getDeclaredMethod("getCallServiceManager");
//		testGetCDI(CallServiceManager.class, new CallServiceManager(), m);
//	}
	
//	@Test
//	public void testGetCDIBeanResolver() {
//		System.out.println("getCDIBeanResolver");
//		CdiBeanResolver cdiBeanResolver = instance.getCdiBeanResolver();
//		assertThat(cdiBeanResolver).isInstanceOf(CdiBeanResolver.class);
//	}

	/**
	 * Test of setContext method, of class.
	 */
	@Test
	public void setContextTest() {
		System.out.println("setContext");
		ThreadLocalContextHolder.cleanupThread();
		HandshakeRequest request = mock(HandshakeRequest.class);
		HttpSession httpSession = mock(HttpSession.class);
		when(request.getHttpSession()).thenReturn(httpSession);
		when(httpSession.getAttribute(eq(Constants.LOCALE))).thenReturn(null).thenReturn(Locale.CHINA);
		doReturn(Locale.CANADA).when(instance).getLocale(eq(request));
		instance.setContext(request);
		Object result = ThreadLocalContextHolder.get(Constants.HTTPSESSION);
		verify(httpSession).setAttribute(eq(Constants.LOCALE), eq(Locale.CANADA));
		assertThat(result).isEqualTo(httpSession);
	}

	/**
	 * Test of getLocale method, of class RSEndpoint.
	 * @throws org.ocelotds.exceptions.LocaleNotFoundException
	 */
	@Test
	public void testGetLocaleNotFound() throws LocaleNotFoundException {
		Locale result = instance.getLocale(null);
		assertThat(result).isEqualTo(Locale.US);
		
		HandshakeRequest request = mock(HandshakeRequest.class);
		when(request.getHeaders()).thenReturn(null);
		result = instance.getLocale(request);
		assertThat(result).isEqualTo(Locale.US);

		Map<String, List<String>> headers = new HashMap<>();
		when(request.getHeaders()).thenReturn(headers);
		assertThat(result).isEqualTo(Locale.US);
	}
	/**
	 * Test of getLocale method, of class RSEndpoint.
	 * @throws org.ocelotds.exceptions.LocaleNotFoundException
	 */
	@Test
	public void testGetLocale() throws LocaleNotFoundException {
		System.out.println("getLocale");
		HandshakeRequest request = mock(HandshakeRequest.class);
		Map<String, List<String>> headers = new HashMap<>();
		when(request.getHeaders()).thenReturn(headers);
		when(localeExtractor.extractFromAccept(any(String.class))).thenThrow(LocaleNotFoundException.class).thenReturn(Locale.FRANCE).thenReturn(Locale.CHINA);
		headers.put(HttpHeaders.ACCEPT_LANGUAGE, Arrays.asList(null, "acceptFrance", "acceptChina"));

		Locale result = instance.getLocale(request);
		assertThat(result).isEqualTo(Locale.FRANCE);
	}
}
