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
import javax.security.auth.Subject;
import javax.websocket.Session;
import org.ocelotds.Constants;
import org.ocelotds.context.ThreadLocalContextHolder;
import org.ocelotds.logger.OcelotLogger;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.security.SubjectServices;
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

	@Inject
	SubjectServices subjectServices;

	@Override
	public void sendMessageToClient(MessageFromClient message, Session session) {
		if (session != null) {
			Map<String, Object> sessionProperties = session.getUserProperties();
			// Get subject from config and set in session
			final Principal principal = (Principal) sessionProperties.get(Constants.PRINCIPAL);
			ThreadLocalContextHolder.put(Constants.PRINCIPAL, principal);

			final Locale locale = (Locale) sessionProperties.get(Constants.LOCALE);
			ThreadLocalContextHolder.put(Constants.LOCALE, locale);

			final Subject subject = (Subject) sessionProperties.get(Constants.SUBJECT);
			subjectServices.setSubject(subject, principal);
			logger.debug("Decorate CallService for add context to session Principal : {}, Locale : {}, Subject : {}", principal, locale, subject);
		}
		callSercice.sendMessageToClient(message, session);
	}
}