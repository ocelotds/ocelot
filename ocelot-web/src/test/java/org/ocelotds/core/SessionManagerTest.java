/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core;

import org.ocelotds.objects.FakeCDI;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.security.JsTopicAccessController;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Spy;
import org.ocelotds.Constants;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.messaging.MessageType;
import org.ocelotds.security.JsTopicACAnnotationLiteral;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionManagerTest {

	private static final String TOPIC1 = "TOPIC_1";
	private static final String TOPIC2 = "TOPIC_2";
	private static final String SUBTOPIC2 = "subscribers:TOPIC_2";

	@Mock
	private Logger logger;

	@Mock
	Instance<JsTopicAccessController> topicAccessController;

	@InjectMocks
	@Spy
	private SessionManager instance;

	@Before
	public void init() {
		when(topicAccessController.select(any(Annotation.class))).thenReturn(null);
	}

	private static final Annotation DEFAULT_AT = new AnnotationLiteral<Default>() {
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Test of checkAccessTopic method, of class SessionManager.
	 *
	 * @throws java.lang.IllegalAccessException
	 */
	@Test(expected = IllegalAccessException.class)
	public void testCheckAccessGlobalTopic() throws IllegalAccessException {
		FakeCDI<JsTopicAccessController> globalTAC = new FakeCDI();
		JsTopicAccessController jtac = mock(JsTopicAccessController.class);
		globalTAC.add(jtac);
		doThrow(IllegalAccessException.class).when(jtac).checkAccess(any(Session.class), anyString());
		when(topicAccessController.select(DEFAULT_AT)).thenReturn(globalTAC);
		Session session = mock(Session.class);
		instance.checkAccessTopic(session, TOPIC1);
	}

	/**
	 * Test of checkAccessTopic method, of class SessionManager.
	 *
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testCheckAccessOkGlobalTopic() throws IllegalAccessException {
		FakeCDI<JsTopicAccessController> globalTAC = new FakeCDI();
		JsTopicAccessController jtac = mock(JsTopicAccessController.class);
		globalTAC.add(jtac);
		doNothing().when(jtac).checkAccess(any(Session.class), anyString());
		when(topicAccessController.select(DEFAULT_AT)).thenReturn(globalTAC);
		Session session = mock(Session.class);
		instance.checkAccessTopic(session, TOPIC1);
	}

	/**
	 * Test of checkAccessTopic method, of class SessionManager.
	 *
	 * @throws java.lang.IllegalAccessException
	 */
	@Test(expected = IllegalAccessException.class)
	public void testCheckAccessSpecificTopic() throws IllegalAccessException {
		FakeCDI<JsTopicAccessController> topic1TAC = new FakeCDI();
		JsTopicAccessController jtac = mock(JsTopicAccessController.class);
		topic1TAC.add(jtac);
		doThrow(IllegalAccessException.class).when(jtac).checkAccess(any(Session.class), eq(TOPIC1));
		when(topicAccessController.select(new JsTopicACAnnotationLiteral(TOPIC1))).thenReturn(topic1TAC);
		Session session = mock(Session.class);
		try {
			instance.checkAccessTopic(session, TOPIC2);
		} catch (IllegalAccessException e) {
			fail("Topic2 should be ok");
		}
		instance.checkAccessTopic(session, TOPIC1);
	}

	/**
	 * Test of checkAccessTopic method, of class SessionManager.
	 *
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testCheckAccessOkSpecificTopic() throws IllegalAccessException {
		FakeCDI<JsTopicAccessController> topic1TAC = new FakeCDI();
		JsTopicAccessController jtac = mock(JsTopicAccessController.class);
		topic1TAC.add(jtac);
		doNothing().when(jtac).checkAccess(any(Session.class), eq(TOPIC1));
		when(topicAccessController.select(new JsTopicACAnnotationLiteral(TOPIC1))).thenReturn(topic1TAC);
		Session session = mock(Session.class);
		instance.checkAccessTopic(session, TOPIC1);
	}

	/**
	 * Test of registerTopicSession method, of class SessionManager.
	 *
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testRegisterTopicSession() throws IllegalAccessException {
		System.out.println("registerTopicSession");
		Session session = mock(Session.class);
		when(session.isOpen()).thenReturn(true);
		int result = instance.registerTopicSession(null, session);
		assertThat(result).isEqualTo(0);

		result = instance.registerTopicSession("", session);
		assertThat(result).isEqualTo(0);

		result = instance.registerTopicSession(TOPIC1, session);
		assertThat(result).isEqualTo(1);
		assertThat(instance.getNumberSubscribers(TOPIC1)).isEqualTo(1);

		session = mock(Session.class);
		when(session.isOpen()).thenReturn(true);
		result = instance.registerTopicSession(TOPIC1, session);
		assertThat(result).isEqualTo(2);
		assertThat(instance.getNumberSubscribers(TOPIC1)).isEqualTo(2);

		result = instance.registerTopicSession(TOPIC1, session);
		assertThat(result).isEqualTo(2);
		assertThat(instance.getNumberSubscribers(TOPIC1)).isEqualTo(2);

		session = mock(Session.class);
		when(session.isOpen()).thenReturn(false);
		result = instance.registerTopicSession(TOPIC1, session);
		assertThat(result).isEqualTo(2);
		assertThat(instance.getNumberSubscribers(TOPIC1)).isEqualTo(2);
	}

	/**
	 * Test of unregisterTopicSession method, of class SessionManager.
	 *
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testUnregisterTopicSession() throws IllegalAccessException {
		System.out.println("unregisterTopicSession");
		Session session = mock(Session.class);
		when(session.isOpen()).thenReturn(true);
		int result = instance.unregisterTopicSession(null, session);
		assertThat(result).isEqualTo(0);

		result = instance.unregisterTopicSession("", session);
		assertThat(result).isEqualTo(0);

		result = instance.registerTopicSession(TOPIC1, session);
		assertThat(result).isEqualTo(1);
		assertThat(instance.getNumberSubscribers(TOPIC1)).isEqualTo(1);

		result = instance.unregisterTopicSession(TOPIC1, session);
		assertThat(result).isEqualTo(0);
		assertThat(instance.getNumberSubscribers(TOPIC1)).isEqualTo(0);
	}

	/**
	 * Test of isInconsistenceContext method, of class SessionManager.
	 */
	@Test
	public void testIsInconsistenceContext() {
		System.out.println("isInconsistenceContext");
		Session session = mock(Session.class);
		boolean result = instance.isInconsistenceContext(null, null);
		assertThat(result).isTrue();

		result = instance.isInconsistenceContext(null, session);
		assertThat(result).isTrue();

		result = instance.isInconsistenceContext("", null);
		assertThat(result).isTrue();

		result = instance.isInconsistenceContext("", session);
		assertThat(result).isTrue();

		result = instance.isInconsistenceContext("TOPIC", null);
		assertThat(result).isTrue();

		result = instance.isInconsistenceContext("TOPIC", session);
		assertThat(result).isFalse();
	}

	/**
	 * Test of unregisterTopicSession method, of class SessionManager.
	 *
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testUnregisterAllTopicSession() throws IllegalAccessException {
		System.out.println("unregisterAllTopicSession");
		Session session = mock(Session.class);
		when(session.isOpen()).thenReturn(true);
		int result = instance.registerTopicSession(TOPIC1, session);
		assertThat(result).isEqualTo(1);
		assertThat(instance.getNumberSubscribers(TOPIC1)).isEqualTo(1);

		Session session1 = mock(Session.class);
		when(session1.isOpen()).thenReturn(true);
		result = instance.registerTopicSession(TOPIC1, session1);
		assertThat(result).isEqualTo(2);
		assertThat(instance.getNumberSubscribers(TOPIC1)).isEqualTo(2);

		result = instance.registerTopicSession(TOPIC2, session);
		assertThat(result).isEqualTo(1);

		instance.unregisterTopicSession("ALL", session);
		assertThat(instance.getNumberSubscribers(TOPIC1)).isEqualTo(1);
		assertThat(instance.getNumberSubscribers(TOPIC2)).isEqualTo(0);

		result = instance.unregisterTopicSession(TOPIC1, session1);
		assertThat(result).isEqualTo(0);
	}

	@Test
	public void testRemoveSessionToSessions() {
		System.out.println("removeSessionToSessions");
		Session session = mock(Session.class);
		Collection<Session> sessions = new ArrayList<>();
		sessions.add(session);
		// sessions is null
		int result = instance.removeSessionToSessions(session, null);
		assertThat(result).isEqualTo(0);

		// sessions is empty
		result = instance.removeSessionToSessions(session, Collections.EMPTY_LIST);
		assertThat(result).isEqualTo(0);

		// remove session ok
		result = instance.removeSessionToSessions(session, sessions);
		assertThat(result).isEqualTo(1);
		assertThat(sessions).isEmpty();

		// remove session nok, session is not in sessions
		result = instance.removeSessionToSessions(session, sessions);
		assertThat(result).isEqualTo(0);
		assertThat(sessions).isEmpty();

		// remove session ok, and sessions not empty after
		sessions.add(session);
		sessions.add(mock(Session.class));
		result = instance.removeSessionToSessions(session, sessions);
		assertThat(result).isEqualTo(1);
		assertThat(sessions).hasSize(1);
	}

	/**
	 * Test of unregisterTopicSessions method, of class SessionManager.
	 *
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testUnregisterTopicSessions() throws IllegalAccessException {
		System.out.println("unregisterTopicSessions");
		Collection<Session> sessions = new ArrayList<>();

		Session session = mock(Session.class);
		when(session.isOpen()).thenReturn(true);
		int result = instance.registerTopicSession(TOPIC1, session);
		assertThat(result).isEqualTo(1);
		assertThat(instance.getNumberSubscribers(TOPIC1)).isEqualTo(1);
		sessions.add(session);

		Session session1 = mock(Session.class);
		when(session1.isOpen()).thenReturn(true);
		result = instance.registerTopicSession(TOPIC1, session1);
		assertThat(result).isEqualTo(2);
		assertThat(instance.getNumberSubscribers(TOPIC1)).isEqualTo(2);
		sessions.add(session1);

		boolean res = instance.unregisterTopicSessions(TOPIC1, sessions);
		assertThat(res).isTrue();
		assertThat(instance.getNumberSubscribers(TOPIC1)).isEqualTo(0);

		res = instance.unregisterTopicSessions(TOPIC1, null);
		assertThat(res).isFalse();
		res = instance.unregisterTopicSessions(TOPIC1, Collections.EMPTY_LIST);
		assertThat(res).isFalse();
	}

	/**
	 * Test of removeSessionsToTopic method, of class SessionManager.
	 *
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testRemoveSessionsToTopic() throws IllegalAccessException {
		System.out.println("removeSessionsToTopic");
		Collection<Session> sessions = new ArrayList<>();

		Session session = mock(Session.class);
		sessions.add(session);
		when(session.isOpen()).thenReturn(true);
		int result = instance.registerTopicSession(TOPIC1, session);
		assertThat(result).isEqualTo(1);
		assertThat(instance.getNumberSubscribers(TOPIC1)).isEqualTo(1);

		Session session1 = mock(Session.class);
		sessions.add(session1);
		when(session1.isOpen()).thenReturn(true);
		result = instance.registerTopicSession(TOPIC2, session1);
		assertThat(result).isEqualTo(1);
		assertThat(instance.getNumberSubscribers(TOPIC2)).isEqualTo(1);

		instance.removeSessionsToTopic(sessions);
		assertThat(instance.getNumberSubscribers(TOPIC1)).isEqualTo(0);
		assertThat(instance.getNumberSubscribers(TOPIC2)).isEqualTo(0);

		instance.removeSessionsToTopic(null);

		instance.removeSessionsToTopic(Collections.EMPTY_LIST);
	}

	/**
	 * Test of removeSessionToTopics method, of class SessionManager.
	 *
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testRemoveSessionToTopic() throws IllegalAccessException {
		System.out.println("removeSessionToTopic");
		Session session = mock(Session.class);
		when(session.isOpen()).thenReturn(true);
		int result = instance.registerTopicSession(TOPIC1, session);
		assertThat(result).isEqualTo(1);
		assertThat(instance.getNumberSubscribers(TOPIC1)).isEqualTo(1);

		result = instance.registerTopicSession(TOPIC2, session);
		assertThat(result).isEqualTo(1);
		assertThat(instance.getNumberSubscribers(TOPIC2)).isEqualTo(1);

		Session session1 = mock(Session.class);
		when(session1.isOpen()).thenReturn(true);
		result = instance.registerTopicSession(TOPIC2, session1);
		assertThat(result).isEqualTo(2);
		assertThat(instance.getNumberSubscribers(TOPIC2)).isEqualTo(2);
		result = instance.registerTopicSession(SUBTOPIC2, session1);
		assertThat(result).isEqualTo(1);
		assertThat(instance.getNumberSubscribers(SUBTOPIC2)).isEqualTo(1);

		RemoteEndpoint.Async async = mock(RemoteEndpoint.Async.class);
		when(session1.getAsyncRemote()).thenReturn(async);

		instance.removeSessionToTopics(session);
		instance.removeSessionToTopics(null);
		assertThat(instance.getNumberSubscribers(TOPIC1)).isEqualTo(0);
		assertThat(instance.getNumberSubscribers(TOPIC2)).isEqualTo(1);

		ArgumentCaptor<MessageToClient> captureMsg = ArgumentCaptor.forClass(MessageToClient.class);
		verify(async).sendObject(captureMsg.capture());
		MessageToClient msg = captureMsg.getValue();
		assertThat(msg.getType()).isEqualTo(MessageType.MESSAGE);
		assertThat(msg.getId()).isEqualTo(Constants.Topic.SUBSCRIBERS + Constants.Topic.COLON + TOPIC2);
		assertThat(msg.getResponse()).isEqualTo(1);
	}

	/**
	 * Test of sendSubscriptionEvent method, of class SessionManager.
	 */
	@Test
	public void testSendSubscriptionEvent() {
		System.out.println("sendSubscriptionEvent");
		Collection<Session> sessions = new ArrayList<>();
		Session session = mock(Session.class);
		RemoteEndpoint.Async async = mock(RemoteEndpoint.Async.class);
		sessions.add(session);
		when(session.isOpen()).thenReturn(Boolean.FALSE).thenReturn(Boolean.TRUE);
		when(session.getAsyncRemote()).thenReturn(async);
		doReturn(Collections.EMPTY_LIST).doReturn(sessions).when(instance).getSessionsForTopic(TOPIC1);

		instance.sendSubscriptionEvent(TOPIC1, 1);
		instance.sendSubscriptionEvent(TOPIC1, 2);
		instance.sendSubscriptionEvent(TOPIC1, 3);

		ArgumentCaptor<MessageToClient> captureMsg = ArgumentCaptor.forClass(MessageToClient.class);
		verify(async).sendObject(captureMsg.capture());
		MessageToClient msg = captureMsg.getValue();
		assertThat(msg.getType()).isEqualTo(MessageType.MESSAGE);
		assertThat(msg.getId()).isEqualTo(TOPIC1);
		assertThat(msg.getResponse()).isEqualTo(3);
	}
}
