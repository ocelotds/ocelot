/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import java.util.ArrayList;
import java.util.Collection;
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
import org.ocelotds.marshalling.annotations.JsonMarshaller;
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
	public void testSendMessageToTopicFor1Session() {
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
		instance.sendMessageToTopic(msg);

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
		instance.sendMessageToTopic(msg);
		assertThat(msg.getType()).isEqualTo(MessageType.MESSAGE);
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
		instance.sendMessageToTopic(msg);
		assertThat(msg.getType()).isEqualTo(MessageType.MESSAGE);
	}

	/**
	 * Test of sendObjectToTopic method, of class TopicsMessagesBroadcaster.
	 */
	@Test
	public void sendObjectToTopicWithoutMarshaller() {
		System.out.println("sendObjectToTopic");
		String payload = "PAYLOAD";
		EventMetadata metadata = mock(EventMetadata.class);
		InjectionPoint injectionPoint = mock(InjectionPoint.class);
		Annotated annotated = mock(Annotated.class);
		JsTopicEvent event = mock(JsTopicEvent.class);

		when(metadata.getInjectionPoint()).thenReturn(injectionPoint);
		when(injectionPoint.getAnnotated()).thenReturn(annotated);
		when(annotated.getAnnotation(JsTopicEvent.class)).thenReturn(null).thenReturn(event);
		when(annotated.getAnnotation(JsonMarshaller.class)).thenReturn(null);
		when(event.value()).thenReturn("TOPIC");

		instance.sendObjectToTopic(payload, metadata);

		instance.sendObjectToTopic(payload, metadata);

		ArgumentCaptor<MessageToClient> captureMtC = ArgumentCaptor.forClass(MessageToClient.class);
		verify(instance).sendMessageToTopic(captureMtC.capture());
		
		MessageToClient value = captureMtC.getValue();
		String json = value.toJson();
		assertThat(json).isEqualTo("{\"type\":\"MESSAGE\",\"id\":\"TOPIC\",\"deadline\":0,\"response\":\"PAYLOAD\"}");
	}

	/**
	 * Test of sendObjectToTopic method, of class TopicsMessagesBroadcaster.
	 */
	@Test
	public void sendObjectToTopicWithMarshaller() {
		System.out.println("sendObjectToTopic");
		String payload = "PAYLOAD";
		EventMetadata metadata = mock(EventMetadata.class);
		InjectionPoint injectionPoint = mock(InjectionPoint.class);
		Annotated annotated = mock(Annotated.class);
		JsTopicEvent event = mock(JsTopicEvent.class);
		JsonMarshaller jm = mock(JsonMarshaller.class);

		when(metadata.getInjectionPoint()).thenReturn(injectionPoint);
		when(injectionPoint.getAnnotated()).thenReturn(annotated);
		when(annotated.getAnnotation(JsTopicEvent.class)).thenReturn(event);
		when(annotated.getAnnotation(JsonMarshaller.class)).thenReturn(jm);
		doReturn(JsMarshaller.class).when(jm).value();
		when(event.value()).thenReturn("TOPIC");

		instance.sendObjectToTopic(payload, metadata);

		ArgumentCaptor<MessageToClient> captureMtC = ArgumentCaptor.forClass(MessageToClient.class);
		verify(instance).sendMessageToTopic(captureMtC.capture());
		
		MessageToClient value = captureMtC.getValue();
		String json = value.toJson();
		assertThat(json).isEqualTo("{\"type\":\"MESSAGE\",\"id\":\"TOPIC\",\"deadline\":0,\"response\":\"payload\"}");
	}
}
