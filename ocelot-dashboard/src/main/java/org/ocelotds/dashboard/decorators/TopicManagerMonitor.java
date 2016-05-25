/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.dashboard.decorators;

import java.util.Collection;
import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.websocket.Session;
import org.ocelotds.annotations.JsTopicEvent;
import org.ocelotds.dashboard.objects.SessionInfo;
import org.ocelotds.topic.SessionManager;
import org.ocelotds.topic.TopicManager;

/**
 *
 * @author hhfrancois
 */
@Decorator
@Priority(0)
public abstract class TopicManagerMonitor implements TopicManager {

	@Inject
	@Delegate
	@Any
	TopicManager topicManager;

	@Inject
	SessionManager sessionManager;

	@Inject
	@JsTopicEvent(value = "session-topic-add", jsonPayload = true)
	Event<String> addSessionToTopic;

	@Inject
	@JsTopicEvent(value = "session-topic-remove", jsonPayload = true)
	Event<String> removeSessionToTopic;

	@Override
	public int registerTopicSession(String topic, Session session) throws IllegalAccessException {
		int before = topicManager.getNumberSubscribers(topic);
		int after = topicManager.registerTopicSession(topic, session);
		if(after>before) {
			addSessionToTopic.fire(createMessage(topic, session));
		}
		return after;
	}

	@Override
	public int unregisterTopicSession(String topic, Session session) {
		int before = topicManager.getNumberSubscribers(topic);
		int after = topicManager.unregisterTopicSession(topic, session);
		if(after<before) {
			removeSessionToTopic.fire(createMessage(topic, session));
		}
		return after;
	}

	@Override
	public Collection<String> removeSessionToTopics(Session session) {
		Collection<String> topicsUpdated = topicManager.removeSessionToTopics(session);
		for (String topic : topicsUpdated) {
			removeSessionToTopic.fire(createMessage(topic, session));
		}
		return topicsUpdated;
	}
	
	private String createMessage(String topic, Session session) {
		return "{\"topic\":\""+topic+"\", \"sessionInfo\":{\"id\":\""+session.getId()+"\", \"username\":\""+sessionManager.getUsername(session)+"\",\"open\":\""+session.isOpen()+"\"}}";
	}

}
