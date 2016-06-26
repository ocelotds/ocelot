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
import org.ocelotds.marshalling.ArgumentServices;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
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
			String topic = jte.value();
			msg.setId(topic);
			JsonMarshaller jm = annotated.getAnnotation(JsonMarshaller.class);
			try {
				if (jm != null) {
					msg.setJson(argumentServices.getJsonResultFromSpecificMarshaller(jm, payload));
				} else if(jte.jsonPayload()) {
					if(!String.class.isInstance(payload)) {
						throw new UnsupportedOperationException("'"+payload+"' cannot be a json object. Field annotated JsTopicEvent(jsonPayload=true) must be Event<String> type and fire correct Json.");
					}
					msg.setJson((String) payload);
				} else {
					msg.setResponse(payload);
				}
				topicsMessagesBroadcaster.sendMessageToTopic(msg, payload);
			} catch (JsonMarshallingException ex) {
				logger.error("'"+payload+"' cannot be send to : '"+topic+"'. It cannot be serialized with marshaller "+jm, ex);
			} catch (Throwable ex) {
				logger.error("'"+payload+"' cannot be send to : '"+topic+"'.", ex);
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
		Object payload;
		if(null != msg.getJson()) {
			payload = msg.getJson();
		} else {
			payload = msg.getResponse();
		}
		return payload;
	}
}
