/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.topic;

import java.lang.annotation.Annotation;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.websocket.Session;
import org.ocelotds.annotations.JsTopicControl;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.security.JsTopicAccessController;
import org.ocelotds.security.JsTopicCtrlAnnotationLiteral;
import org.ocelotds.security.UserContext;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
public class TopicAccessManager {

	private static final Annotation DEFAULT_AT = new AnnotationLiteral<Default>() {
		private static final long serialVersionUID = 1L;
	};

	@Inject
	@OcelotLogger
	private Logger logger;
	
	@Inject
	private UserContextFactory userContextFactory;

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
		boolean tacPresent = checkAccessTopicGlobalAC(session, topic);
		tacPresent  |= checkAccessTopicSpecificAC(session, topic);
		if (!tacPresent) {
			logger.info("No topic access control found in project, add {} implementation with optional Qualifier {} in your project for add topic security.", JsTopicAccessController.class, JsTopicControl.class);
		} else {
			logger.debug("Topic access control found in project.");
		}
	}
	
	/**
	 * Check if global access control is allowed
	 * @param session
	 * @param topic
	 * @return true if at least one global topicAccessControl exist
	 * @throws IllegalAccessException 
	 */
	boolean checkAccessTopicGlobalAC(Session session, String topic) throws IllegalAccessException {
		Instance<JsTopicAccessController> accessControls = topicAccessController.select(DEFAULT_AT);
		return checkAccessTopicSpecificAC(session, topic, accessControls);
	}
	
	/**
	 * Check if specific access control is allowed
	 * @param session
	 * @param topic
	 * @return true if at least one specific topicAccessControl exist
	 * @throws IllegalAccessException 
	 */
	boolean checkAccessTopicSpecificAC(Session session, String topic) throws IllegalAccessException {
		Instance<JsTopicAccessController> accessControls = topicAccessController.select(new JsTopicCtrlAnnotationLiteral(topic));
		return checkAccessTopicSpecificAC(session, topic, accessControls);
	}
	
	/**
	 * Check if access topic is granted by accessControls
	 * @param session
	 * @param topic
	 * @param accessControls
	 * @return
	 * @throws IllegalAccessException 
	 */
	boolean checkAccessTopicSpecificAC(Session session, String topic, Instance<JsTopicAccessController> accessControls) throws IllegalAccessException {
		boolean tacPresent = false;
		if (null != accessControls) {
			UserContext userContext = userContextFactory.getUserContext(session.getId());
			for (JsTopicAccessController accessControl : accessControls) {
				accessControl.checkAccess(userContext, topic);
				tacPresent = true;
			}
		}
		return tacPresent;
	}
}
