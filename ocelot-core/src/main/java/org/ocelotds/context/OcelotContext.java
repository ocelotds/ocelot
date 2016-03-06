/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.context;

import java.security.Principal;
import java.util.Locale;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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

	@Inject
	private Principal principal;

	@Produces
	Session getSession() {
		return (Session) ThreadLocalContextHolder.get(Constants.SESSION);
	}

//	@Produces
	HttpSession getHttpSession() {
		return (HttpSession) ThreadLocalContextHolder.get(Constants.HTTPSESSION);
	}

	public Locale getLocale() {
		Locale locale = getLocaleFromHttpSession();
		if(locale == null) {
			locale = Locale.US;
		}
		return locale;
	}
	
	Locale getLocaleFromHttpSession() {
		Locale locale = null;
		HttpSession httpSession = getHttpSession();
		if (httpSession != null) {
			locale = (Locale) httpSession.getAttribute(Constants.LOCALE);
		}
		return locale;
	}
	
	public void setLocale(Locale locale) {
		setLocaleToHttpSession(locale);
	}

	public void setLocaleToHttpSession(Locale locale) {
		HttpSession session = getHttpSession();
		if (session != null) {
			session.removeAttribute(Constants.LOCALE);
			if (locale != null) {
				session.setAttribute(Constants.LOCALE, locale);
			}
		} else {
			logger.warn("HttpSession seems not associating with local thread, locale not set in.");
		}
	}

	HttpServletRequest getRequest() {
		return (HttpServletRequest) ThreadLocalContextHolder.get(Constants.HTTPREQUEST);
	}

	public boolean isUserInRole(String role) {
		HttpServletRequest request = getRequest();
		if (request != null) {
			return request.isUserInRole(role);
		}
		logger.warn("Fail to get userInRole, HttpServletRequest is null in threadLocal");
		return false;
	}

	public Principal getPrincipal() {
		return principal;
	}
}
