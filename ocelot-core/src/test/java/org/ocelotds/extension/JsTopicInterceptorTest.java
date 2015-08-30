/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.extension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.enterprise.event.Event;
import javax.interceptor.InvocationContext;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ocelotds.messaging.MessageToClient;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import static org.assertj.core.api.Assertions.*;
import org.ocelotds.annotations.JsTopic;
import org.ocelotds.annotations.JsTopicName;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class JsTopicInterceptorTest {

	@Mock
	private Event<MessageToClient> wsEvent;
	@InjectMocks
	private final JsTopicInterceptor jsTopicInterceptor = new JsTopicInterceptor();

	/**
	 * If JsTopic is Present with value, so message id fired equals value
	 *
	 * @throws Exception
	 */
	@Test
	public void testProcessMethodWithJsTopicAndValue() throws Exception {
		System.out.println("testProcessMethodWithJsTopicAndValue");
		// Given
		Method method = this.getClass().getMethod("methodWithJsTopicAndWithTopicName");
		InvocationContext ctx = mock(InvocationContext.class);
		when(ctx.getMethod()).thenReturn(method);

		// When
		jsTopicInterceptor.processJsTopic(ctx);

		// Then
		ArgumentCaptor<MessageToClient> argument = ArgumentCaptor.forClass(MessageToClient.class);
		verify(wsEvent).fire(argument.capture());
		assertThat(argument.getValue().getId()).isEqualTo("TOPICNAME");
	}

	/**
	 * If JsTopic is Present with value, so message id fired equals value
	 *
	 * @throws Exception
	 */
	@Test
	public void testProcessMethodWithJsTopicAndJsTopicName() throws Exception {
		System.out.println("testProcessMethodWithJsTopicAndValue");
		// Given
		Method method = this.getClass().getMethod("methodWithJsTopicAndWithJsTopicName", new Class<?>[] {Integer.class, String.class});
		InvocationContext ctx = mock(InvocationContext.class);
		when(ctx.getMethod()).thenReturn(method);
		Object[] parameters = new Object[] {5, "OTHERTOPIC"};
		when(ctx.getParameters()).thenReturn(parameters);

		// When
		jsTopicInterceptor.processJsTopic(ctx);

		// Then
		ArgumentCaptor<MessageToClient> argument = ArgumentCaptor.forClass(MessageToClient.class);
		verify(wsEvent).fire(argument.capture());
		assertThat(argument.getValue().getId()).isEqualTo("OTHERTOPIC");
	}

	/**
	 * If JsTopic is Present with value, so message id fired equals value
	 *
	 * @throws Exception
	 */
	@Test
	public void testProcessMethodWithJsTopicAndJsTopicNameWithPrefix() throws Exception {
		System.out.println("testProcessMethodWithJsTopicAndJsTopicNameWithPrefix");
		// Given
		Method method = this.getClass().getMethod("methodWithJsTopicAndWithJsTopicName", new Class<?>[] {String.class});
		InvocationContext ctx = mock(InvocationContext.class);
		when(ctx.getMethod()).thenReturn(method);
		Object[] parameters = new Object[] {"OTHERTOPIC"};
		when(ctx.getParameters()).thenReturn(parameters);

		// When
		jsTopicInterceptor.processJsTopic(ctx);

		// Then
		ArgumentCaptor<MessageToClient> argument = ArgumentCaptor.forClass(MessageToClient.class);
		verify(wsEvent).fire(argument.capture());
		assertThat(argument.getValue().getId()).isEqualTo("number:OTHERTOPIC");
	}

	/**
	 * If JsTopic is present but without value and without JsTopicName on argument so exception
	 *
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testProcessMethodWithJsTopicWithoutValue() throws Exception {
		System.out.println("testProcessMethodWithJsTopicWithoutValue");
		Method method = this.getClass().getMethod("methodWithJsTopicAndWithoutTopicName");
		final InvocationContext ctx = mock(InvocationContext.class);
		when(ctx.getMethod()).thenReturn(method);
		Object result = jsTopicInterceptor.processJsTopic(ctx);
	}

	private Method createMockClassMethod(Class returnType) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Method method = mock(Method.class);
		Field field = Method.class.getDeclaredField("returnType");
		field.setAccessible(true);
		field.set(method, returnType);
		return method;
	}

	@JsTopic
	public void methodWithJsTopicAndWithoutTopicName() {

	}

	@JsTopic("TOPICNAME")
	public void methodWithJsTopicAndWithTopicName() {

	}

	@JsTopic
	public void methodWithJsTopicAndWithJsTopicName(Integer i, @JsTopicName String topic) {

	}

	@JsTopic
	public void methodWithJsTopicAndWithJsTopicName(@JsTopicName(prefix = "number") String topic) {

	}
}
