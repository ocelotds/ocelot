/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.integration.dataservices.topic;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.ocelotds.Constants;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.JsTopic;
import org.ocelotds.annotations.JsTopicEvent;
import org.ocelotds.annotations.JsTopicName;
import org.ocelotds.messaging.MessageEvent;
import org.ocelotds.messaging.MessageToClient;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.CDI)
public class TopicDataService {
	
	@Inject
	@JsTopicEvent("mytopic")
	Event<Object> wsEvent;
	
	@Inject
	@MessageEvent
	Event<MessageToClient> wsEventDynTopic;
	
	
	@JsTopic("mytopic")
	public String sendMessageInMyTopic() {
		return "messageFromServer1";
	}
	
	@JsTopic
	public String sendMessageInDynTopic(@JsTopicName String topic) {
		return "messageFromServer2";
	}
	
	public void sendXMessageInMyTopic(int nb) {
		for (int i = 0; i < nb; i++) {
			wsEvent.fire("messageFromServer"+i);
		}
	}
	
	public void sendXMessageInDynTopic(int nb, String topic) {
		for (int i = 0; i < nb; i++) {
			MessageToClient toTopic = new MessageToClient();
			toTopic.setId(topic);
			toTopic.setResponse("messageFromServer"+i);
			wsEventDynTopic.fire(toTopic);
		}
	}

	@JsTopic("admintopic")
	public String sendMessageInAdminTopic() {
		return "messageFromServer1";
	}
}
