/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.ocelot;

import fr.hhdev.ocelot.messaging.Fault;
import fr.hhdev.ocelot.messaging.MessageToClient;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author hhfrancois
 */
@Slf4j
@ClientEndpoint
public class OcelotClientEnpoint {
	private static final Map<String, String> results = new HashMap<>();
	private static final Map<String, Fault> faults = new HashMap<>();
	
	@OnMessage
	public void onMessage(String message, Session session) {
		logger.debug("RECEIVE RESPONSE FROM SERVER = {}", message);
		MessageToClient messageToClient = MessageToClient.createFromJson(message);
		logger.debug("RESULTAT SAVED {} : {}/{}", new Object[] {messageToClient.getId(), messageToClient.getResult(), messageToClient.getFault()});
		if(null!=messageToClient.getResult()) {
			results.put(messageToClient.getId(), ""+messageToClient.getResult());
		}
		if(null!=messageToClient.getFault()) {
			faults.put(messageToClient.getId(), messageToClient.getFault());
		}
	}
	
	public static String getResult(String messageid) {
		String get = results.get(messageid);
		results.remove(messageid);
		return get;
	}

	public static Fault getFault(String messageid) {
		Fault get = faults.get(messageid);
		faults.remove(messageid);
		return get;
	}
}
