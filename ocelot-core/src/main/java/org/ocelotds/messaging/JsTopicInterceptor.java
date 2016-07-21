/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.messaging;

import org.ocelotds.annotations.JsTopic;
import org.ocelotds.annotations.JsTopicName;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import javax.annotation.Priority;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotLogger;
import org.slf4j.Logger;

/**
 * This class transform a simple method to chanel for topic
 *
 * @author hhfrancois
 */
@JsTopic
@Priority(0)
@Interceptor
public class JsTopicInterceptor implements Serializable {

	private static final long serialVersionUID = -849762977471230875L;

	@Inject
	@OcelotLogger
	private transient Logger logger;

	@Inject
	@MessageEvent
	transient Event<MessageToClient> wsEvent;

	/**
	 *
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	@AroundInvoke
	public Object processJsTopic(InvocationContext ctx) throws Exception {
		Method method = ctx.getMethod();
		String topic = getStaticTopic(method);
		boolean jsonPayload = isJsonPayload(method);
		if (null == topic || topic.isEmpty()) {
			topic = getDynamicTopic(method, ctx.getParameters());
		}
		if (null != topic && !topic.isEmpty()) { // topic name is specify
			return proceedAndSendMessage(ctx, topic, jsonPayload);
		}
		throw new IllegalArgumentException("Topic name can't be empty.");
	}

	/**
	 * Get static topicname, from annotation @JsTopic on method considerated
	 *
	 * @param method
	 * @return
	 */
	String getStaticTopic(Method method) {
		if(null == method || !method.isAnnotationPresent(JsTopic.class) ) {
			return null;
		}
		JsTopic jsTopic = method.getAnnotation(JsTopic.class);
		return jsTopic.value();
	}

	/**
	 * Get dynamic topicname from parameter annotated @JsTopicName
	 *
	 * @param method
	 * @param parameters
	 * @return
	 */
	String getDynamicTopic(Method method, Object[] parameters) {
		int idx = 0; // synchronise the two arrays, array of parameter and array of annotations
		Annotation[][] parametersAnnotations = method.getParameterAnnotations();
		for (Annotation[] parameterAnnotations : parametersAnnotations) { // for each parameter
			JsTopicName jsTopicName = getJsTopicNameAnnotation(parameterAnnotations);
			if (jsTopicName != null) {
				return computeTopic(jsTopicName, parameters[idx].toString());
			}
			idx++;
		}
		return null;
	}

	/**
	 * The topic receive directly payload in json
	 *
	 * @param method
	 * @return
	 */
	boolean isJsonPayload(Method method) {
		if(null == method || !method.isAnnotationPresent(JsTopic.class) ) {
			return false;
		}
		JsTopic jsTopic = method.getAnnotation(JsTopic.class);
		return jsTopic.jsonPayload();
	}

	/**
	 * Get JsTopicName annotation
	 *
	 * @param parameterAnnotations
	 * @param topic
	 * @return
	 */
	JsTopicName getJsTopicNameAnnotation(Annotation[] parameterAnnotations) {
		for (Annotation parameterAnnotation : parameterAnnotations) {
			if (parameterAnnotation.annotationType().equals(JsTopicName.class)) {
				return (JsTopicName) parameterAnnotation;
			}
		}
		return null;
	}

	/**
	 * Compute full topicname from jsTopicName.prefix, topic and jsTopicName.postfix
	 * @param jsTopicName
	 * @param topic
	 * @return
	 */
	String computeTopic(JsTopicName jsTopicName, String topic) {
		StringBuilder result = new StringBuilder();
		if (!jsTopicName.prefix().isEmpty()) {
			result.append(jsTopicName.prefix()).append(Constants.Topic.COLON);
		}
		result.append(topic);
		if (!jsTopicName.postfix().isEmpty()) {
			result.append(Constants.Topic.COLON).append(jsTopicName.postfix());
		}
		return result.toString();
	}

	/**
	 * Proceed the method and send MessageToClient to topic
	 * @param ctx
	 * @param topic
	 * @return
	 * @throws Exception
	 */
	Object proceedAndSendMessage(InvocationContext ctx, String topic, boolean jsonPayload) throws Exception {
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(topic);
		Object result = ctx.proceed();
		if(jsonPayload) {
			if(!String.class.isInstance(result)) {
				throw new UnsupportedOperationException("Method annotated JsTopic(jsonPayload=true) must return String type and correct Json.");
			}
			messageToClient.setJson((String) result);
		} else {
			messageToClient.setResponse(result);
		}
		wsEvent.fire(messageToClient);
		return result;
	}
}
