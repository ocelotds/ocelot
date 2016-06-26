/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.JsCacheRemove;
import org.ocelotds.annotations.JsCacheResult;
import org.ocelotds.topic.TopicManager;
import org.ocelotds.core.UpdatedCacheManager;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;
import org.ocelotds.annotations.DashboardOnDebug;
import org.ocelotds.annotations.JsTopic;
import org.ocelotds.annotations.JsTopicName;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.annotations.WsDataService;
import org.ocelotds.context.OcelotContext;
import org.ocelotds.marshallers.LocaleMarshaller;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.marshalling.annotations.JsonUnmarshaller;
import org.ocelotds.objects.Options;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.CDI)
@DashboardOnDebug
public class OcelotServices {
	
	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	private UpdatedCacheManager updatedCacheManager;

	@Inject
	private TopicManager topicManager;
	
	@Inject
	private OcelotContext ocelotContext;

	/**
	 * Init core
	 * @param options
	 * @param httpSession
	 */
	public void initCore(Options options, HttpSession httpSession) {
		httpSession.setAttribute(Constants.Options.OPTIONS, options);
	}
	
	/**
	 * define locale for current user
	 * 
	 * @param locale
	 */
	@JsCacheRemove(cls = OcelotServices.class, methodName = "getLocale", keys = {}, userScope = true)
	public void setLocale(@JsonUnmarshaller(LocaleMarshaller.class) Locale locale) {
		logger.debug("Receive setLocale call from client. {}", locale);
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
		logger.debug("Receive getUsername call from client.");
		return ocelotContext.getPrincipal().getName();
	}

	/**
	 * GEt outdated cache among list
	 * @param states
	 * @return 
	 */
	public Collection<String> getOutDatedCache(Map<String, Long> states) {
		return updatedCacheManager.getOutDatedCache(states);
	}

	/**
	 * Subscribe to topic
	 * 
	 * @param topic
	 * @param session
	 * @return
	 * @throws IllegalAccessException 
	 */
	@JsTopic
	@WsDataService
	public Integer subscribe(@JsTopicName(prefix = Constants.Topic.SUBSCRIBERS) String topic, Session session) throws IllegalAccessException {
		return topicManager.registerTopicSession(topic, session);
	}

	/**
	 * Unsubscribe to topic
	 * @param topic
	 * @param session
	 * @return 
	 */
	@JsTopic
	@WsDataService
	public Integer unsubscribe(@JsTopicName(prefix = Constants.Topic.SUBSCRIBERS) String topic, Session session) {
		return topicManager.unregisterTopicSession(topic, session);
	}

	/**
	 * Get number of subscriber
	 * 
	 * @param topic
	 * @return 
	 */
	public Integer getNumberSubscribers(String topic) {
		return topicManager.getNumberSubscribers(topic);
	}
}
