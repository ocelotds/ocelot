/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.JsCacheRemove;
import org.ocelotds.annotations.JsCacheResult;
import org.ocelotds.annotations.TransientDataService;
import org.ocelotds.core.MethodWithSessionInjection;
import org.ocelotds.core.SessionManager;
import org.ocelotds.core.UpdatedCacheManager;
import org.ocelotds.i18n.Locale;
import org.ocelotds.i18n.ThreadLocalContextHolder;
import java.util.Collection;
import java.util.Map;
import javax.inject.Inject;
import javax.websocket.Session;
import org.ocelotds.annotations.JsTopic;
import org.ocelotds.annotations.JsTopicName;
import org.ocelotds.logger.OcelotLogger;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.CDI)
public class OcelotServices {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	private UpdatedCacheManager updatedCacheManager;
	
	@Inject
	private SessionManager sessionManager;
	
	@MethodWithSessionInjection
	@JsCacheRemove(cls = OcelotServices.class, methodName = "getLocale")
	public void setLocale(Locale locale) {
	}

	@TransientDataService
	public void setLocale(Locale l, Session session) {
		java.util.Locale locale = new java.util.Locale(l.getLanguage(), l.getCountry());
		logger.debug("Receive setLocale({}) call from client.", locale);
		session.getUserProperties().put(Constants.LOCALE, locale);
		ThreadLocalContextHolder.put(Constants.LOCALE, locale);
	}

	@MethodWithSessionInjection
	@JsCacheResult(year = 1)
	public Locale getLocale() {
		return null;
	}

	@TransientDataService
	public Locale getLocale(Session session) {
		logger.debug("Receive getLocale call from client.");
		java.util.Locale locale = (java.util.Locale) session.getUserProperties().get(Constants.LOCALE);
		Locale l = new Locale();
		l.setLanguage(locale.getLanguage());
		l.setCountry(locale.getCountry());
		logger.debug("getLocale() = {}", l);
		return l;
	}
	
	public Collection<String> getOutDatedCache(Map<String, Long> states) {
		return updatedCacheManager.getOutDatedCache(states);
	}
	
	@MethodWithSessionInjection
	public void subscribe(String topic) {
	}

	@MethodWithSessionInjection
	public void unsubscribe(String topic) {
	}

	@JsTopic
	@TransientDataService
	public Integer subscribe(Session session, @JsTopicName(prefix=Constants.Topic.SUBSCRIBERS) String topic) throws IllegalAccessException {
		return sessionManager.registerTopicSession(topic, session);
	}

	@JsTopic
	@TransientDataService
	public Integer unsubscribe(Session session, @JsTopicName(prefix=Constants.Topic.SUBSCRIBERS) String topic) {
		return sessionManager.unregisterTopicSession(topic, session);
	}

	public Integer getNumberSubscribers(String topic) {
		return sessionManager.getNumberSubscribers(topic);
	}
}
