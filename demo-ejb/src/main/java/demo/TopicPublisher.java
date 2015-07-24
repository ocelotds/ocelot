/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo;

import fr.hhdev.ocelot.messaging.MessageToClient;
import fr.hhdev.ocelot.messaging.MessageEvent;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 *
 * @author hhfrancois
 */
@Singleton
public class TopicPublisher {
	
	private int i = 0;
	@Inject
	@MessageEvent
	Event<MessageToClient> wsEvent;

	@Schedule(dayOfWeek = "*", month = "*", hour = "*", dayOfMonth = "*", year = "*", minute = "*", second = "*/30", persistent = false)
	public void publish() {
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId("mytopic");
		messageToClient.setResponse("Message From server "+(i++));
		wsEvent.fire(messageToClient);
	}
}
