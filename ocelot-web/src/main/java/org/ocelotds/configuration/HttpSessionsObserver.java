/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.configuration;

import java.util.Enumeration;
import java.util.Locale;
import javax.enterprise.context.Initialized;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.HttpHeaders;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.context.ThreadLocalContextHolder;
import org.ocelotds.exceptions.LocaleNotFoundException;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
public class HttpSessionsObserver {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	private LocaleExtractor localeExtractor;

	@Inject
	HttpServletRequest request;

	public void processSessionScopedInit(@Observes @Initialized(SessionScoped.class) HttpSession session) {
		setContext(session);
	}

	void setContext(HttpSession httpSession) {
//		Session wsSession = requestManager.getSessionByHttpSession(httpSession);
//		System.out.println("WS Ocelot Session associate with request : "+wsSession);
		httpSession.setAttribute(Constants.LOCALE, getLocale());
		ThreadLocalContextHolder.put(Constants.HTTPSESSION, httpSession);
	}

	/**
	 * Return locale of client
	 *
	 * @param request
	 * @return
	 */
	Locale getLocale() {
		Enumeration<String> accepts = request.getHeaders(HttpHeaders.ACCEPT_LANGUAGE);
		logger.debug("Get accept-language from client headers : {}", accepts);
		if (null != accepts) {
			while (accepts.hasMoreElements()) {
				try {
					return localeExtractor.extractFromAccept(accepts.nextElement());
				} catch (LocaleNotFoundException ex) {
				}
			}
		}
		return Locale.US;
	}
}
