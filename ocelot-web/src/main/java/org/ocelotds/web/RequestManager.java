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
import org.ocelotds.objects.WsUserContext;
import org.ocelotds.security.UserContext;

/**
 *
 * @author hhfrancois
 */
@ApplicationScoped
public class RequestManager {

	private final Map<HandshakeRequest, Session> sessionByRequest = new HashMap<>();
	private final Map<String, HandshakeRequest> requestBySessionId = new HashMap<>();

	Map<HandshakeRequest, Session> getSessionByRequest() {
		return sessionByRequest;
	}
	Map<String, HandshakeRequest> getRequestBySessionId() {
		return requestBySessionId;
	}

	public void addSession(HandshakeRequest request, Session session) {
		if (request != null) {
			if (session != null) {
				getSessionByRequest().put(request, session);
				getRequestBySessionId().put(session.getId(), request);
			} 
		} else {
			removeSession(session);
		}
	}

	public void removeSession(Session session) {
		if(null != session) {
			HandshakeRequest request = null;
			Set<Map.Entry<HandshakeRequest, Session>> entrySet = getSessionByRequest().entrySet();
			for (Map.Entry<HandshakeRequest, Session> entry : entrySet) {
				if (session.equals(entry.getValue())) {
					request = entry.getKey();
					break;
				}
			}
			if (request != null) {
				getSessionByRequest().remove(request);
			}
			getRequestBySessionId().remove(session.getId());
		}
	}

	public Session getSessionByHttpSession(HttpSession httpSession) {
		Set<Map.Entry<HandshakeRequest, Session>> entrySet = getSessionByRequest().entrySet();
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
	
	HandshakeRequest getHandshakeRequest(Session session) {
		if(null != session) {
			return getRequestBySessionId().get(session.getId());
		}
		return null;
	}

	public UserContext getUserContext(Session session) {
		return new WsUserContext(getHandshakeRequest(session));
	}
}
