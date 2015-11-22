/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.context;

import java.security.Principal;
import java.util.Locale;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.websocket.Session;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotLogger;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
public class OcelotContext {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Produces
	Session getSession() {
		return (Session) ThreadLocalContextHolder.get(Constants.SESSION);
	}

	public Locale getLocale() {
		Session session = getSession();
		Locale locale;
		if (session == null) {
			locale = Locale.US;
		} else {
			locale = (Locale) session.getUserProperties().get(Constants.LOCALE);
			if (null == locale) {
				locale = new Locale("en", "US");
				logger.debug("Get locale from OcelotServices : default : {}", locale);
			} else {
				logger.debug("Get locale from OcelotServices : {}", locale);
			}
		}
		return locale;
	}

	public void setLocale(Locale locale) {
		Session session = getSession();
		session.getUserProperties().put(Constants.LOCALE, locale);
	}

	public String getUsername() {
		Session session = getSession();
		if(null != session) {
			Principal p = session.getUserPrincipal();
			if (null != p) {
				return p.getName();
			}
		}
		return Constants.ANONYMOUS;
	}
}
