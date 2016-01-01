/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ocelotds.core.SessionManager;
import org.ocelotds.messaging.MessageToClient;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.annotations.JsTopicEvent;
import org.ocelotds.literals.JsonMarshallerLiteral;
import org.ocelotds.marshallers.LocaleMarshaller;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.ocelotds.messaging.MessageType;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class TopicsMessagesBroadcasterTest {
	
	@Mock
	private Logger logger;

	@Mock
	private SessionManager sessionManager;

	@InjectMocks
	@Spy
	private TopicsMessagesBroadcaster instance;
	
	/**
	 * Test of sendMessageToTopic method, of class TopicsMessagesBroadcaster.
	 */
	@Test
	public void testSendMessageToTopicFor1OpenedSession() {
		when(logger.isDebugEnabled()).thenReturn(Boolean.TRUE);
		System.out.println("sendMessageToTopicFor1Session");
		Collection<Session> sessions = new ArrayList<>();
		Session session1 = mock(Session.class);
		RemoteEndpoint.Async async = mock(RemoteEndpoint.Async.class);
		when(session1.isOpen()).thenReturn(true);
		when(session1.getAsyncRemote()).thenReturn(async);
		sessions.add(session1);
		Session session2 = mock(Session.class);
		sessions.add(session2);
		when(sessionManager.getSessionsForTopic(anyString())).thenReturn(sessions);

		MessageToClient msg = new MessageToClient();
		String id = UUID.randomUUID().toString();
		msg.setId(id);
		String expResult = "RESULT";
		msg.setResult(expResult);
		int result = instance.sendMessageToTopic(msg);
		assertThat(result).isEqualTo(1);

		ArgumentCaptor<MessageToClient> captureMsg = ArgumentCaptor.forClass(MessageToClient.class);
		verify(async).sendObject(captureMsg.capture());
		assertThat(captureMsg.getValue().getResponse()).isEqualTo(expResult);
		assertThat(captureMsg.getValue().getId()).isEqualTo(id);
		assertThat(captureMsg.getValue().getType()).isEqualTo(MessageType.MESSAGE);
		assertThat(captureMsg.getValue().getDeadline()).isEqualTo(0L);
	}
	
	/**
	 * Test of sendMessageToTopic method, of class TopicsMessagesBroadcaster.
	 */
	@Test
	public void testSendMessageToTopicFor2Session() {
		when(logger.isDebugEnabled()).thenReturn(Boolean.TRUE);
		System.out.println("sendMessageToTopicFor1Session");
		Collection<Session> sessions = new ArrayList<>();
		Session session1 = mock(Session.class);
		RemoteEndpoint.Async async = mock(RemoteEndpoint.Async.class);
		when(session1.isOpen()).thenReturn(true);
		when(session1.getAsyncRemote()).thenReturn(async);
		sessions.add(session1);
		Session session2 = mock(Session.class);
		when(session2.isOpen()).thenReturn(true);
		when(session2.getAsyncRemote()).thenReturn(async);
		sessions.add(session2);
		when(sessionManager.getSessionsForTopic(anyString())).thenReturn(sessions);

		MessageToClient msg = new MessageToClient();
		String id = UUID.randomUUID().toString();
		msg.setId(id);
		String expResult = "RESULT";
		msg.setResult(expResult);
		int result = instance.sendMessageToTopic(msg);
		assertThat(result).isEqualTo(2);
	}

	/**
	 * Test of sendMessageToTopic method, of class TopicsMessagesBroadcaster.
	 */
	@Test
	public void testSendMessageToTopicFor0Session() {
		System.out.println("sendMessageToTopicFor0Session");
		Collection<Session> sessions = new ArrayList<>();
		sessions.add(null);
		when(sessionManager.getSessionsForTopic(anyString())).thenReturn(sessions);

		MessageToClient msg = new MessageToClient();
		String id = UUID.randomUUID().toString();
		msg.setId(id);
		String expResult = "RESULT";
		msg.setResult(expResult);
		int result = instance.sendMessageToTopic(msg);
		assertThat(msg.getType()).isEqualTo(MessageType.MESSAGE);
		assertThat(result).isEqualTo(0);
	}

	/**
	 * Test of sendMessageToTopic method, of class TopicsMessagesBroadcaster.
	 */
	@Test
	public void testSendMessageToTopicForNullSession() {
		System.out.println("sendMessageToTopicForNullSession");
		Collection<Session> sessions = new ArrayList<>();
		when(sessionManager.getSessionsForTopic(anyString())).thenReturn(sessions);

		MessageToClient msg = new MessageToClient();
		String id = UUID.randomUUID().toString();
		msg.setId(id);
		String expResult = "RESULT";
		msg.setResult(expResult);
		int result = instance.sendMessageToTopic(msg);
		assertThat(msg.getType()).isEqualTo(MessageType.MESSAGE);
		assertThat(result).isEqualTo(0);
	}

	/**
	 * Test of sendMessageToTopic method, of class TopicsMessagesBroadcaster.
	 */
	@Test
	public void testSendMessageToTopicForNullSessions() {
		System.out.println("sendMessageToTopicForNullSession");
		when(sessionManager.getSessionsForTopic(anyString())).thenReturn(null);

		MessageToClient msg = new MessageToClient();
		String id = UUID.randomUUID().toString();
		msg.setId(id);
		String expResult = "RESULT";
		msg.setResult(expResult);
		int result = instance.sendMessageToTopic(msg);
		assertThat(msg.getType()).isEqualTo(MessageType.MESSAGE);
		assertThat(result).isEqualTo(0);
	}

	/**
	 * Test of sendObjectToTopic method, of class TopicsMessagesBroadcaster.
	 */
	@Test
	public void testSendObjectToTopicWithoutMarshaller() {
		System.out.println("sendObjectToTopic");
		String payload = "PAYLOAD";
		EventMetadata metadata = mock(EventMetadata.class);
		InjectionPoint injectionPoint = mock(InjectionPoint.class);
		Annotated annotated = mock(Annotated.class);
		JsTopicEvent event = mock(JsTopicEvent.class);

		when(metadata.getInjectionPoint()).thenReturn(injectionPoint);
		when(injectionPoint.getAnnotated()).thenReturn(annotated);
		when(annotated.getAnnotation(JsTopicEvent.class)).thenReturn(null).thenReturn(event);
		when(annotated.isAnnotationPresent(JsonMarshaller.class)).thenReturn(true).thenReturn(false);
		doReturn("\"payload\"").when(instance).getJsonFromMarshaller(anyObject(), any(JsonMarshaller.class));
		when(event.value()).thenReturn("TOPIC");

		// no JsTopicEvent
		instance.sendObjectToTopic(payload, metadata);

		// JsTopicEvent, marshaller
		instance.sendObjectToTopic(payload, metadata);

		// JsTopicEvent, no marshaller
		instance.sendObjectToTopic(payload, metadata);

		ArgumentCaptor<MessageToClient> captureMtC = ArgumentCaptor.forClass(MessageToClient.class);
		verify(instance, times(2)).sendMessageToTopic(captureMtC.capture());
		
		List<MessageToClient> allValues = captureMtC.getAllValues();
		MessageToClient value = allValues.get(0);
		String json = value.toJson();
		assertThat(json).isEqualTo("{\"type\":\"MESSAGE\",\"id\":\"TOPIC\",\"deadline\":0,\"response\":\"payload\"}");
		value = allValues.get(1);
		json = value.toJson();
		assertThat(json).isEqualTo("{\"type\":\"MESSAGE\",\"id\":\"TOPIC\",\"deadline\":0,\"response\":\"PAYLOAD\"}");
	}
	
	@Test
	public void testGetJsonFromMarshaller() {
		Locale locale = Locale.FRANCE;
		String result = instance.getJsonFromMarshaller(locale, new JsonMarshallerLiteral(LocaleMarshaller.class));
		assertThat(result).isEqualTo("{\"country\":\"FR\",\"language\":\"fr\"}");
	}
	
	@Test
	public void testGetJsonFromMarshallerFail1() {
		Locale locale = Locale.FRANCE;
		String result = instance.getJsonFromMarshaller(locale, new JsonMarshallerLiteral(JsMarshaller.class));
		assertThat(result).isEqualTo(null);
	}

	@Test
	public void testGetJsonFromMarshallerFail2() {
		Locale locale = Locale.FRANCE;
		String result = instance.getJsonFromMarshaller(locale, new JsonMarshallerLiteral(BadMarshaller.class));
		assertThat(result).isEqualTo(null);
	}

	@Test
	public void testGetJsonFromMarshallerFail3() {
		Locale locale = Locale.FRANCE;
		String result = instance.getJsonFromMarshaller(locale, new JsonMarshallerLiteral(BadMarshaller2.class));
		assertThat(result).isEqualTo(null);
	}

	private class BadMarshaller implements org.ocelotds.marshalling.JsonMarshaller {
		
		public BadMarshaller(String s) {
			
		}

		@Override
		public String toJson(Object obj) throws JsonMarshallingException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
		
	}
	
	private class BadMarshaller2 implements org.ocelotds.marshalling.JsonMarshaller {
		
		private BadMarshaller2() {
			
		}

		@Override
		public String toJson(Object obj) throws JsonMarshallingException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
		
	}

}
