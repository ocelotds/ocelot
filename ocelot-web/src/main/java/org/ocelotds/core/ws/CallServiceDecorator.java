/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.ws;

import java.security.Principal;
import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.websocket.Session;
import org.ocelotds.Constants;
import org.ocelotds.context.ThreadLocalContextHolder;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.messaging.MessageFromClient;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@Decorator
@Priority(0)
public abstract class CallServiceDecorator implements CallService {

	@Inject
	@Delegate
	@Any
	CallService callSercice;

	@Inject
	@OcelotLogger
	private Logger logger;
	
	@Inject
	private Principal principal;

	@Override
	public boolean sendMessageToClient(MessageFromClient message, Session session) {
		if (null != session) {
			// Get subject from config and set in session, locale is always take in session
			ThreadLocalContextHolder.put(Constants.SESSION, session);
			logger.debug("Decorate CallService for add context to session Principal : {}", principal);
			return callSercice.sendMessageToClient(message, session);
		}
		return false;
	}
}
