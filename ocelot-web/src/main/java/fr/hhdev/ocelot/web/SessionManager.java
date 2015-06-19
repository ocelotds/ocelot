/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot.web;

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
	 * Register session for topic
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
		logger.debug("'{}' subscribe to '{}'", session.getId(), topic);
		sessions.add(session);
	}

	/**
	 * Unregister session for topic
	 *
	 * @param topic
	 * @param session
	 */
	public void unregisterTopicSession(String topic, Session session) {
		logger.debug("'{}' unsubscribe to '{}'", session.getId(), topic);
		Set<Session> sessions = sessionsByTopic.get(topic);
		if (sessions != null && sessions.contains(session)) {
			sessions.remove(session);
		}
	}

	/**
	 * Remove session cause it's close by the endpoint
	 *
	 * @param session
	 */
	public void removeSessionToTopic(Session session) {
		for (String topic : sessionsByTopic.keySet()) {
			Set<Session> sessions = sessionsByTopic.get(topic);
			if (sessions.contains(session)) {
				sessions.remove(session);
			}
		}
	}

	/**
	 * Get Sessions for topics
	 *
	 * @param id
	 * @return
	 */
	public Collection<Session> getSessionsForTopic(String id) {
		Collection<Session> result;
		if(existsSessionForTopic(id)) {
			return sessionsByTopic.get(id);
		} else {
			result = Collections.EMPTY_LIST;
		}
		return result;
	}

	/**
	 * check if sessions exist for topic
	 *
	 * @param id
	 * @return
	 */
	public boolean existsSessionForTopic(String id) {
		return sessionsByTopic.containsKey(id) && !sessionsByTopic.get(id).isEmpty();
	}
}
