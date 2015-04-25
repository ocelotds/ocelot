/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.test;

import fr.hhdev.ocelot.messaging.Fault;
import fr.hhdev.ocelot.messaging.MessageToClient;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
@ClientEndpoint
public class OcelotClientEnpoint {
	private static final Map<String, String> results = new HashMap<>();
	private static final Map<String, Fault> faults = new HashMap<>();
	private static final Logger logger = LoggerFactory.getLogger(OcelotClientEnpoint.class);
	
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
		logger.debug("LECTURE DU RESULT SAVED POUR L'ID {}", new Object[] {messageid});
		String get = results.get(messageid);
		results.remove(messageid);
		logger.debug("LECTURE DU RESULT SAVED POUR L'ID {} : {}", new Object[] {messageid, get});
		return get;
	}

	public static Fault getFault(String messageid) {
		logger.debug("LECTURE DU FAULT SAVED POUR L'ID {}", new Object[] {messageid});
		Fault get = faults.get(messageid);
		faults.remove(messageid);
		logger.debug("LECTURE DU FAULT SAVED POUR L'ID {} : {}", new Object[] {messageid, get});
		return get;
	}
}
