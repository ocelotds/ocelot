/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.core;

import org.ocelotds.annotations.JsTopicAccessControl;
import org.ocelotds.security.JsTopicAccessController;
import org.ocelotds.security.JsTopicACAnnotationLiteral;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.Session;
import org.ocelotds.Constants;
import org.ocelotds.logger.OcelotLogger;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.messaging.MessageType;
import org.slf4j.Logger;

/**
 * Singleton sessions manager
 *
 * @author hhfrancois
 */
@Singleton
public class SessionManager {

	@Inject
	@OcelotLogger
	private Logger logger;

	private final Map<String, Set<Session>> sessionsByTopic = new ConcurrentHashMap<>();

	private static final Annotation DEFAULT_AT = new AnnotationLiteral<Default>() {
		private static final long serialVersionUID = 1L;
	};

	@Inject
	@Any
	Instance<JsTopicAccessController> topicAccessController;

	/**
	 * Process Access Topic Controller
	 *
	 * @param session
	 * @param topic
	 * @throws IllegalAccessException
	 */
	void checkAccessTopic(Session session, String topic) throws IllegalAccessException {
		JsTopicACAnnotationLiteral tcal = new JsTopicACAnnotationLiteral(topic);
		Instance<JsTopicAccessController> accessControls = topicAccessController.select(DEFAULT_AT);
		boolean tacPresent = false;
		if (Objects.nonNull(accessControls)) {
			for (JsTopicAccessController accessControl : accessControls) {
				accessControl.checkAccess(session, topic);
				tacPresent = true;
			}
		}
		accessControls = topicAccessController.select(tcal);
		if (Objects.nonNull(accessControls)) {
			for (JsTopicAccessController accessControl : accessControls) {
				accessControl.checkAccess(session, topic);
				tacPresent = true;
			}
		}
		if (!tacPresent) {
			logger.info("No topic access control found in project, add {} implementation with optional Qualifier {} in your project for add topic security.", JsTopicAccessController.class, JsTopicAccessControl.class);
		} else {
			logger.debug("Topic access control found in project.");
		}
	}

	/**
	 * Register session for topic
	 *
	 * @param topic
	 * @param session
	 * @return int : number subscribers
	 * @throws IllegalAccessException
	 */
	public int registerTopicSession(String topic, Session session) throws IllegalAccessException {
		Set<Session> sessions;
		if (sessionsByTopic.containsKey(topic)) {
			sessions = sessionsByTopic.get(topic);
		} else {
			sessions = Collections.synchronizedSet(new HashSet<Session>());
			sessionsByTopic.put(topic, sessions);
		}
		if (sessions.contains(session)) {
			sessions.remove(session);
		}
		checkAccessTopic(session, topic);
		logger.debug("'{}' subscribe to '{}'", session.getId(), topic);
		if(session.isOpen()) {
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
		logger.debug("'{}' unsubscribe to '{}'", session.getId(), topic);
		if (Constants.Topic.ALL.equals(topic)) {
			for (Collection<Session> sessions : sessionsByTopic.values()) {
				if (Objects.nonNull(sessions) && sessions.contains(session)) {
					sessions.remove(session);
				}
			}
		} else {
			Collection<Session> sessions = sessionsByTopic.get(topic);
			if (Objects.nonNull(sessions) && sessions.contains(session)) {
				sessions.remove(session);
			}
		}
		return getNumberSubscribers(topic);
	}

	/**
	 * Unregister sessions for topic
	 *
	 * @param topic
	 * @param sessions
	 */
	public void unregisterTopicSessions(String topic, Collection<Session> sessions) {
		Collection<Session> all = sessionsByTopic.get(topic);
		if (Objects.nonNull(sessions)) {
			all.removeAll(sessions);
		}
	}

	/**
	 * Remove sessions cause they are closed
	 *
	 * @param sessions
	 */
	public void removeSessionsToTopic(Collection<Session> sessions) {
		for (String topic : sessionsByTopic.keySet()) {
			unregisterTopicSessions(topic, sessions);
			sendSubscriptionEvent(Constants.Topic.SUBSCRIBERS + Constants.Topic.COLON + topic, getNumberSubscribers(topic));
		}
	}

	/**
	 * Remove session cause it's closed by the endpoint
	 *
	 * @param session
	 */
	public void removeSessionToTopics(Session session) {
		for (String topic : sessionsByTopic.keySet()) {
			sendSubscriptionEvent(Constants.Topic.SUBSCRIBERS + Constants.Topic.COLON + topic, unregisterTopicSession(topic, session));
		}
	}

	/**
	 * Send subscription event to all client
	 *
	 * @param topic
	 * @param session
	 */
	private void sendSubscriptionEvent(String topic, int nb) {
		Collection<Session> sessions = getSessionsForTopic(topic);
		if (!sessions.isEmpty()) {
			MessageToClient messageToClient = new MessageToClient();
			messageToClient.setId(topic);
			messageToClient.setType(MessageType.MESSAGE);
			messageToClient.setResponse(nb);
			for (Session session : sessions) {
				if (session.isOpen()) {
					session.getAsyncRemote().sendObject(messageToClient);
				}
			}
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
		if (sessionsByTopic.containsKey(topic)) {
			return Collections.unmodifiableSet(sessionsByTopic.get(topic));
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
		if (sessionsByTopic.containsKey(topic)) {
			return sessionsByTopic.get(topic).size();
		}
		return 0;
	}
}
