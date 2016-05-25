/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.dashboard.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.websocket.Session;
import org.ocelotds.annotations.DashboardOnDebug;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.JsTopic;
import org.ocelotds.annotations.JsTopicName;
import org.ocelotds.dashboard.objects.SessionInfo;
import org.ocelotds.dashboard.security.DashboardSecureProvider;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.messaging.MessageType;
import org.ocelotds.security.OcelotSecured;
import org.ocelotds.topic.SessionManager;
import org.ocelotds.topic.TopicManager;

/**
 *
 * @author hhfrancois
 */
@DataService
@DashboardOnDebug
@OcelotSecured(provider = DashboardSecureProvider.class)
public class TopicServices {

	@Inject
	private TopicManager topicManager;

	@Inject
	SessionManager sessionManager;

	public Map<String, Collection<SessionInfo>> getSessionIdsByTopic() {
		Map<String, Collection<Session>> sessionsByTopic = topicManager.getSessionsByTopic();
		Set<Map.Entry<String, Collection<Session>>> entrySet = sessionsByTopic.entrySet();
		Map<String, Collection<SessionInfo>> result = new HashMap<>();
		for (Map.Entry<String, Collection<Session>> entry : entrySet) {
			Collection<SessionInfo> sessionInfos = new ArrayList<>();
			result.put(entry.getKey(), sessionInfos);
			Collection<Session> value = entry.getValue();
			for (Session session : value) {
				sessionInfos.add(new SessionInfo(session.getId(), sessionManager.getUsername(session), session.isOpen()));
			}
		}
		return result;
	}

	@JsTopic(jsonPayload = true)
	public String sendJsonToTopic(String json, @JsTopicName String topic) {
		return json;
	}

	public void sendJsonToTopicForSession(String json, String topic, String sessionid) {
		MessageToClient mtc = new MessageToClient();
		mtc.setId(topic);
		mtc.setJson(json);
		mtc.setType(MessageType.MESSAGE);
		Collection<Session> sessions = topicManager.getSessionsForTopic(topic);
		for (Session session : sessions) {
			if (session.getId().equals(sessionid) && session.isOpen()) {
				session.getAsyncRemote().sendObject(mtc);
				break;
			}
		}
	}
}
