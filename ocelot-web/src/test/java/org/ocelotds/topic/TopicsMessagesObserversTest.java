/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.topic;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.InjectionPoint;
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
import org.ocelotds.annotations.JsTopicEvent;
import org.ocelotds.marshalling.ArgumentServices;
import org.ocelotds.marshalling.exceptions.JsonMarshallerException;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class TopicsMessagesObserversTest {

	private final Object PAYLOAD = "PAYLOAD";
	private final String TOPIC = "TOPIC";

	@Mock
	private Logger logger;

	@Mock
	private ArgumentServices argumentServices;

	@Mock
	TopicsMessagesBroadcaster topicsMessagesBroadcaster;

	@InjectMocks
	@Spy
	private TopicsMessagesObservers instance;

	/**
	 * Test of sendObjectToTopic method, of class TopicsMessagesBroadcaster.
	 *
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 */
	@Test
	public void testSendObjectToTopicNotAnnotated() throws JsonMarshallingException {
		System.out.println("sendObjectToTopic");
		EventMetadata metadata = mock(EventMetadata.class);
		InjectionPoint injectionPoint = mock(InjectionPoint.class);
		Annotated annotated = mock(Annotated.class);

		when(metadata.getInjectionPoint()).thenReturn(injectionPoint);
		when(injectionPoint.getAnnotated()).thenReturn(annotated);
		when(annotated.getAnnotation(JsTopicEvent.class)).thenReturn(null);

		// no JsTopicEvent
		instance.sendObjectToTopic(PAYLOAD, metadata);
		verify(instance, never()).sendMessageToTopic(any(MessageToClient.class));
	}

	/**
	 * Test of sendObjectToTopic method, of class TopicsMessagesBroadcaster.
	 *
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 */
	@Test
	public void testSendObjectToTopicWithoutMarshaller() throws JsonMarshallingException {
		System.out.println("sendObjectToTopic");
		EventMetadata metadata = mock(EventMetadata.class);
		InjectionPoint injectionPoint = mock(InjectionPoint.class);
		Annotated annotated = mock(Annotated.class);
		JsTopicEvent jte = mock(JsTopicEvent.class);

		when(metadata.getInjectionPoint()).thenReturn(injectionPoint);
		when(injectionPoint.getAnnotated()).thenReturn(annotated);
		when(annotated.getAnnotation(JsTopicEvent.class)).thenReturn(jte);
		when(annotated.getAnnotation(JsonMarshaller.class)).thenReturn(null);
		when(jte.value()).thenReturn(TOPIC);

		// JsTopicEvent, no marshaller
		instance.sendObjectToTopic(PAYLOAD, metadata);

		ArgumentCaptor<MessageToClient> captureMtC = ArgumentCaptor.forClass(MessageToClient.class);
		ArgumentCaptor<Object> captureObject = ArgumentCaptor.forClass(Object.class);
		verify(topicsMessagesBroadcaster).sendMessageToTopic(captureMtC.capture(), captureObject.capture());

		assertThat(captureMtC.getValue().getResponse()).isEqualTo(PAYLOAD);
		assertThat(captureObject.getValue()).isEqualTo(PAYLOAD);
	}

	/**
	 * Test of sendObjectToTopic method, of class TopicsMessagesBroadcaster.
	 *
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 */
	@Test
	public void testSendObjectToTopicJsonPayload() throws JsonMarshallingException {
		System.out.println("sendObjectToTopic");
		EventMetadata metadata = mock(EventMetadata.class);
		InjectionPoint injectionPoint = mock(InjectionPoint.class);
		Annotated annotated = mock(Annotated.class);
		JsTopicEvent jte = mock(JsTopicEvent.class);

		when(metadata.getInjectionPoint()).thenReturn(injectionPoint);
		when(injectionPoint.getAnnotated()).thenReturn(annotated);
		when(annotated.getAnnotation(JsTopicEvent.class)).thenReturn(jte);
		when(annotated.getAnnotation(JsonMarshaller.class)).thenReturn(null);
		when(jte.value()).thenReturn(TOPIC);
		when(jte.jsonPayload()).thenReturn(true);

		// JsTopicEvent, jsonPayload <,no marshaller
		instance.sendObjectToTopic(PAYLOAD, metadata);

		ArgumentCaptor<MessageToClient> captureMtC = ArgumentCaptor.forClass(MessageToClient.class);
		ArgumentCaptor<Object> captureObject = ArgumentCaptor.forClass(Object.class);
		verify(topicsMessagesBroadcaster).sendMessageToTopic(captureMtC.capture(), captureObject.capture());

		assertThat(captureMtC.getValue().getJson()).isEqualTo(PAYLOAD);
		assertThat(captureObject.getValue()).isEqualTo(PAYLOAD);
	}

	/**
	 * Test of sendObjectToTopic method, of class TopicsMessagesBroadcaster.
	 *
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 */
	@Test
	public void testSendObjectToTopicJsonPayloadFailed() throws JsonMarshallingException {
		System.out.println("sendObjectToTopic");
		EventMetadata metadata = mock(EventMetadata.class);
		InjectionPoint injectionPoint = mock(InjectionPoint.class);
		Annotated annotated = mock(Annotated.class);
		JsTopicEvent jte = mock(JsTopicEvent.class);

		when(metadata.getInjectionPoint()).thenReturn(injectionPoint);
		when(injectionPoint.getAnnotated()).thenReturn(annotated);
		when(annotated.getAnnotation(JsTopicEvent.class)).thenReturn(jte);
		when(annotated.getAnnotation(JsonMarshaller.class)).thenReturn(null);
		when(jte.value()).thenReturn(TOPIC);
		when(jte.jsonPayload()).thenReturn(true);

		// JsTopicEvent, jsonPayload <,no marshaller
		instance.sendObjectToTopic(new Long(5), metadata);

		verify(topicsMessagesBroadcaster, never()).sendMessageToTopic(any(MessageToClient.class), anyObject());
	}

	/**
	 * Test of sendObjectToTopic method, of class TopicsMessagesBroadcaster.
	 *
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 */
	@Test
	public void testSendObjectToTopicWithMarshaller() throws JsonMarshallingException, JsonMarshallerException {
		System.out.println("sendObjectToTopic");
		EventMetadata metadata = mock(EventMetadata.class);
		InjectionPoint injectionPoint = mock(InjectionPoint.class);
		Annotated annotated = mock(Annotated.class);
		JsTopicEvent jte = mock(JsTopicEvent.class);
		JsonMarshaller jm = mock(JsonMarshaller.class);

		when(metadata.getInjectionPoint()).thenReturn(injectionPoint);
		when(injectionPoint.getAnnotated()).thenReturn(annotated);
		when(annotated.getAnnotation(JsTopicEvent.class)).thenReturn(jte);
		when(annotated.getAnnotation(JsonMarshaller.class)).thenReturn(jm);
		when(jte.value()).thenReturn(TOPIC);
		when(argumentServices.getJsonResultFromSpecificMarshaller(eq(jm), eq(PAYLOAD))).thenReturn("MARSHALLED");

		// JsTopicEvent, no marshaller
		instance.sendObjectToTopic(PAYLOAD, metadata);

		ArgumentCaptor<MessageToClient> captureMtC = ArgumentCaptor.forClass(MessageToClient.class);
		ArgumentCaptor<Object> captureObject = ArgumentCaptor.forClass(Object.class);
		verify(topicsMessagesBroadcaster).sendMessageToTopic(captureMtC.capture(), captureObject.capture());

		assertThat(captureMtC.getValue().getJson()).isEqualTo("MARSHALLED");
		assertThat(captureObject.getValue()).isEqualTo(PAYLOAD);
	}

	/**
	 * Test of sendObjectToTopic method, of class TopicsMessagesBroadcaster.
	 *
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 */
	@Test
	public void testSendObjectToTopicWithMarshallerFail() throws JsonMarshallingException, JsonMarshallerException {
		System.out.println("sendObjectToTopic");
		EventMetadata metadata = mock(EventMetadata.class);
		InjectionPoint injectionPoint = mock(InjectionPoint.class);
		Annotated annotated = mock(Annotated.class);
		JsTopicEvent event = mock(JsTopicEvent.class);

		when(metadata.getInjectionPoint()).thenReturn(injectionPoint);
		when(injectionPoint.getAnnotated()).thenReturn(annotated);
		when(annotated.getAnnotation(JsTopicEvent.class)).thenReturn(event);
		when(annotated.getAnnotation(JsonMarshaller.class)).thenReturn(mock(JsonMarshaller.class));
		when(argumentServices.getJsonResultFromSpecificMarshaller(any(JsonMarshaller.class), anyObject())).thenThrow(JsonMarshallingException.class).thenThrow(JsonMarshallerException.class).thenThrow(Throwable.class);
		when(event.value()).thenReturn(TOPIC);

		// JsTopicEvent, marshaller
		instance.sendObjectToTopic(PAYLOAD, metadata);
		instance.sendObjectToTopic(PAYLOAD, metadata);
		instance.sendObjectToTopic(PAYLOAD, metadata);

		verify(topicsMessagesBroadcaster, never()).sendMessageToTopic(any(MessageToClient.class), anyObject());
	}
	
	/**
	 * Test of sendMessageToTopic method, of class.
	 */
	@Test
	public void sendMessageToTopicTest() {
		System.out.println("sendMessageToTopic");
		MessageToClient mtc = mock(MessageToClient.class);
		doReturn(PAYLOAD).when(instance).getPayload(eq(mtc));
		when(topicsMessagesBroadcaster.sendMessageToTopic(eq(mtc), eq(PAYLOAD))).thenReturn(1);
		int result = instance.sendMessageToTopic(mtc);
		assertThat(result).isEqualTo(1);
		verify(topicsMessagesBroadcaster).sendMessageToTopic(eq(mtc), eq(PAYLOAD));
	}

	/**
	 * Test of getPayload method, of class.
	 */
	@Test
	public void getPayloadTest() {
		System.out.println("getPayload");
		MessageToClient mtc = new MessageToClient();
		mtc.setResponse("RESPONSE");
		Object result = instance.getPayload(mtc);
		assertThat(result).isEqualTo("RESPONSE");
		mtc = new MessageToClient();
		mtc.setJson("JSON");
		result = instance.getPayload(mtc);
		assertThat(result).isEqualTo("JSON");
	}
}
