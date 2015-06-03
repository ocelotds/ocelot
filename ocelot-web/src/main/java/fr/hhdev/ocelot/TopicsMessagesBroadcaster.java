/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package fr.hhdev.ocelot;

import fr.hhdev.ocelot.messaging.MessageEvent;
import fr.hhdev.ocelot.messaging.MessageToClient;
import java.io.IOException;
import java.util.Collection;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.EncodeException;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton envoyant les messages en mode push aux clients
 * @author hhfrancois
 */
@Singleton
public class TopicsMessagesBroadcaster {

	private final static Logger logger = LoggerFactory.getLogger(TopicsMessagesBroadcaster.class);

	@Inject
	private SessionManager sessionManager;

	/**
	 * Réponse asynchrone d'un requete initiée par le client Utilise l'encoder positionné sur le endpoint
	 *
	 * @param msg
	 */
	public void sendMessageToTopic(@Observes @MessageEvent MessageToClient msg) {
		logger.debug("SENDING MESSAGE/RESPONSE TO TOPIC {}", msg.toJson());
		try {
			if (sessionManager.existsTopicSessionForId(msg.getId())) {
				Collection<Session> sessions = sessionManager.getTopicSessionsForId(msg.getId());
				if (sessions != null && !sessions.isEmpty()) {
					logger.debug("SEND MESSAGE TO '{}' TOPIC {} CLIENT(s) : {}", new Object[]{msg.getId(), sessions.size(), msg.toJson()});
					for (Session session : sessions) {
						if (session.isOpen()) {
							session.getBasicRemote().sendObject(msg);
						}
					}
				} else {
					logger.debug("NO CLIENT FOR TOPIC '{}'", msg.getId());
				}
			} else {
				logger.debug("NO TOPIC '{}'", msg.getId());
			}
		} catch (IOException | EncodeException ex) {
			logger.error(ex.getMessage(), ex);
		}
	}
}
