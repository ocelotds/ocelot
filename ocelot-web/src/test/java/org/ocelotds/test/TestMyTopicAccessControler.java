/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.test;

import org.ocelotds.annotations.JsTopicAccessControl;
import org.ocelotds.security.JsTopicAccessController;
import javax.inject.Singleton;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
@Singleton
@JsTopicAccessControl("mytopic")
public class TestMyTopicAccessControler implements JsTopicAccessController {
	
	private final static Logger logger = LoggerFactory.getLogger(TestMyTopicAccessControler.class);

	private boolean access = true; 

	public boolean isAccess() {
		return access;
	}

	public void setAccess(boolean access) {
		this.access = access;
	}

	@Override
	public void checkAccess(Session session, String topic) throws IllegalAccessException {
		logger.debug("Check mytopic access to topic {} : access = {}", topic, access);
		if(!access) {
			throw new IllegalAccessException("mytopic access is set to false");
		}
	}
	
}
