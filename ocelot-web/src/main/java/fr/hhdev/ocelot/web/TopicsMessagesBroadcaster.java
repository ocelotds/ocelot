/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package fr.hhdev.ocelot.web;

import fr.hhdev.ocelot.messaging.MessageEvent;
import fr.hhdev.ocelot.messaging.MessageToClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.EncodeException;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton that send push messages
 * @author hhfrancois
 */
@Singleton
public class TopicsMessagesBroadcaster {

	private final static Logger logger = LoggerFactory.getLogger(TopicsMessagesBroadcaster.class);

	@Inject
	private SessionManager sessionManager;

	/**
	 * Send message to topic
	 *
	 * @param msg
	 */
	public void sendMessageToTopic(@Observes @MessageEvent MessageToClient msg) {
		logger.debug("Sending message to topic {}...", msg.toJson());
		try {
			if (sessionManager.existsSessionForTopic(msg.getId())) {
				Collection<Session> sessions = sessionManager.getSessionsForTopic(msg.getId());
				Collection<Session> closed = new ArrayList<>();
				if (!sessions.isEmpty()) {
					logger.debug("Send message to '{}' topic {} client(s) : {}", new Object[]{msg.getId(), sessions.size(), msg.toJson()});
					for (Session session : sessions) {
						if (session.isOpen()) {
							session.getBasicRemote().sendObject(msg);
						} else {
							closed.add(session);
						}
					}
					logger.debug("Session closed to remove '{}'", closed.size());
					sessions.removeAll(closed);
				} else {
					logger.debug("No client for topic '{}'", msg.getId());
				}
			} else {
				logger.debug("No topic '{}'", msg.getId());
			}
		} catch (IOException | EncodeException ex) {
			logger.error("Fail to send message to topic: "+msg.getId(), ex);
		}
	}
}
