/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.topic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.SessionException;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ocelotds.messaging.MessageToClient;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.security.JsTopicMessageController;
import org.ocelotds.security.NotRecipientException;
import org.ocelotds.security.UserContext;
import org.ocelotds.topic.messageControl.MessageControllerManager;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class TopicsMessagesBroadcasterTest {

	private final Object PAYLOAD = "PAYLOAD";
	private final String TOPIC = "TOPIC";

	@Mock
	private Logger logger;

	@Mock
	private TopicManager topicManager;

	@Mock
	private UserContextFactory userContextFactory;
	
	@Mock
	MessageControllerManager messageControllerManager;

	@InjectMocks
	@Spy
	private TopicsMessagesBroadcaster instance;

	/**
	 * Test of sendMessageToTopic method, of class TopicsMessagesBroadcaster.
	 *
	 * @throws javax.websocket.SessionException
	 */
	@Test
	public void testSendMessageToTopic() throws SessionException {
		System.out.println("testSendMessageToTopic");
		doReturn(5).when(instance).sendMessageToTopic(anyList(), any(MessageToClient.class), eq(PAYLOAD));
		int result = instance.sendMessageToTopic(new MessageToClient(), PAYLOAD);
		assertThat(result).isEqualTo(5);
	}

	/**
	 * Test of sendMessageToTopic method, of class TopicsMessagesBroadcaster.
	 *
	 * @throws javax.websocket.SessionException
	 */
	@Test
	public void testSendMessageToTopicNoSessions() throws SessionException {
		System.out.println("testSendMessageToTopicNoSessions");
		Collection<Session> sessions = new ArrayList<>();

		when(topicManager.getSessionsForTopic(anyString())).thenReturn(null).thenReturn(sessions); // session is closed

		int result = instance.sendMessageToTopic(null, new MessageToClient(), PAYLOAD);
		assertThat(result).isEqualTo(0);

		result = instance.sendMessageToTopic(null, new MessageToClient(), PAYLOAD);
		assertThat(result).isEqualTo(0);
	}

	/**
	 * Test of sendMessageToTopic method, of class TopicsMessagesBroadcaster.
	 *
	 * @throws javax.websocket.SessionException
	 */
	@Test
	public void testSendMessageToTopicForRetain() throws SessionException {
		when(logger.isDebugEnabled()).thenReturn(Boolean.TRUE);
		System.out.println("testSendMessageToTopicFor2Opened1ClosedSession");
		Session session = mock(Session.class);
		Collection<Session> sessions = Arrays.asList(mock(Session.class), session, mock(Session.class));
		Collection<Session> sessionTargets = Arrays.asList(session);

		when(topicManager.getSessionsForTopic(anyString())).thenReturn(sessions);
		doReturn(2).when(instance).sendMessageToTopicForSessions(anyList(), any(MessageToClient.class), anyString());

		int result = instance.sendMessageToTopic(sessionTargets, new MessageToClient(), PAYLOAD);
		assertThat(result).isEqualTo(2);

		ArgumentCaptor<Collection> captureClosed = ArgumentCaptor.forClass(Collection.class);
		verify(instance).sendMessageToTopicForSessions(captureClosed.capture(), any(MessageToClient.class), eq(PAYLOAD));
		assertThat(captureClosed.getValue()).hasSize(1);
	}

	/**
	 * Test of sendMessageToTopic method, of class TopicsMessagesBroadcaster.
	 *
	 * @throws javax.websocket.SessionException
	 */
	@Test
	public void testSendMessageToTopicForSessionsFor2Opened1ClosedSession() throws SessionException {
		when(logger.isDebugEnabled()).thenReturn(Boolean.TRUE);
		System.out.println("testSendMessageToTopicForSessions");
		Collection<Session> sessions = Arrays.asList(mock(Session.class), mock(Session.class), mock(Session.class));
		JsTopicMessageController jtmc = mock(JsTopicMessageController.class);

		when(messageControllerManager.getJsTopicMessageController(anyString())).thenReturn(jtmc);
		doReturn(1).doThrow(SessionException.class).doReturn(1).when(instance).checkAndSendMtcToSession(any(Session.class), eq(jtmc), any(MessageToClient.class), anyObject());
		when(topicManager.getSessionsForTopic(anyString())).thenReturn(sessions);

		int result = instance.sendMessageToTopicForSessions(sessions, new MessageToClient(), PAYLOAD);
		assertThat(result).isEqualTo(2);

		ArgumentCaptor<Collection> captureClosed = ArgumentCaptor.forClass(Collection.class);
		verify(topicManager).removeSessionsToTopic(captureClosed.capture());
		assertThat(captureClosed.getValue()).hasSize(1);
	}
	
	/**
	 * Test of checkAndSendMtcToSession method, of class.
	 *
	 * @throws javax.websocket.SessionException
	 */
	@Test
	public void checkAndSendMtcToSessionNullTest() throws SessionException {
		System.out.println("checkAndSendMtcToSession");
		JsTopicMessageController jtmcmsgControl = mock(JsTopicMessageController.class);
		MessageToClient mtc = mock(MessageToClient.class);

		int result = instance.checkAndSendMtcToSession(null, jtmcmsgControl, mtc, null);
		assertThat(result).isEqualTo(0);
	}

	/**
	 * Test of checkAndSendMtcToSession method, of class.
	 *
	 * @throws javax.websocket.SessionException
	 */
	@Test(expected = SessionException.class)
	public void checkAndSendMtcToSessionCloseTest() throws SessionException {
		System.out.println("checkAndSendMtcToSession");
		Session session = mock(Session.class);
		JsTopicMessageController jtmcmsgControl = mock(JsTopicMessageController.class);
		MessageToClient mtc = mock(MessageToClient.class);

		when(session.isOpen()).thenReturn(Boolean.FALSE);

		instance.checkAndSendMtcToSession(session, jtmcmsgControl, mtc, null);
	}

	/**
	 * Test of checkAndSendMtcToSession method, of class.
	 *
	 * @throws javax.websocket.SessionException
	 * @throws org.ocelotds.security.NotRecipientException
	 */
	@Test
	public void checkAndSendMtcToSessionTest() throws SessionException, NotRecipientException {
		System.out.println("checkAndSendMtcToSession");
		Session session = mock(Session.class);
		RemoteEndpoint.Async async = mock(RemoteEndpoint.Async.class);
		when(session.isOpen()).thenReturn(true);
		when(session.getId()).thenReturn("ID1");
		when(session.getAsyncRemote()).thenReturn(async);
		JsTopicMessageController jtmcmsgControl = mock(JsTopicMessageController.class);
		MessageToClient mtc = mock(MessageToClient.class);

		when(session.isOpen()).thenReturn(Boolean.TRUE);
		when(userContextFactory.getUserContext(eq("ID1"))).thenReturn(mock(UserContext.class));
		doNothing().doThrow(NotRecipientException.class).when(instance).checkMessageTopic(any(UserContext.class), anyString(), eq(PAYLOAD), eq(jtmcmsgControl));

		int result = instance.checkAndSendMtcToSession(session, jtmcmsgControl, mtc, PAYLOAD);
		assertThat(result).isEqualTo(1);

		result = instance.checkAndSendMtcToSession(session, jtmcmsgControl, mtc, PAYLOAD);
		assertThat(result).isEqualTo(0);
	}

	/**
	 * Test of checkMessageTopic method, of class.
	 *
	 * @throws org.ocelotds.security.NotRecipientException
	 */
	@Test
	public void checkMessageTopicTest() throws NotRecipientException {
		System.out.println("checkMessageTopic");
		UserContext userContext = mock(UserContext.class);
		JsTopicMessageController jtmc = mock(JsTopicMessageController.class);
		doNothing().when(jtmc).checkRight(eq(userContext), eq(TOPIC), eq(PAYLOAD));
		instance.checkMessageTopic(userContext, TOPIC, PAYLOAD, jtmc);
	}

	/**
	 * Test of checkMessageTopic method, of class.
	 *
	 * @throws org.ocelotds.security.NotRecipientException
	 */
	@Test(expected = NotRecipientException.class)
	public void checkMessageTopicTestFail() throws NotRecipientException {
		System.out.println("checkMessageTopic");
		UserContext userContext = mock(UserContext.class);
		JsTopicMessageController jtmc = mock(JsTopicMessageController.class);
		doThrow(NotRecipientException.class).when(jtmc).checkRight(eq(userContext), eq(TOPIC), eq(PAYLOAD));
		instance.checkMessageTopic(userContext, TOPIC, PAYLOAD, jtmc);
	}
}
