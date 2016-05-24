/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ocelotds.topic;

import org.ocelotds.messaging.MessageEvent;
import org.ocelotds.messaging.MessageToClient;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import org.ocelotds.annotations.JsTopicEvent;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.core.services.ArgumentServices;
import org.ocelotds.marshallers.JsonMarshallerException;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.slf4j.Logger;

/**
 * Bean that send push messages
 *
 * @author hhfrancois
 */
public class TopicsMessagesObservers {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	private ArgumentServices argumentServices;
	
	@Inject
	private TopicsMessagesBroadcaster topicsMessagesBroadcaster;

	/**
	 * Send message to topic
	 *
	 * @param payload
	 * @param metadata
	 */
	public void sendObjectToTopic(@Observes @JsTopicEvent("") Object payload, EventMetadata metadata) {
		MessageToClient msg = new MessageToClient();
		InjectionPoint injectionPoint = metadata.getInjectionPoint();
		Annotated annotated = injectionPoint.getAnnotated();
		JsTopicEvent jte = annotated.getAnnotation(JsTopicEvent.class);
		if (jte != null) {
			JsonMarshaller jm = annotated.getAnnotation(JsonMarshaller.class);
			try {
				msg.setId(jte.value());
				if (jm != null) {
					msg.setJson(argumentServices.getJsonResultFromSpecificMarshaller(jm, payload));
				} else {
					msg.setResponse(payload);
				}
				topicsMessagesBroadcaster.sendMessageToTopic(msg, payload);
			} catch (JsonMarshallerException ex) {
				logger.error(jm+" can't be instantiate", ex);
			} catch (Throwable ex) {
				logger.error(payload+" can't be serialized with marshaller "+jm, ex);
			}
		}
	}
	
	/**
	 * Send message to topic, return number sended
	 * @param msg
	 * @return 
	 */
	public int sendMessageToTopic(@Observes @MessageEvent MessageToClient msg) {
		return topicsMessagesBroadcaster.sendMessageToTopic(msg, getPayload(msg));
	}
	
	Object getPayload(MessageToClient msg) {
		Object payload = null;
		if(null != msg.getJson()) {
			payload = msg.getJson();
		} else {
			payload = msg.getResponse();
		}
		return payload;
	}
}
