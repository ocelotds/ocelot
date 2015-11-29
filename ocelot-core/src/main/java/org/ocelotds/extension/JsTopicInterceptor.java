/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.extension;

import org.ocelotds.annotations.JsTopic;
import org.ocelotds.annotations.JsTopicName;
import org.ocelotds.messaging.MessageEvent;
import org.ocelotds.messaging.MessageToClient;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotLogger;
import org.slf4j.Logger;

/**
 * This class transform a sipmle method to chanel for topic
 * @author hhfrancois
 */
@Interceptor
@JsTopic
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
		JsTopic jsTopic = method.getAnnotation(JsTopic.class);
		String topic = jsTopic.value();
		if (null == topic || topic.isEmpty()) {
			Object[] parameters = ctx.getParameters();
			int idx = 0;
			Annotation[][] parametersAnnotations = method.getParameterAnnotations();
			for (Annotation[] parameterAnnotations : parametersAnnotations) {
				for (Annotation parameterAnnotation : parameterAnnotations) {
					if (parameterAnnotation.annotationType().equals(JsTopicName.class)) {
						JsTopicName jsTopicName = (JsTopicName) parameterAnnotation;
						topic = (String) parameters[idx];
						if (!jsTopicName.prefix().isEmpty()) {
							topic = jsTopicName.prefix() + Constants.Topic.COLON + topic;
						}
						if (!jsTopicName.postfix().isEmpty()) {
							topic = topic + Constants.Topic.COLON + jsTopicName.postfix();
						}
						break;
					}
				}
				idx++;
			}
		}
		if (null == topic || topic.isEmpty()) {
			throw new IllegalArgumentException("Topic name can't be empty.");
		}
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(topic);
		Object result = ctx.proceed();
		messageToClient.setResponse(result);
		wsEvent.fire(messageToClient);
		return result;
	}
}
