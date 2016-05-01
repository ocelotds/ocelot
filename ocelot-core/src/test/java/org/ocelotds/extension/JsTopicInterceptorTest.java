/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.annotations.JsTopic;
import org.ocelotds.annotations.JsTopicName;
import org.ocelotds.messaging.MessageToClient;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class JsTopicInterceptorTest {

	@Mock
	Event<MessageToClient> wsEvent;

	@Mock
	Logger logger;

	@InjectMocks
	@Spy
	JsTopicInterceptor instance;

	/**
	 * Test of processJsTopic method, of class JsTopicInterceptor.
	 *
	 * @throws java.lang.Exception
	 */
	@Test
	public void testProcessJsTopic() throws Exception {
		System.out.println("processJsTopic");
		InvocationContext ctx = mock(InvocationContext.class);
		doReturn("STATICTOPIC").doReturn("").doReturn(null).when(instance).getStaticTopic(any(Method.class));
		doReturn("DYNTOPIC").when(instance).getDynamicTopic(any(Method.class), any(Object[].class));
		instance.processJsTopic(ctx);
		instance.processJsTopic(ctx);
		instance.processJsTopic(ctx);
		ArgumentCaptor<String> captors = ArgumentCaptor.forClass(String.class);
		verify(instance, times(3)).proceedAndSendMessage(eq(ctx), captors.capture());
		List<String> topics = captors.getAllValues();
		assertThat(topics.get(0)).isEqualTo("STATICTOPIC");
		assertThat(topics.get(1)).isEqualTo("DYNTOPIC");
		assertThat(topics.get(2)).isEqualTo("DYNTOPIC");
	}

	/**
	 * Test of processJsTopic method, of class JsTopicInterceptor.
	 *
	 * @throws java.lang.Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testProcessJsTopicFail() throws Exception {
		System.out.println("processJsTopic");
		InvocationContext ctx = mock(InvocationContext.class);
		doReturn(null).when(instance).getStaticTopic(any(Method.class));
		doReturn(null).when(instance).getDynamicTopic(any(Method.class), any(Object[].class));
		instance.processJsTopic(ctx);
	}

	/**
	 * Test of processJsTopic method, of class JsTopicInterceptor.
	 *
	 * @throws java.lang.Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testProcessJsTopicFail2() throws Exception {
		System.out.println("processJsTopic");
		InvocationContext ctx = mock(InvocationContext.class);
		doReturn(null).when(instance).getStaticTopic(any(Method.class));
		doReturn("").when(instance).getDynamicTopic(any(Method.class), any(Object[].class));
		instance.processJsTopic(ctx);
	}

	/**
	 * Test of getStaticTopic method, of class JsTopicInterceptor.
	 *
	 * @throws java.lang.Exception
	 */
	@Test
	public void testGetStaticTopic() throws Exception {
		System.out.println("getStaticTopic");
		Method methodAnnotated = this.getClass().getMethod("methodWithJsTopicAndWithTopicName");
		Method methodNotAnnotated = this.getClass().getMethod("methodWithoutJsTopic");
		String result = instance.getStaticTopic(null);
		assertThat(result).isEqualTo(null);
		result = instance.getStaticTopic(methodNotAnnotated);
		assertThat(result).isEqualTo(null);
		result = instance.getStaticTopic(methodAnnotated);
		assertThat(result).isEqualTo("TOPICNAME");
	}

	/**
	 * Test of getDynamicTopic method, of class JsTopicInterceptor.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testGetDynamicTopic() throws Exception {
		System.out.println("getDynamicTopic");
		Method method = this.getClass().getMethod("methodWithJsTopicAndWithJsTopicName", new Class[] {Integer.class, String.class});
		Object[] parameters = new Object[] {5, "TOPICNAME"};
		String result = instance.getDynamicTopic(method, parameters);
		assertThat(result).isEqualTo("TOPICNAME");
		method = this.getClass().getMethod("methodWithJsTopicAndWithoutJsTopicName", new Class[] {Integer.class, String.class});
		result = instance.getDynamicTopic(method, parameters);
		assertThat(result).isNull();
	}

	/**
	 * Test of getJsTopicNameAnnotation method, of class JsTopicInterceptor.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testGetJsTopicNameAnnotation() throws Exception {
		System.out.println("getJsTopicNameAnnotation");
		JsTopicName jtn = new JsTopicNameImpl("", "");
		Annotation[] parameterAnnotations = new Annotation[] {jtn};
		JsTopicName result = instance.getJsTopicNameAnnotation(parameterAnnotations);
		assertThat(result).isEqualTo(jtn);
		
		parameterAnnotations = new Annotation[] {INJECT_AT, jtn, ANY_AT};
		result = instance.getJsTopicNameAnnotation(parameterAnnotations);
		assertThat(result).isEqualTo(jtn);

		parameterAnnotations = new Annotation[] {INJECT_AT, ANY_AT};
		result = instance.getJsTopicNameAnnotation(parameterAnnotations);
		assertThat(result).isNull();
	}

	/**
	 * Test of computeTopic method, of class JsTopicInterceptor.
	 */
	@Test
	public void testComputeTopic() {
		System.out.println("computeTopic");
		String topic = "TOPIC";
		JsTopicName jsTopicName = new JsTopicNameImpl("", "");
		String result = instance.computeTopic(jsTopicName, topic);
		assertThat(result).isEqualTo("TOPIC");
		
		jsTopicName = new JsTopicNameImpl("PREFIX", "");
		result = instance.computeTopic(jsTopicName, topic);
		assertThat(result).isEqualTo("PREFIX:TOPIC");

		jsTopicName = new JsTopicNameImpl("", "POSTFIX");
		result = instance.computeTopic(jsTopicName, topic);
		assertThat(result).isEqualTo("TOPIC:POSTFIX");

		jsTopicName = new JsTopicNameImpl("PREFIX", "POSTFIX");
		result = instance.computeTopic(jsTopicName, topic);
		assertThat(result).isEqualTo("PREFIX:TOPIC:POSTFIX");
	}

	/**
	 * Test of proceedAndSendMessage method, of class JsTopicInterceptor.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testProceedAndSendMessage() throws Exception {
		System.out.println("proceedAndSendMessage");
		InvocationContext ctx = mock(InvocationContext.class);
		when(ctx.proceed()).thenReturn("RESULT");
		String topic = "";
		Object expResult = null;
		Object result = instance.proceedAndSendMessage(ctx, "TOPIC");
		ArgumentCaptor<MessageToClient> argument = ArgumentCaptor.forClass(MessageToClient.class);
		verify(wsEvent).fire(argument.capture());
		assertThat(result).isEqualTo("RESULT");
		MessageToClient mtc = argument.getValue();
		assertThat(mtc.getId()).isEqualTo("TOPIC");
		assertThat(mtc.getResponse()).isEqualTo("RESULT");
	}

	@JsTopic("TOPICNAME")
	public void methodWithJsTopicAndWithTopicName() {
	}

	public void methodWithoutJsTopic() {

	}

	@JsTopic
	public void methodWithJsTopicAndWithJsTopicName(Integer i, @JsTopicName String topic) {
	}
	@JsTopic
	public void methodWithJsTopicAndWithoutJsTopicName(Integer i, String topic) {
	}
	
	private static final Annotation INJECT_AT = new AnnotationLiteral<Inject>() {};
	private static final Annotation ANY_AT = new AnnotationLiteral<Any>() {};

	class JsTopicNameImpl extends AnnotationLiteral<JsTopicName> implements JsTopicName {
		
		final String prefix;
		final String postfix;

		public JsTopicNameImpl(String prefix, String postfix) {
			this.prefix = prefix;
			this.postfix = postfix;
		}
		
		@Override
		public String prefix() {
			return prefix;
		}

		@Override
		public String postfix() {
			return postfix;
		}
		
	}
}
