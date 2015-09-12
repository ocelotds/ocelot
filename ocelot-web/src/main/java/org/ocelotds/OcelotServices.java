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
import org.ocelotds.i18n.ThreadLocalContextHolder;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import javax.websocket.Session;
import org.ocelotds.annotations.JsTopic;
import org.ocelotds.annotations.JsTopicName;
import org.ocelotds.logger.OcelotLogger;
import org.ocelotds.marshallers.LocaleMarshaller;
import org.ocelotds.marshallers.LocaleUnmarshaller;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.marshalling.annotations.JsonUnmarshaller;
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
	public void setLocale(@JsonUnmarshaller(LocaleUnmarshaller.class) Locale locale) {
	}

	@TransientDataService
	@JsCacheRemove(cls = OcelotServices.class, methodName = "getLocale", keys = {})
	public void setLocale(@JsonUnmarshaller(LocaleUnmarshaller.class) Locale locale, Session session) {
		logger.debug("Receive setLocale({}) call from client.", locale);
		session.getUserProperties().put(Constants.LOCALE, locale);
		ThreadLocalContextHolder.put(Constants.LOCALE, locale);
	}

	@MethodWithSessionInjection
	public Locale getLocale() {
		return null;
	}

	@TransientDataService
	@JsCacheResult(year = 1)
	@JsonMarshaller(LocaleMarshaller.class)
	public Locale getLocale(Session session) {
		logger.debug("Receive getLocale call from client.");
		return (Locale) session.getUserProperties().get(Constants.LOCALE);
	}
	
	@MethodWithSessionInjection
	public String getUsername() {
		return null;
	}

	@TransientDataService
	public String getUSername(Session session) {
		logger.debug("Receive getUsername call from client.");
		return (String) session.getUserPrincipal().getName();
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
