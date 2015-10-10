/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import java.security.Principal;
import org.ocelotds.context.ThreadLocalContextHolder;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.JsCacheRemove;
import org.ocelotds.annotations.JsCacheResult;
import org.ocelotds.annotations.TransientDataService;
import org.ocelotds.core.MethodWithSessionInjection;
import org.ocelotds.core.SessionManager;
import org.ocelotds.core.UpdatedCacheManager;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import javax.websocket.Session;
import org.ocelotds.annotations.JsTopic;
import org.ocelotds.annotations.JsTopicName;
import org.ocelotds.annotations.OcelotLogger;
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
		ThreadLocalContextHolder.put(Constants.LOCALE, locale);
	}

	/**
	 * define locale for current user
	 *
	 * @param locale
	 * @param session
	 */
	@TransientDataService
	@JsCacheRemove(cls = OcelotServices.class, methodName = "getLocale", keys = {})
	public void setLocale(@JsonUnmarshaller(LocaleUnmarshaller.class) Locale locale, Session session) {
		session.getUserProperties().put(Constants.LOCALE, locale);
	}

	/**
	 * get current user locale
	 *
	 * @return
	 */
	@JsCacheResult(year = 1)
	@JsonMarshaller(LocaleMarshaller.class)
	public Locale getLocale() {
		logger.debug("Receive getLocale call from client.");
		return (Locale) ThreadLocalContextHolder.get(Constants.LOCALE);
	}

	/**
	 * get current user locale
	 *
	 * @return
	 */
	@MethodWithSessionInjection
	public String getUsername() {
		return null;
	}

	/**
	 * return current username from session
	 *
	 * @param session
	 * @return
	 */
	@TransientDataService
	public String getUsername(Session session) {
		logger.debug("Receive getUsername call from client.");
		Principal p = session.getUserPrincipal();
		if(null!= p) {
			return (String) session.getUserPrincipal().getName();
		}
		return Constants.ANONYMOUS;
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
	public Integer subscribe(Session session, @JsTopicName(prefix = Constants.Topic.SUBSCRIBERS) String topic) throws IllegalAccessException {
		return sessionManager.registerTopicSession(topic, session);
	}

	@JsTopic
	@TransientDataService
	public Integer unsubscribe(Session session, @JsTopicName(prefix = Constants.Topic.SUBSCRIBERS) String topic) {
		return sessionManager.unregisterTopicSession(topic, session);
	}

	public Integer getNumberSubscribers(String topic) {
		return sessionManager.getNumberSubscribers(topic);
	}
}
