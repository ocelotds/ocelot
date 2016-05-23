/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.topic;

import org.ocelotds.topic.topicAccess.TopicAccessManager;
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
public interface TopicManager {

	/**
	 * Return map topics and all sessions associate
	 * @return 
	 */
	public Map<String, Set<Session>> getSessionsByTopic();

	/**
	 * Register session for topic
	 *
	 * @param topic
	 * @param session
	 * @return int : number subscribers
	 * @throws IllegalAccessException
	 */
	public int registerTopicSession(String topic, Session session) throws IllegalAccessException;
	/**
	 * Unregister session for topic. topic 'ALL' remove session for all topics
	 *
	 * @param topic
	 * @param session
	 * @return int : number subscribers remaining
	 */
	public int unregisterTopicSession(String topic, Session session);

	/**
	 * Unregister sessions for topic
	 *
	 * @param topic
	 * @param sessions
	 * @return
	 */
	public boolean unregisterTopicSessions(String topic, Collection<Session> sessions);

	/**
	 * Remove sessions cause they are closed
	 *
	 * @param sessions
	 */
	public void removeSessionsToTopic(Collection<Session> sessions);

	/**
	 * Remove session cause it's closed by the endpoint
	 *
	 * @param session
	 */
	public void removeSessionToTopics(Session session);


	/**
	 * Get Sessions for topics
	 *
	 * @param topic
	 * @return
	 */
	public Collection<Session> getSessionsForTopic(String topic);

	/**
	 * Get Number Sessions for topics
	 *
	 * @param topic
	 * @return
	 */
	public int getNumberSubscribers(String topic);
}
