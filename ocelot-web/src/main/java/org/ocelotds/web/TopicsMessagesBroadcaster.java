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
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.websocket.Session;
import javax.websocket.SessionException;
import org.ocelotds.annotations.JsTopicEvent;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.core.services.ArgumentServices;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.security.UserContext;
import org.ocelotds.security.JsTopicCtrlAnnotationLiteral;
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
	private SessionManager sessionManager;

	@Inject
	private RequestManager requestManager;

	@Inject
	private ArgumentServices argumentServices;

	@Inject
	@Any
	Instance<JsTopicMessageController> topicMessageController;

	
	
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
	
	/**
	 * Send message to topic, return number sended
	 * @param msg
	 * @return 
	 */
	public int sendMessageToTopic(@Observes @MessageEvent MessageToClient msg) {
		int sended = 0;
		logger.debug("Sending message to topic {}...", msg);
		Collection<Session> sessions = sessionManager.getSessionsForTopic(msg.getId());
		if (sessions != null && !sessions.isEmpty()) {
			JsTopicMessageController msgControl = getJsTopicMessageController(msg.getId());
			Collection<Session> sessionsClosed = new ArrayList<>();
			for (Session session : sessions) {
				try {
					sended += sendMtcToSession(session, msgControl, msg);
				} catch(SessionException se) {
					sessionsClosed.add(se.getSession());
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Send message to '{}' topic {} client(s) : {}", new Object[]{msg.getId(), sessions.size() - sessionsClosed.size(), msg});
			}
			sessionManager.removeSessionsToTopic(sessionsClosed);
		} else {
			logger.debug("No client for topic '{}'", msg.getId());
		}
		return sended;
	}
	
	/**
	 * Send Message to session, check right before
	 * @param session
	 * @param msgControl
	 * @param mtc
	 * @return
	 * @throws SessionException 
	 */
	int sendMtcToSession(Session session, JsTopicMessageController msgControl, MessageToClient mtc) throws SessionException {
		if (session != null) {
			if (session.isOpen()) {
				try {
					checkMessageTopic(requestManager.getUserContext(session), mtc, msgControl);
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
	 * Get jstopic message controller
	 * @param topic
	 * @return 
	 */
	JsTopicMessageController getJsTopicMessageController(String topic) {
		JsTopicCtrlAnnotationLiteral anno = new JsTopicCtrlAnnotationLiteral(topic);
		Instance<JsTopicMessageController> select = topicMessageController.select(anno);
		if(select.isUnsatisfied()) {
			return null;
		}
		return select.get();
	}

	/**
	 * Check if message is granted by messageControl
	 * @param ctx
	 * @param mtc
	 * @param msgControl
	 * @return
	 * @throws NotRecipientException 
	 */
	void checkMessageTopic(UserContext ctx, MessageToClient mtc, JsTopicMessageController msgControl) throws NotRecipientException {
		if (null != msgControl) {
			msgControl.checkRight(ctx, mtc);
		}
		mtc.setType(MessageType.MESSAGE);
	}
}
