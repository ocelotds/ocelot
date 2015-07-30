/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.test;

import fr.hhdev.ocelot.security.JsTopicAccessController;
import javax.inject.Singleton;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
@Singleton
public class TestTopicAccessControler implements JsTopicAccessController {
	
	private final static Logger logger = LoggerFactory.getLogger(TestTopicAccessControler.class);

	private boolean access = true; 

	public boolean isAccess() {
		return access;
	}

	public void setAccess(boolean access) {
		this.access = access;
	}

	@Override
	public void checkAccess(Session session, String topic) throws IllegalAccessException {
		logger.debug("Check access to topic {} : access = {}", topic, access);
		if(!access) {
			throw new IllegalAccessException("access is set to false");
		}
	}
	
}
