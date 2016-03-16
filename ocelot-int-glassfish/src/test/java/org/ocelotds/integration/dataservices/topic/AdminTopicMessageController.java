/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.integration.dataservices.topic;

import javax.inject.Inject;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.security.JsTopicMessageController;
import org.slf4j.Logger;
import org.ocelotds.annotations.JsTopicControl;
import org.ocelotds.security.NotRecipientException;
import org.ocelotds.security.UserContext;

/**
 * 
 * @author hhfrancois
 */
@JsTopicControl("admintopic")
public class AdminTopicMessageController implements JsTopicMessageController {

	@Inject
	@OcelotLogger
	private Logger logger;
	
	@Override
	public void checkRight(UserContext ctx, MessageToClient mtc) throws NotRecipientException {
		String topic = mtc.getId();
		boolean access = ctx.isUserInRole("ADMINR");
		System.out.println("CHECK if "+ctx.getPrincipal()+" is ADMINR "+access);
		logger.debug("Check mytopic access to topic {} : access = {}", topic, access);
		if(!access) {
			throw new NotRecipientException(ctx.getPrincipal().getName());
		}
	}
	
}
