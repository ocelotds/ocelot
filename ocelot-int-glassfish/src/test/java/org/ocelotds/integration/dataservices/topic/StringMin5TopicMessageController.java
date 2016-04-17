/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.integration.dataservices.topic;

import org.ocelotds.security.JsTopicMessageController;
import org.ocelotds.annotations.JsTopicControl;
import org.ocelotds.security.NotRecipientException;
import org.ocelotds.security.UserContext;

/**
 * 
 * @author hhfrancois
 */
@JsTopicControl("string5topic")
public class StringMin5TopicMessageController implements JsTopicMessageController<String> {

	@Override
	public void checkRight(UserContext ctx, String topic, String payload) throws NotRecipientException {
		boolean access = (payload!=null)?payload.length()>4:false;
		System.out.println("CHECK payload lengh > 4 : "+access);
		if(!access) {
			throw new NotRecipientException(ctx.getPrincipal().getName());
		}
	}
	
}
