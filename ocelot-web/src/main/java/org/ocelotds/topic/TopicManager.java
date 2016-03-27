/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.topic;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.Session;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.messaging.MessageType;
import org.slf4j.Logger;

/**
 * Singleton sessions manager
 *
 * @author hhfrancois
 */
@ApplicationScoped
public class TopicManager {

	@Inject
	@OcelotLogger
	private Logger logger;

	private final Map<String, Set<Session>> map = new ConcurrentHashMap<>();
	
	@Inject
	TopicAccessManager topicAccessManager;

	/**
	 * Register session for topic
	 *
	 * @param topic
	 * @param session
	 * @return int : number subscribers
	 * @throws IllegalAccessException
	 */
	public int registerTopicSession(String topic, Session session) throws IllegalAccessException {
		if (isInconsistenceContext(topic, session)) {
			return 0;
		}
		Set<Session> sessions;
		if (map.containsKey(topic)) {
			sessions = map.get(topic);
		} else {
			sessions = Collections.synchronizedSet(new HashSet<Session>());
			map.put(topic, sessions);
		}
		if (sessions.contains(session)) {
			sessions.remove(session);
		}
		topicAccessManager.checkAccessTopic(session, topic);
		logger.debug("'{}' subscribe to '{}'", session.getId(), topic);
		if (session.isOpen()) {
			sessions.add(session);
		}
		return getNumberSubscribers(topic);
	}

	/**
	 * Unregister session for topic. topic 'ALL' remove session for all topics
	 *
	 * @param topic
	 * @param session
	 * @return int : number subscribers remaining
	 */
	public int unregisterTopicSession(String topic, Session session) {
		if (isInconsistenceContext(topic, session)) {
			return 0;
		}
		logger.debug("'{}' unsubscribe to '{}'", session.getId(), topic);
		if (Constants.Topic.ALL.equals(topic)) {
			for (Collection<Session> sessions : map.values()) {
				removeSessionToSessions(session, sessions);
			}
		} else {
			removeSessionToSessions(session, map.get(topic));
		}
		return getNumberSubscribers(topic);
	}

	/**
	 * Return if argument is inconsistent for context : topic and session null or empty
	 * @param topic
	 * @param session
	 * @return 
	 */
	boolean isInconsistenceContext(String topic, Session session) {
		return null == topic || null == session || topic.isEmpty();
	}
	
	/**
	 * Remove session in sessions
	 *
	 * @param session
	 * @param sessions
	 * @return 1 if session removed else 0
	 */
	int removeSessionToSessions(Session session, Collection<Session> sessions) {
		if (sessions != null) {
			if (sessions.remove(session)) {
				return 1;
			}
		}
		return 0;
	}

	/**
	 * Unregister sessions for topic
	 *
	 * @param topic
	 * @param sessions
	 * @return 
	 */
	public boolean unregisterTopicSessions(String topic, Collection<Session> sessions) {
		if (sessions != null && !sessions.isEmpty()) {
			Collection<Session> all = map.get(topic);
			return all.removeAll(sessions);
		}
		return false;
	}

	/**
	 * Remove sessions cause they are closed
	 *
	 * @param sessions
	 */
	public void removeSessionsToTopic(Collection<Session> sessions) {
		if (sessions != null && !sessions.isEmpty()) {
			for (String topic : map.keySet()) {
				unregisterTopicSessions(topic, sessions);
				sendSubscriptionEvent(Constants.Topic.SUBSCRIBERS + Constants.Topic.COLON + topic, getNumberSubscribers(topic));
			}
		}
	}

	/**
	 * Remove session cause it's closed by the endpoint
	 *
	 * @param session
	 */
	public void removeSessionToTopics(Session session) {
		if(session != null) {
			for (String topic : map.keySet()) {
				sendSubscriptionEvent(Constants.Topic.SUBSCRIBERS + Constants.Topic.COLON + topic, unregisterTopicSession(topic, session));
			}
		}
	}

	/**
	 * Send subscription event to all client
	 *
	 * @param topic
	 * @param session
	 */
	void sendSubscriptionEvent(String topic, int nb) {
		Collection<Session> sessions = getSessionsForTopic(topic);
		if (!sessions.isEmpty()) {
			MessageToClient messageToClient = new MessageToClient();
			messageToClient.setId(topic);
			messageToClient.setType(MessageType.MESSAGE);
			messageToClient.setResponse(nb);
//			Collection<Session> sessionsClosed = new ArrayList<>(); // throws java.lang.StackOverflowError
			for (Session session : sessions) {
				if (session.isOpen()) {
					session.getAsyncRemote().sendObject(messageToClient);
//				} else {
//					sessionsClosed.add(session);
				}
			}
//			removeSessionsToTopic(sessionsClosed);
		}
	}

	/**
	 * Get Sessions for topics
	 *
	 * @param topic
	 * @return
	 */
	public Collection<Session> getSessionsForTopic(String topic) {
		Collection<Session> result;
		if (map.containsKey(topic)) {
			return Collections.unmodifiableSet(map.get(topic));
		} else {
			result = Collections.EMPTY_LIST;
		}
		return result;
	}

	/**
	 * Get Number Sessions for topics
	 *
	 * @param topic
	 * @return
	 */
	public int getNumberSubscribers(String topic) {
		if (map.containsKey(topic)) {
			return map.get(topic).size();
		}
		return 0;
	}
}
