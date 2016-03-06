/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;

/**
 *
 * @author hhfrancois
 */
@ApplicationScoped
public class RequestManager {

	private final Map<HandshakeRequest, Session> sessionsByRequest = new HashMap<>();

	Map<HandshakeRequest, Session> getSessionsByRequest() {
		return sessionsByRequest;
	}

	public void addSession(HandshakeRequest request, Session session) {
		if (request != null) {
			if (session != null) {
				getSessionsByRequest().put(request, session);
			} 
		} else {
			removeSession(session);
		}
	}

	public void removeSession(Session session) {
		HandshakeRequest request = null;
		Set<Map.Entry<HandshakeRequest, Session>> entrySet = getSessionsByRequest().entrySet();
		for (Map.Entry<HandshakeRequest, Session> entry : entrySet) {
			if (session.equals(entry.getValue())) {
				request = entry.getKey();
				break;
			}
		}
		if (request != null) {
			getSessionsByRequest().remove(request);
		}
	}

	public Session getSessionByHttpSession(HttpSession httpSession) {
		Set<Map.Entry<HandshakeRequest, Session>> entrySet = getSessionsByRequest().entrySet();
		for (Map.Entry<HandshakeRequest, Session> entry : entrySet) {
			if (httpSession.equals(entry.getKey().getHttpSession())) {
				Session session = entry.getValue();
				if(session.isOpen()) {
					return session;
				}
			}
		}
		return null;
	}

}
