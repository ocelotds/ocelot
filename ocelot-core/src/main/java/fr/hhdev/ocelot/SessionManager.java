/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton sessions manager
 * @author hhfrancois
 */
@Singleton
public class SessionManager {
	private final static Logger logger = LoggerFactory.getLogger(SessionManager.class);
	private final Map<String, Set<Session>> sessionsByTopic = new HashMap<>();
	
	/**
	 * Enregistre une session correspondant à un topic
	 *
	 * @param topic
	 * @param session
	 */
	public void registerTopicSession(String topic, Session session) {
		Set<Session> sessions;
		if (sessionsByTopic.containsKey(topic)) {
			sessions = sessionsByTopic.get(topic);
		} else {
			sessions = new HashSet<>();
			sessionsByTopic.put(topic, sessions);
		}
		logger.debug("SUBSCRIPTION TO '{}'", topic);
		sessions.add(session);
	}

	/**
	 * Dés-Enregistre une session correspondant à un topic
	 *
	 * @param topic
	 * @param session
	 */
	public void unregisterTopicSession(String topic, Session session) {
		logger.debug("UNSUBSCRIPTION TO '{}'", topic);
		Set<Session> sessions = sessionsByTopic.get(topic);
		if (sessions != null && sessions.contains(session)) {
			sessions.remove(session);
		}
	}

	/**
	 * Supprime une sesion ou qu'elle soit car fermé par le endpoint
	 *
	 * @param session
	 */
	public void removeSession(Session session) {
		for (String topic : sessionsByTopic.keySet()) {
			Set<Session> sessions = sessionsByTopic.get(topic);
			if (sessions.contains(session)) {
				sessions.remove(session);
			}
		}
	}

	/**
	 * retourne et supprime la session correspondant au topic id
	 *
	 * @param id
	 * @return
	 */
	public Collection<Session> getTopicSessionsForId(String id) {
		Collection<Session> result;
		if(existsTopicSessionForId(id)) {
			return sessionsByTopic.get(id);
		} else {
			result = Collections.EMPTY_LIST;
		}
		return result;
	}

	/**
	 * retourne si une session correspond au msg id
	 *
	 * @param id
	 * @return
	 */
	public boolean existsTopicSessionForId(String id) {
		return sessionsByTopic.containsKey(id) && !sessionsByTopic.get(id).isEmpty();
	}
}
