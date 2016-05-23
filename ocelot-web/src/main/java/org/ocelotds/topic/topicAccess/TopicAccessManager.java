/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.topic.topicAccess;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import org.ocelotds.annotations.JsTopicControl;
import org.ocelotds.annotations.JsTopicControls;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.security.JsTopicAccessController;
import org.ocelotds.security.JsTopicCtrlAnnotationLiteral;
import org.ocelotds.security.JsTopicCtrlsAnnotationLiteral;
import org.ocelotds.security.UserContext;
import org.ocelotds.topic.JsTopicControlsTools;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
public class TopicAccessManager {

	static final Annotation DEFAULT_AT = new AnnotationLiteral<Default>() {
		private static final long serialVersionUID = 1L;
	};

	@Inject
	@OcelotLogger
	private Logger logger;
	
	@Inject
	private JsTopicControlsTools jsTopicControlsTools;
	
	@Inject
	@Any
	Instance<JsTopicAccessController> topicAccessController;

	/**
	 * Process Access Topic Controller
	 *
	 * @param ctx
	 * @param topic
	 * @throws IllegalAccessException
	 */
	public void checkAccessTopic(UserContext ctx, String topic) throws IllegalAccessException {
		boolean tacPresent0 = checkAccessTopicGlobalAC(ctx, topic);
		boolean tacPresent1 = checkAccessTopicFromJsTopicControl(ctx, topic);
		boolean tacPresent2 = checkAccessTopicFromJsTopicControls(ctx, topic);
		if (!(tacPresent0 | tacPresent1 | tacPresent2)) {
			logger.info("No topic access control found in project, add {} implementation with optional Qualifier {} in your project for add subscription security.", JsTopicAccessController.class, JsTopicControl.class);
		}
	}
	
	/**
	 * Check if global access control is allowed
	 * @param ctx
	 * @param topic
	 * @return true if at least one global topicAccessControl exist
	 * @throws IllegalAccessException 
	 */
	boolean checkAccessTopicGlobalAC(UserContext ctx, String topic) throws IllegalAccessException {
		logger.debug("Looking for accessController for topic '{}' from GlobalAccess", topic);
		Iterable<JsTopicAccessController> accessControls = topicAccessController.select(DEFAULT_AT);
		return checkAccessTopicFromControllers(ctx, topic, accessControls);
	}
	
	/**
	 * Check if specific access control is allowed
	 * @param session
	 * @param topic
	 * @return true if at least one specific topicAccessControl exist
	 * @throws IllegalAccessException 
	 */
	boolean checkAccessTopicFromJsTopicControl(UserContext ctx, String topic) throws IllegalAccessException {
		logger.debug("Looking for accessController for topic '{}' from JsTopicControl annotation", topic);
		Iterable<JsTopicAccessController> accessControls = topicAccessController.select(new JsTopicCtrlAnnotationLiteral(topic));
		return checkAccessTopicFromControllers(ctx, topic, accessControls);
	}
	
	/**
	 * Check if specific access control is allowed
	 * @param ctx
	 * @param topic
	 * @return true if at least one specific topicAccessControl exist
	 * @throws IllegalAccessException 
	 */
	boolean checkAccessTopicFromJsTopicControls(UserContext ctx, String topic) throws IllegalAccessException {
		logger.debug("Looking for accessController for topic '{}' from JsTopicControls annotation", topic);
		Instance<JsTopicAccessController> select = topicAccessController.select(new JsTopicCtrlsAnnotationLiteral());
		if(select.isUnsatisfied()) {
			return false;
		}
		return checkAccessTopicFromJsTopicAccessControllers(ctx, topic, select);
	}
	
	/**
	 * Check if specific access control is allowed from controllers
	 * @param ctx
	 * @param topic
	 * @param controllers
	 * @return true if at least one specific topicAccessControl exist
	 * @throws IllegalAccessException 
	 */
	boolean checkAccessTopicFromJsTopicAccessControllers(UserContext ctx, String topic, Iterable<JsTopicAccessController> controllers) throws IllegalAccessException {
		logger.debug("Looking for accessController for topic '{}' from JsTopicAccessControllers", topic);
		for (JsTopicAccessController jsTopicAccessController : controllers) {
			if(checkAccessTopicFromController(ctx, topic, jsTopicAccessController)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if specific access control is allowed from controller
	 * @param ctx
	 * @param topic
	 * @param controllers
	 * @return true if at least one specific topicAccessControl exist
	 * @throws IllegalAccessException 
	 */
	boolean checkAccessTopicFromController(UserContext ctx, String topic, JsTopicAccessController jsTopicAccessController) throws IllegalAccessException {
		logger.debug("Looking for accessController for topic '{}' from JsTopicAccessController {}", topic, jsTopicAccessController);
		JsTopicControls jsTopicControls = jsTopicControlsTools.getJsTopicControlsFromProxyClass(jsTopicAccessController.getClass());
		logger.debug("Looking for accessController for topic '{}' from jsTopicControls {}", topic, jsTopicControls);
		if(null != jsTopicControls) {
			logger.debug("Looking for accessController for topic '{}' from jsTopicControls {}, {}", topic, jsTopicControls, jsTopicControls.value());
			for (JsTopicControl jsTopicControl : jsTopicControls.value()) {
				if(topic.equals(jsTopicControl.value())) {
					logger.debug("Found accessController for topic '{}' from JsTopicControls annotation", topic);
					checkAccessTopicFromControllers(ctx, topic, Arrays.asList(jsTopicAccessController));
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Check if access topic is granted by accessControls
	 * @param ctx
	 * @param topic
	 * @param accessControls
	 * @return
	 * @throws IllegalAccessException 
	 */
	boolean checkAccessTopicFromControllers(UserContext ctx, String topic, Iterable<JsTopicAccessController> accessControls) throws IllegalAccessException {
		logger.debug("Looking for accessController for topic '{}' : '{}'", topic, accessControls);
		boolean tacPresent = false;
		if (null != accessControls) {
			for (JsTopicAccessController accessControl : accessControls) {
				accessControl.checkAccess(ctx, topic);
				tacPresent = true;
			}
		}
		return tacPresent;
	}
}
