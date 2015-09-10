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
import java.util.Objects;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import org.ocelotds.Constants;
import org.ocelotds.logger.OcelotLogger;
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
		if (Objects.isNull(topic) || topic.isEmpty()) {
			Object[] parameters = ctx.getParameters();
			int idx = 0;
			Annotation[][] parametersAnnotations = method.getParameterAnnotations();
			for (Annotation[] parameterAnnotations : parametersAnnotations) {
				for (Annotation parameterAnnotation : parameterAnnotations) {
					if (parameterAnnotation.annotationType().equals(JsTopicName.class)) {
						JsTopicName jsTopicName = (JsTopicName) parameterAnnotation;
						if (!jsTopicName.prefix().isEmpty()) {
							topic = jsTopicName.prefix() + Constants.Topic.COLON + parameters[idx];
						} else {
							topic = (String) parameters[idx];
						}
						break;
					}
				}
				idx++;
			}
		}
		if (Objects.isNull(topic) || topic.isEmpty()) {
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
