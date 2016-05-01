/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.topic.messageControl;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.ocelotds.annotations.JsTopicControl;
import org.ocelotds.annotations.JsTopicControls;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.core.UnProxyClassServices;
import org.ocelotds.security.JsTopicCtrlAnnotationLiteral;
import org.ocelotds.security.JsTopicCtrlsAnnotationLiteral;
import org.ocelotds.security.JsTopicMessageController;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
public class MessageControllerManager {
	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	MessageControllerCache messageControllerCache;

	@Inject
	@Any
	Instance<JsTopicMessageController<?>> topicMessageController;

	@Inject
	private UnProxyClassServices unProxyClassServices;

	/**
	 * Get jstopic message controller
	 * @param topic
	 * @return 
	 */
	public JsTopicMessageController getJsTopicMessageController(String topic) {
		logger.debug("Looking for messageController for topic '{}'", topic);
		JsTopicMessageController messageController = messageControllerCache.loadFromCache(topic);
		if(null == messageController) { // not in cache
			messageController = getJsTopicMessageControllerFromJsTopicControl(topic); // get from JsTopicControl
			if(null == messageController) {
				messageController = getJsTopicMessageControllerFromJsTopicControls(topic); // get from JsTopicControls
				if(null == messageController) {
					messageController = new DefaultJsTopicMessageController();
				}
			}
			messageControllerCache.saveToCache(topic, messageController.getClass()); // save in cache
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
		Instance<JsTopicMessageController<?>> select = topicMessageController.select(new JsTopicCtrlAnnotationLiteral(topic));
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
		Instance<JsTopicMessageController<?>> select = topicMessageController.select(new JsTopicCtrlsAnnotationLiteral());
		if(select.isUnsatisfied()) {
			return null;
		}
		return getJsTopicMessageControllerFromIterable(topic, select);
	}
	
	/**
	 * without jdk8, @Repeatable doesn't work, so we use @JsTopicControls annotation and parse it
	 * @param topic
	 * @param controllers
	 * @return 
	 */
	JsTopicMessageController getJsTopicMessageControllerFromIterable(String topic, Iterable<JsTopicMessageController<?>> controllers) {
		if(null == controllers || null == topic) {
			return null;
		}
		for (JsTopicMessageController<?> jsTopicMessageController : controllers) {
			JsTopicControls jsTopicControls = getJsTopicControls(jsTopicMessageController);
			if(null != jsTopicControls) {
				JsTopicControl[] jsTopicControlList = jsTopicControls.value();
				for (JsTopicControl jsTopicControl : jsTopicControlList) {
					if(topic.equals(jsTopicControl.value())) {
						logger.debug("Found messageController for topic '{}' from JsTopicControls annotation", topic);
						return jsTopicMessageController;
					}
				}
			}
		}
		return null;
	}

	/**
	 * get JsTopicControls from JsTopicAccessController instance
	 * @param jsTopicAccessController
	 * @return 
	 */
	JsTopicControls getJsTopicControls(JsTopicMessageController jsTopicMessageController) {
		Class<?> realClass = unProxyClassServices.getRealClass(jsTopicMessageController.getClass());
		return realClass.getAnnotation(JsTopicControls.class);
	}
}
