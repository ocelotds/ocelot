/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.topic;

import java.util.ArrayList;
import java.util.Arrays;
import org.ocelotds.topic.topicAccess.TopicAccessManager;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
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
public class TopicManagerImpl implements TopicManager {

	@Inject
	@OcelotLogger
	private Logger logger;

	final Map<String, Collection<Session>> map = new ConcurrentHashMap<>();

	@Inject
	TopicAccessManager topicAccessManager;

	@Inject
	private UserContextFactory userContextFactory;
	
	/**
	 * Return map topics and all sessions associate
	 *
	 * @return
	 */
	@Override
	public Map<String, Collection<Session>> getSessionsByTopic() {
		return map;
	}

	/**
	 * Register session for topic
	 *
	 * @param topic
	 * @param session
	 * @return int : number subscribers
	 * @throws IllegalAccessException
	 */
	@Override
	public int registerTopicSession(String topic, Session session) throws IllegalAccessException {
		if (isInconsistenceContext(topic, session)) {
			return 0;
		}
		Collection<Session> sessions;
		if (map.containsKey(topic)) {
			sessions = map.get(topic);
		} else {
			sessions = Collections.synchronizedCollection(new ArrayList<Session>());
			map.put(topic, sessions);
		}
		topicAccessManager.checkAccessTopic(userContextFactory.getUserContext(session.getId()), topic);
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
	@Override
	public int unregisterTopicSession(String topic, Session session) {
		if (isInconsistenceContext(topic, session)) {
			return 0;
		}
		logger.debug("'{}' unsubscribe to '{}'", session.getId(), topic);
		if (Constants.Topic.ALL.equals(topic)) {
			for (Map.Entry<String, Collection<Session>> entry : map.entrySet()) {
				Collection<Session> sessions = entry.getValue();
				removeSessionToSessions(session, sessions);
				if (sessions.isEmpty()) {
					map.remove(entry.getKey());
				}
			}
		} else {
			Collection<Session> sessions = map.get(topic);
			removeSessionToSessions(session, sessions);
			if (sessions==null || sessions.isEmpty()) {
				map.remove(topic);
			}
		}
		return getNumberSubscribers(topic);
	}

	/**
	 * Return if argument is inconsistent for context : topic and session null or empty
	 *
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
	boolean unregisterTopicSessions(String topic, Collection<Session> sessions) {
		boolean unregister = false;
		if (sessions != null && !sessions.isEmpty()) {
			Collection<Session> all = map.get(topic);
			if(all != null) {
				unregister = all.removeAll(sessions);
				if (all.isEmpty()) {
					map.remove(topic);
				}
			}
		}
		return unregister;
	}

	/**
	 * Remove sessions cause they are closed
	 *
	 * @param sessions
	 */
	@Override
	public Collection<String> removeSessionsToTopic(Collection<Session> sessions) {
		if (sessions != null && !sessions.isEmpty()) {
			Collection<String> topicUpdated = new ArrayList<>();
			for (String topic : map.keySet()) {
				if(unregisterTopicSessions(topic, sessions)) {
					topicUpdated.add(topic);
					sendSubscriptionEvent(Constants.Topic.SUBSCRIBERS + Constants.Topic.COLON + topic, getNumberSubscribers(topic));
				}
			}
			return topicUpdated;
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * Remove session cause it's closed by the endpoint
	 *
	 * @param session
	 */
	@Override
	public Collection<String> removeSessionToTopics(Session session) {
		if (session != null) {
			return removeSessionsToTopic(Arrays.asList(session));
//			for (String topic : map.keySet()) {
//				sendSubscriptionEvent(Constants.Topic.SUBSCRIBERS + Constants.Topic.COLON + topic, unregisterTopicSession(topic, session));
//			}
		}
		return Collections.EMPTY_LIST;
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
	@Override
	public Collection<Session> getSessionsForTopic(String topic) {
		Collection<Session> result;
		if (map.containsKey(topic)) {
			return Collections.unmodifiableCollection(map.get(topic));
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
	@Override
	public int getNumberSubscribers(String topic) {
		if (map.containsKey(topic)) {
			return map.get(topic).size();
		}
		return 0;
	}
}
