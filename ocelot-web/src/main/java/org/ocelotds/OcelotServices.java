/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.JsCacheRemove;
import org.ocelotds.annotations.JsCacheResult;
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
import org.ocelotds.context.OcelotContext;
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

	@Inject
	private OcelotContext ocelotContext;

	@Inject
	private Session session;

	/**
	 * define locale for current user
	 *
	 * @param locale
	 */
	@JsCacheRemove(cls = OcelotServices.class, methodName = "getLocale", keys = {})
	public void setLocale(@JsonUnmarshaller(LocaleUnmarshaller.class) Locale locale) {
		ocelotContext.setLocale(locale);
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
		return ocelotContext.getLocale();
	}

	/**
	 * return current username from session
	 *
	 * @return
	 */
	public String getUsername() {
		return ocelotContext.getUsername();
	}

	public Collection<String> getOutDatedCache(Map<String, Long> states) {
		return updatedCacheManager.getOutDatedCache(states);
	}

	@JsTopic
	public Integer subscribe(@JsTopicName(prefix = Constants.Topic.SUBSCRIBERS) String topic) throws IllegalAccessException {
		return sessionManager.registerTopicSession(topic, session);
	}

	@JsTopic
	public Integer unsubscribe(@JsTopicName(prefix = Constants.Topic.SUBSCRIBERS) String topic) {
		return sessionManager.unregisterTopicSession(topic, session);
	}

	public Integer getNumberSubscribers(String topic) {
		return sessionManager.getNumberSubscribers(topic);
	}
}
