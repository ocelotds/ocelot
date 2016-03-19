/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.integration.dataservices.topic;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.ocelotds.security.JsTopicAccessController;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.security.UserContext;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@ApplicationScoped
public class TopicAccessController implements JsTopicAccessController {
	
	@Inject
	@OcelotLogger
	Logger logger;

	private boolean access = true; 

	public boolean isAccess() {
		return access;
	}

	public void setAccess(boolean access) {
		this.access = access;
	}

	@Override
	public void checkAccess(UserContext ctx, String topic) throws IllegalAccessException {
		logger.debug("Check access to topic {} : access = {}", topic, access);
		if(!access) {
			throw new IllegalAccessException("access is set to false");
		}
	}
	
}
