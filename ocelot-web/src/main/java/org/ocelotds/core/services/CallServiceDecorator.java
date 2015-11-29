/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.services;

import java.security.Principal;
import java.util.Locale;
import java.util.Map;
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
public abstract class CallServiceDecorator implements CallService {

	@Inject
	@Delegate
	@Any
	CallService callSercice;

	@Inject
	@OcelotLogger
	private Logger logger;

	@Override
	public void sendMessageToClient(MessageFromClient message, Session session) {
		if (null != session) {
			Map<String, Object> sessionProperties = session.getUserProperties();
			// Get subject from config and set in session
			final Principal principal = session.getUserPrincipal();
			ThreadLocalContextHolder.put(Constants.PRINCIPAL, principal);

			ThreadLocalContextHolder.put(Constants.SESSION, session);
			ThreadLocalContextHolder.put(Constants.HANDSHAKEREQUEST, sessionProperties.get(Constants.HANDSHAKEREQUEST));

			final Locale locale = (Locale) sessionProperties.get(Constants.LOCALE);
			logger.debug("Decorate CallService for add context to session Principal : {}, Locale : {}", principal, locale);
		}
		callSercice.sendMessageToClient(message, session);
	}
}
