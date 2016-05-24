/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ocelotds.topic;

import org.ocelotds.topic.messageControl.MessageControllerManager;
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
import javax.websocket.SessionException;
import org.ocelotds.annotations.JsTopicEvent;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.core.services.ArgumentServices;
import org.ocelotds.marshallers.JsonMarshallerException;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.security.UserContext;
import org.ocelotds.security.JsTopicMessageController;
import org.ocelotds.security.NotRecipientException;
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
	private TopicManager sessionManager;

	@Inject
	private UserContextFactory userContextFactory;

	@Inject
	private MessageControllerManager messageControllerManager;

	/**
	 * Send message to topic, return number sended
	 * @param mtc
	 * @param payload
	 * @return 
	 */
	public int sendMessageToTopic(MessageToClient mtc, Object payload) {
		int sended = 0;
		logger.debug("Sending message to topic {}...", mtc);
		Collection<Session> sessions = sessionManager.getSessionsForTopic(mtc.getId());
		if (sessions != null && !sessions.isEmpty()) {
			JsTopicMessageController msgControl = messageControllerManager.getJsTopicMessageController(mtc.getId());
			Collection<Session> sessionsClosed = new ArrayList<>();
			for (Session session : sessions) {
				try {
					sended += checkAndSendMtcToSession(session, msgControl, mtc, payload);
				} catch(SessionException se) {
					sessionsClosed.add(se.getSession());
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Send message to '{}' topic {} client(s) : {}", new Object[]{mtc.getId(), sessions.size() - sessionsClosed.size(), mtc});
			}
			sessionManager.removeSessionsToTopic(sessionsClosed);
		} else {
			logger.debug("No client for topic '{}'", mtc.getId());
		}
		return sended;
	}

	/**
	 * Send Message to session, check right before
	 * @param session
	 * @param msgControl
	 * @param mtc
	 * @param payload
	 * @return
	 * @throws SessionException 
	 */
	int checkAndSendMtcToSession(Session session, JsTopicMessageController msgControl, MessageToClient mtc, Object payload) throws SessionException {
		if (session != null) {
			if (session.isOpen()) {
				try {
					if (null != msgControl) {
						checkMessageTopic(userContextFactory.getUserContext(session.getId()), mtc.getId(), payload, msgControl);
					}
					mtc.setType(MessageType.MESSAGE);
					session.getAsyncRemote().sendObject(mtc);
					return 1;
				} catch (NotRecipientException ex) {
					logger.debug("{} is exclude to receive a message in {}", ex.getMessage(), mtc.getId());
				}
			} else {
				throw new SessionException("CLOSED", null, session);
			}
		}
		return 0;
	}
	
	/**
	 * Check if message is granted by messageControl
	 * @param ctx
	 * @param mtc
	 * @param msgControl
	 * @return
	 * @throws NotRecipientException 
	 */
	void checkMessageTopic(UserContext ctx, String topic, Object payload, JsTopicMessageController msgControl) throws NotRecipientException {
		if (null != msgControl) {
			msgControl.checkRight(ctx, topic, payload);
		}
	}
}
