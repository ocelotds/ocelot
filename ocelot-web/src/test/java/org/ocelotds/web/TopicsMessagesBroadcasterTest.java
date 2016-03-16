/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.SessionException;
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
import org.ocelotds.core.services.ArgumentServices;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.ocelotds.messaging.MessageType;
import org.ocelotds.objects.FakeCDI;
import org.ocelotds.security.JsTopicMessageController;
import org.ocelotds.security.NotRecipientException;
import org.ocelotds.security.UserContext;
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

	@Mock
	private ArgumentServices argumentServices;
	
	@Mock
	private RequestManager requestManager;
	
	@Spy
	Instance<JsTopicMessageController> topicMessageController = new FakeCDI<>();
	

	@InjectMocks
	@Spy
	private TopicsMessagesBroadcaster instance;
	
	/**
	 * Test of sendObjectToTopic method, of class TopicsMessagesBroadcaster.
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 * @throws java.lang.InstantiationException
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testSendObjectToTopicNotAnnotated() throws JsonMarshallingException, InstantiationException, IllegalAccessException {
		System.out.println("sendObjectToTopic");
		String payload = "PAYLOAD";
		EventMetadata metadata = mock(EventMetadata.class);
		InjectionPoint injectionPoint = mock(InjectionPoint.class);
		Annotated annotated = mock(Annotated.class);
		JsTopicEvent event = mock(JsTopicEvent.class);

		when(metadata.getInjectionPoint()).thenReturn(injectionPoint);
		when(injectionPoint.getAnnotated()).thenReturn(annotated);
		when(annotated.getAnnotation(JsTopicEvent.class)).thenReturn(null);

		// no JsTopicEvent
		instance.sendObjectToTopic(payload, metadata);
		verify(instance, never()).sendMessageToTopic(any(MessageToClient.class));
	}

	/**
	 * Test of sendObjectToTopic method, of class TopicsMessagesBroadcaster.
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 * @throws java.lang.InstantiationException
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testSendObjectToTopicWithoutMarshaller() throws JsonMarshallingException, InstantiationException, IllegalAccessException {
		System.out.println("sendObjectToTopic");
		String payload = "PAYLOAD";
		EventMetadata metadata = mock(EventMetadata.class);
		InjectionPoint injectionPoint = mock(InjectionPoint.class);
		Annotated annotated = mock(Annotated.class);
		JsTopicEvent jte = mock(JsTopicEvent.class);

		when(metadata.getInjectionPoint()).thenReturn(injectionPoint);
		when(injectionPoint.getAnnotated()).thenReturn(annotated);
		when(annotated.getAnnotation(JsTopicEvent.class)).thenReturn(jte);
		when(annotated.getAnnotation(JsonMarshaller.class)).thenReturn(null);
		when(jte.value()).thenReturn("TOPIC");

		// JsTopicEvent, no marshaller
		instance.sendObjectToTopic(payload, metadata);

		ArgumentCaptor<MessageToClient> captureMtC = ArgumentCaptor.forClass(MessageToClient.class);
		verify(instance).sendMessageToTopic(captureMtC.capture());
		
		MessageToClient mtc = captureMtC.getValue();
		assertThat(mtc.getResponse()).isEqualTo("PAYLOAD");
	}

	/**
	 * Test of sendObjectToTopic method, of class TopicsMessagesBroadcaster.
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 * @throws java.lang.InstantiationException
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testSendObjectToTopicWithMarshaller() throws JsonMarshallingException, InstantiationException, IllegalAccessException {
		System.out.println("sendObjectToTopic");
		System.out.println("sendObjectToTopic");
		String payload = "PAYLOAD";
		EventMetadata metadata = mock(EventMetadata.class);
		InjectionPoint injectionPoint = mock(InjectionPoint.class);
		Annotated annotated = mock(Annotated.class);
		JsTopicEvent jte = mock(JsTopicEvent.class);
		JsonMarshaller jm = mock(JsonMarshaller.class);

		when(metadata.getInjectionPoint()).thenReturn(injectionPoint);
		when(injectionPoint.getAnnotated()).thenReturn(annotated);
		when(annotated.getAnnotation(JsTopicEvent.class)).thenReturn(jte);
		when(annotated.getAnnotation(JsonMarshaller.class)).thenReturn(jm);
		when(jte.value()).thenReturn("TOPIC");
		when(argumentServices.getJsonResultFromSpecificMarshaller(eq(jm), eq("PAYLOAD"))).thenReturn("MARSHALLED");

		// JsTopicEvent, no marshaller
		instance.sendObjectToTopic(payload, metadata);

		ArgumentCaptor<MessageToClient> captureMtC = ArgumentCaptor.forClass(MessageToClient.class);
		verify(instance).sendMessageToTopic(captureMtC.capture());
		
		MessageToClient mtc = captureMtC.getValue();
		assertThat(mtc.getJson()).isEqualTo("MARSHALLED");
	}

	/**
	 * Test of sendObjectToTopic method, of class TopicsMessagesBroadcaster.
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 * @throws java.lang.InstantiationException
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testSendObjectToTopicWithMarshallerFail() throws JsonMarshallingException, InstantiationException, IllegalAccessException {
		System.out.println("sendObjectToTopic");
		String payload = "PAYLOAD";
		EventMetadata metadata = mock(EventMetadata.class);
		InjectionPoint injectionPoint = mock(InjectionPoint.class);
		Annotated annotated = mock(Annotated.class);
		JsTopicEvent event = mock(JsTopicEvent.class);

		when(metadata.getInjectionPoint()).thenReturn(injectionPoint);
		when(injectionPoint.getAnnotated()).thenReturn(annotated);
		when(annotated.getAnnotation(JsTopicEvent.class)).thenReturn(event);
		when(annotated.getAnnotation(JsonMarshaller.class)).thenReturn(mock(JsonMarshaller.class));
		when(argumentServices.getJsonResultFromSpecificMarshaller(any(JsonMarshaller.class), anyObject())).thenThrow(InstantiationException.class).thenThrow(IllegalAccessException.class).thenThrow(Throwable.class);
		when(event.value()).thenReturn("TOPIC");

		// JsTopicEvent, marshaller
		instance.sendObjectToTopic(payload, metadata);
		instance.sendObjectToTopic(payload, metadata);
		instance.sendObjectToTopic(payload, metadata);

		verify(instance, never()).sendMessageToTopic(any(MessageToClient.class));
	}

	/**
	 * Test of sendMessageToTopic method, of class TopicsMessagesBroadcaster.
	 * @throws javax.websocket.SessionException
	 */
	@Test
	public void testSendMessageToTopicNoSessions() throws SessionException {
		System.out.println("testSendMessageToTopicNoSessions");
		Collection<Session> sessions = new ArrayList<>();

		when(sessionManager.getSessionsForTopic(anyString())).thenReturn(null).thenReturn(sessions);

		int result = instance.sendMessageToTopic(new MessageToClient());
		assertThat(result).isEqualTo(0);

		result = instance.sendMessageToTopic(new MessageToClient());
		assertThat(result).isEqualTo(0);
	}

	/**
	 * Test of sendMessageToTopic method, of class TopicsMessagesBroadcaster.
	 */
	@Test
	public void testSendMessageToTopicFor2Opened1ClosedSession() throws SessionException {
		when(logger.isDebugEnabled()).thenReturn(Boolean.TRUE);
		System.out.println("testSendMessageToTopicFor2Opened1ClosedSession");
		Collection<Session> sessions = Arrays.asList(mock(Session.class), mock(Session.class), mock(Session.class));
		JsTopicMessageController jtmc = mock(JsTopicMessageController.class);

		doReturn(jtmc).when(instance).getJsTopicMessageController(anyString());
		doReturn(1).doThrow(SessionException.class).doReturn(1).when(instance).sendMtcToSession(any(Session.class), eq(jtmc), any(MessageToClient.class));
		when(sessionManager.getSessionsForTopic(anyString())).thenReturn(sessions);

		int result = instance.sendMessageToTopic(new MessageToClient());
		assertThat(result).isEqualTo(2);

		ArgumentCaptor<Collection> captureClosed = ArgumentCaptor.forClass(Collection.class);
		verify(sessionManager).removeSessionsToTopic(captureClosed.capture());
		assertThat(captureClosed.getValue()).hasSize(1);
	}
	
	/**
	 * Test of sendMtcToSession method, of class.
	 * @throws javax.websocket.SessionException
	 */
	@Test
	public void sendMtcToSessionNullTest() throws SessionException {
		System.out.println("sendMtcToSession");
		JsTopicMessageController jtmcmsgControl = mock(JsTopicMessageController.class);
		MessageToClient mtc = mock(MessageToClient.class);

		int result = instance.sendMtcToSession(null, jtmcmsgControl, mtc);
		assertThat(result).isEqualTo(0);
	}
	
	/**
	 * Test of sendMtcToSession method, of class.
	 * @throws javax.websocket.SessionException
	 */
	@Test(expected = SessionException.class)
	public void sendMtcToSessionCloseTest() throws SessionException {
		System.out.println("sendMtcToSession");
		Session session = mock(Session.class);
		JsTopicMessageController jtmcmsgControl = mock(JsTopicMessageController.class);
		MessageToClient mtc = mock(MessageToClient.class);

		when(session.isOpen()).thenReturn(Boolean.FALSE);

		instance.sendMtcToSession(session, jtmcmsgControl, mtc);
	}
	
	/**
	 * Test of sendMtcToSession method, of class.
	 * @throws javax.websocket.SessionException
	 * @throws org.ocelotds.security.NotRecipientException
	 */
	@Test
	public void sendMtcToSessionTest() throws SessionException, NotRecipientException {
		System.out.println("sendMtcToSession");
		Session session = mock(Session.class);
		RemoteEndpoint.Async async = mock(RemoteEndpoint.Async.class);
		when(session.isOpen()).thenReturn(true);
		when(session.getAsyncRemote()).thenReturn(async);
		JsTopicMessageController jtmcmsgControl = mock(JsTopicMessageController.class);
		MessageToClient mtc = mock(MessageToClient.class);

		when(session.isOpen()).thenReturn(Boolean.TRUE);
		when(requestManager.getUserContext(eq(session))).thenReturn(mock(UserContext.class));
		doNothing().doThrow(NotRecipientException.class).when(instance).checkMessageTopic(any(UserContext.class), eq(mtc), eq(jtmcmsgControl));
		
		int result = instance.sendMtcToSession(session, jtmcmsgControl, mtc);
		assertThat(result).isEqualTo(1);
		
		result = instance.sendMtcToSession(session, jtmcmsgControl, mtc);
		assertThat(result).isEqualTo(0);
	}
	
	/**
	 * Test of getJsTopicMessageController method, of class.
	 */
	@Test
	public void getJsTopicMessageControllerTest() {
		System.out.println("GetJsTopicMessageController");
		JsTopicMessageController result = instance.getJsTopicMessageController("TOPIC");
		assertThat(result).isNull();
		((FakeCDI<JsTopicMessageController>) topicMessageController).add(mock(JsTopicMessageController.class));
		result = instance.getJsTopicMessageController("TOPIC");
		assertThat(result).isNotNull();
	}
	
	/**
	 * Test of checkMessageTopic method, of class.
	 * @throws org.ocelotds.security.NotRecipientException
	 */
	@Test
	public void checkMessageTopicTest() throws NotRecipientException {
		System.out.println("checkMessageTopic");
		UserContext userContext = mock(UserContext.class);
		MessageToClient mtc = new MessageToClient();
		JsTopicMessageController jtmc = mock(JsTopicMessageController.class);
		doNothing().when(jtmc).checkRight(eq(userContext), eq(mtc));
		mtc.setType(MessageType.RESULT);
		instance.checkMessageTopic(userContext, mtc, jtmc);
		assertThat(mtc.getType()).isEqualTo(MessageType.MESSAGE);
		mtc.setType(MessageType.RESULT);
		instance.checkMessageTopic(userContext, mtc, jtmc);
		assertThat(mtc.getType()).isEqualTo(MessageType.MESSAGE);
	}

	/**
	 * Test of checkMessageTopic method, of class.
	 * @throws org.ocelotds.security.NotRecipientException
	 */
	@Test(expected = NotRecipientException.class)
	public void checkMessageTopicTestFail() throws NotRecipientException {
		System.out.println("checkMessageTopic");
		UserContext userContext = mock(UserContext.class);
		MessageToClient mtc = mock(MessageToClient.class);
		JsTopicMessageController jtmc = mock(JsTopicMessageController.class);
		doThrow(NotRecipientException.class).when(jtmc).checkRight(eq(userContext), eq(mtc));
		mtc.setType(MessageType.RESULT);
		instance.checkMessageTopic(userContext, mtc, jtmc);
	}
}
