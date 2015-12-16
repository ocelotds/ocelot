/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.JsCacheRemove;
import org.ocelotds.annotations.JsCacheResult;
import org.ocelotds.core.SessionManager;
import org.ocelotds.core.UpdatedCacheManager;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
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
import org.ocelotds.objects.OcelotMethod;
import org.ocelotds.objects.OcelotService;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.CDI)
public class OcelotServices {
	
	@Inject
	private ServiceTools serviceTools;

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
	
	@Any
	@Inject
	@DataService(resolver = "")
	private Instance<Object> dataservices;

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
		return ocelotContext.getPrincipal().getName();
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

	/**
	 * Return all services present in application
	 * [{"name":"instancename", methods=[{"name":"methodname","returntype":"void","argtypes":["",""],"argnames":["name1","name2"],"argtemplates":["",""]}]}]
	 * @return 
	 */
	public List<OcelotService> getServices() {
		List<OcelotService> result = new ArrayList<>();
		for (Object dataservice : dataservices) {
			Class cls = serviceTools.getRealClass(dataservice.getClass());
			OcelotService ocelotService = new OcelotService(serviceTools.getInstanceNameFromDataservice(cls));
			result.add(ocelotService);
			Method[] methods = cls.getMethods();
			for (Method method : methods) {
				if (serviceTools.isConsiderateMethod(method)) {
					ocelotService.getMethods().add(getOcelotMethod(method));
				}
			}
		}
		return result;
	}
	
	private OcelotMethod getOcelotMethod(Method method) {
		OcelotMethod ocelotMethod = new OcelotMethod(method.getName(), serviceTools.getShortName(serviceTools.getLiteralType(method.getGenericReturnType())));
		Annotation[][] annotations = method.getParameterAnnotations();
		Type[] types = method.getGenericParameterTypes();
		int index = 0;
		for (Type type : types) {
			ocelotMethod.getArgtypes().add(serviceTools.getShortName(serviceTools.getLiteralType(type)));
			ocelotMethod.getArgnames().add("arg"+index);
			ocelotMethod.getArgtemplates().add(serviceTools.getTemplateOfType(type, serviceTools.getJsonUnmarshaller(annotations[index])));
			index++;
		}
		return ocelotMethod;
	}
}
