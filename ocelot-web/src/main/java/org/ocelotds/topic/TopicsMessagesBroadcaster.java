/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ocelotds.topic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import org.ocelotds.messaging.MessageEvent;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.messaging.MessageType;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.ocelotds.annotations.JsTopicControl;
import org.ocelotds.annotations.JsTopicControls;
import org.ocelotds.annotations.JsTopicEvent;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.core.services.ArgumentServices;
import org.ocelotds.marshallers.JsonMarshallerException;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.security.UserContext;
import org.ocelotds.security.JsTopicCtrlAnnotationLiteral;
import org.ocelotds.security.JsTopicCtrlsAnnotationLiteral;
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
	private ArgumentServices argumentServices;

	@Inject
	@Any
	Instance<JsTopicMessageController<?>> topicMessageController;
	
	@Inject
	MessageControllerCache messageControllerCache;

	
	
	/**
	 * Send message to topic
	 *
	 * @param payload
	 * @param metadata
	 */
	public void sendObjectToTopic(@Observes @JsTopicEvent("") Object payload, EventMetadata metadata) {
		MessageToClient msg = new MessageToClient();
		InjectionPoint injectionPoint = metadata.getInjectionPoint();
		Annotated annotated = injectionPoint.getAnnotated();
		JsTopicEvent jte = annotated.getAnnotation(JsTopicEvent.class);
		if (jte != null) {
			JsonMarshaller jm = annotated.getAnnotation(JsonMarshaller.class);
			try {
				msg.setId(jte.value());
				if (jm != null) {
					msg.setJson(argumentServices.getJsonResultFromSpecificMarshaller(jm, payload));
				} else {
					msg.setResponse(payload);
				}
				sendMessageToTopic(msg, payload);
			} catch (JsonMarshallerException ex) {
				logger.error(jm+" can't be instantiate", ex);
			} catch (Throwable ex) {
				logger.error(payload+" can't be serialized with marshaller "+jm, ex);
			}
		}
	}
	
	/**
	 * Send message to topic, return number sended
	 * @param msg
	 * @return 
	 */
	public int sendMessageToTopic(@Observes @MessageEvent MessageToClient msg) {
		return sendMessageToTopic(msg, msg.getResponse());
	}
	
	/**
	 * Send message to topic, return number sended
	 * @param mtc
	 * @param payload
	 * @return 
	 */
	int sendMessageToTopic(MessageToClient mtc, Object payload) {
		int sended = 0;
		logger.debug("Sending message to topic {}...", mtc);
		Collection<Session> sessions = sessionManager.getSessionsForTopic(mtc.getId());
		if (sessions != null && !sessions.isEmpty()) {
			JsTopicMessageController msgControl = getJsTopicMessageController(mtc.getId());
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
					checkMessageTopic(userContextFactory.getUserContext(session.getId()), mtc.getId(), payload, msgControl);
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
	 * Get jstopic message controller
	 * @param topic
	 * @return 
	 */
	JsTopicMessageController getJsTopicMessageController(String topic) {
		logger.debug("Looking for messageController for topic '{}'", topic);
		JsTopicMessageController messageController = messageControllerCache.loadFromCache(topic);
		if(null == messageController) { // not in cache
			messageController = getJsTopicMessageControllerFromJsTopicControl(topic); // get from JsTopicControl
			if(null == messageController) {
				messageController = getJsTopicMessageControllerFromJsTopicControls(topic); // get from JsTopicControls
			}
			if(null != messageController) {
				messageControllerCache.saveToCache(topic, messageController.getClass()); // save in cache
			}
		}
		return messageController;
	}
	
	/**
	 * Get jstopic message controller from JsTopicControl
	 * @param topic
	 * @return 
	 */
	JsTopicMessageController getJsTopicMessageControllerFromJsTopicControl(String topic) {
		logger.debug("Looking for messageController for topic '{}' from JsTopicControl annotation", topic);
		JsTopicCtrlAnnotationLiteral anno = new JsTopicCtrlAnnotationLiteral(topic);
		Instance<JsTopicMessageController<?>> select = topicMessageController.select(anno);
		if(!select.isUnsatisfied()) {
			logger.debug("Found messageController for topic '{}' from JsTopicControl annotation", topic);
			return select.get();
		}
		return null;
	}

	/**
	 * without jdk8, @Repeatable doesn't work, so we use @JsTopicControls annotation and parse it
	 * @param topic
	 * @return 
	 */
	JsTopicMessageController getJsTopicMessageControllerFromJsTopicControls(String topic) {
		logger.debug("Looking for messageController for topic '{}' from JsTopicControls annotation", topic);
		JsTopicCtrlsAnnotationLiteral anno = new JsTopicCtrlsAnnotationLiteral();
		Instance<JsTopicMessageController<?>> select = topicMessageController.select(anno);
		if(select.isUnsatisfied()) {
			return null;
		}
		for (JsTopicMessageController<?> jsTopicMessageController : select) {
			JsTopicControls jsTopicControls = jsTopicMessageController.getClass().getAnnotation(JsTopicControls.class);
			JsTopicControl[] jsTopicControlList = jsTopicControls.value();
			for (JsTopicControl jsTopicControl : jsTopicControlList) {
				if(topic.equals(jsTopicControl.value())) {
					logger.debug("Found messageController for topic '{}' from JsTopicControls annotation", topic);
					return jsTopicMessageController;
				}
			}
		}
		return null;
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
