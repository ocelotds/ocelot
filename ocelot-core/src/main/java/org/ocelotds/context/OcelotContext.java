/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.context;

import java.security.Principal;
import java.util.Locale;
import java.util.Map;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
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
		Locale locale = Locale.US;
		Session session = getSession();
		if (session != null) {
			if (session.getUserProperties().containsKey(Constants.LOCALE)) {
				locale = (Locale) session.getUserProperties().get(Constants.LOCALE);
				logger.debug("Get locale from OcelotServices : {}", locale);
			} else {
				logger.debug("Get locale from OcelotServices : default : {}", locale);
			}
		}
		return locale;
	}

	public void setLocale(Locale locale) {
		Session session = getSession();
		if(session!=null) {
			Map<String,Object> userProps =session.getUserProperties();
			userProps.remove(Constants.LOCALE);
			if (locale != null) {
				userProps.put(Constants.LOCALE, locale);
			}
		}
	}

	HandshakeRequest getHandshakeRequest() {
		return (HandshakeRequest) ThreadLocalContextHolder.get(Constants.HANDSHAKEREQUEST);
	}

	public boolean isUserInRole(String role) {
		HandshakeRequest hr = getHandshakeRequest();
		if(hr!=null) {
			return hr.isUserInRole(role);
		}
		logger.warn("Fail to get userInRole, HandshakeRequest is null in threadLocal");
		return false;
	}

	public Principal getPrincipal() {
		Session session = getSession();
		if (null != session) {
			Principal p = session.getUserPrincipal();
			if (null != p) {
				return p;
			}
		}
		return ANONYMOUS;
	}
	
	private static final Principal ANONYMOUS = new Principal() {
		@Override
		public String getName() {
			return Constants.ANONYMOUS;
		}
	};
}
