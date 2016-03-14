/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ocelotds.web;

import org.ocelotds.core.SessionManager;
import org.ocelotds.messaging.MessageEvent;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.messaging.MessageType;
import java.util.ArrayList;
import java.util.Collection;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.websocket.Session;
import org.ocelotds.annotations.JsTopicEvent;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.core.services.ArgumentServices;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.slf4j.Logger;

/**
 * Bean that send push messages
 *
 * @author hhfrancois
 */
public class TopicsMessagesBroadcaster {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	private SessionManager sessionManager;

	@Inject
	private ArgumentServices argumentServices;

	/**
	 * Send message to topic
	 *
	 * @param object
	 * @param metadata
	 */
	public void sendObjectToTopic(@Observes @JsTopicEvent("") Object object, EventMetadata metadata) {
		MessageToClient msg = new MessageToClient();
		InjectionPoint injectionPoint = metadata.getInjectionPoint();
		Annotated annotated = injectionPoint.getAnnotated();
		JsTopicEvent jte = annotated.getAnnotation(JsTopicEvent.class);
		if (jte != null) {
			JsonMarshaller jm = annotated.getAnnotation(JsonMarshaller.class);
			try {
				msg.setId(jte.value());
				if (jm != null) {
					msg.setJson(argumentServices.getJsonResultFromSpecificMarshaller(jm, object));
				} else {
					msg.setResponse(object);
				}
				sendMessageToTopic(msg);
			} catch (InstantiationException | IllegalAccessException ex) {
				logger.error(jm+" can't be instantiate", ex);
			} catch (Throwable ex) {
				logger.error(object+" can't be serialized with marshaller "+jm, ex);
			}
		}
	}
	
	public int sendMessageToTopic(@Observes @MessageEvent MessageToClient msg) {
		msg.setType(MessageType.MESSAGE);
		int sended = 0;
		logger.debug("Sending message to topic {}...", msg);
		Collection<Session> sessions = sessionManager.getSessionsForTopic(msg.getId());
		if (sessions != null && !sessions.isEmpty()) {
			Collection<Session> sessionsClosed = new ArrayList<>();
			for (Session session : sessions) {
				if (session != null) {
					if (session.isOpen()) {
						session.getAsyncRemote().sendObject(msg);
						sended++;
					} else {
						sessionsClosed.add(session);
					}
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Send message to '{}' topic {} client(s) : {}", new Object[]{msg.getId(), sessions.size() - sessionsClosed.size(), msg});
			}
			if (!sessionsClosed.isEmpty()) {
				logger.debug("Session closed to remove '{}'", sessionsClosed.size());
				sessionManager.removeSessionsToTopic(sessionsClosed);
			}
		} else {
			logger.debug("No client for topic '{}'", msg.getId());
		}
		return sended;
	}
}
