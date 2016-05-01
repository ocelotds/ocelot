/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.topic.messageControl;

import org.ocelotds.security.JsTopicMessageController;
import org.ocelotds.security.NotRecipientException;
import org.ocelotds.security.UserContext;

/**
 *
 * @author hhfrancois
 */
public class DefaultJsTopicMessageController implements JsTopicMessageController<Object> {

	@Override
	public void checkRight(UserContext ctx, String topic, Object payload) throws NotRecipientException {
	}
	
}
