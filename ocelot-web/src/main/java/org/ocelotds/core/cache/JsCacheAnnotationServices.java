/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.cache;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.ocelotds.Constants;
import org.ocelotds.KeyMaker;
import org.ocelotds.annotations.JsCacheRemove;
import org.ocelotds.annotations.JsCacheResult;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.messaging.CacheEvent;
import org.ocelotds.messaging.MessageEvent;
import org.ocelotds.messaging.MessageToClient;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
public class JsCacheAnnotationServices {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	@MessageEvent
	Event<MessageToClient> wsEvent;

	@Inject
	@CacheEvent
	Event<String> cacheEvent;
	
	@Inject
	KeyMaker keyMaker;
	
	@Inject
	CacheArgumentServices cacheArgumentServices;

	/**
	 * Process annotation JsCacheRemoveAll and send message for suppress all the cache
	 *
	 */
	public void processJsCacheRemoveAll() {
		logger.debug("Process JsCacheRemoveAll annotation");
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(Constants.Cache.CLEANCACHE_TOPIC);
		messageToClient.setResponse(Constants.Cache.ALL);
		wsEvent.fire(messageToClient);
	}

	/**
	 * Get deadline for cache if 0 : is 1 year cache
	 *
	 * @param jcr : the annotation
	 * @return long
	 */
	public long getJsCacheResultDeadline(JsCacheResult jcr) {
		Calendar deadline = getNowCalendar();
		if ((jcr.year() + jcr.month() + jcr.day() + jcr.hour() + jcr.minute() + jcr.second() + jcr.millisecond()) == 0) {
			deadline.add(Calendar.YEAR, 1);
		} else {
			deadline.add(Calendar.YEAR, jcr.year());
			deadline.add(Calendar.MONTH, jcr.month());
			deadline.add(Calendar.DATE, jcr.day());
			deadline.add(Calendar.HOUR, jcr.hour());
			deadline.add(Calendar.MINUTE, jcr.minute());
			deadline.add(Calendar.SECOND, jcr.second());
			deadline.add(Calendar.MILLISECOND, jcr.millisecond());
		}
		return deadline.getTime().getTime();
	}
	
	/**
	 * Return now
	 * @return 
	 */
	Calendar getNowCalendar() {
		return Calendar.getInstance();
	}

	/**
	 * Process an annotation JsCacheRemove and send a removeCache message to all clients connected
	 *
	 * @param jcr : l'annotation
	 * @param paramNames : name of parameters
	 * @param jsonArgs : method arguments json format
	 */
	public void processJsCacheRemove(JsCacheRemove jcr, List<String> paramNames, List<String> jsonArgs) {
		logger.debug("Process JsCacheRemove annotation : {}", jcr);
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(Constants.Cache.CLEANCACHE_TOPIC);
		if(logger.isDebugEnabled()) {
			logger.debug("JsonArgs from Call : {}", Arrays.toString(jsonArgs.toArray(new String[jsonArgs.size()])));
			logger.debug("ParamName from considerated method : {}", Arrays.toString(paramNames.toArray(new String[paramNames.size()])));
		}
		String argpart = cacheArgumentServices.computeArgPart(jcr.keys(), jsonArgs, paramNames);
		String cachekey = computeCacheKey(jcr.cls(), jcr.methodName(), argpart);
		messageToClient.setResponse(cachekey);
		wsEvent.fire(messageToClient);
		cacheEvent.fire(cachekey);
	}

	/**
	 * Compute the cache key from classname, methodname, and args
	 * @param cls
	 * @param methodName
	 * @param argpart
	 * @return 
	 */
	String computeCacheKey(Class cls, String methodName, String argpart) {
		String cachekey = keyMaker.getMd5(cls.getName() + "." + methodName);
		if (!argpart.isEmpty()) {
			cachekey += "_" + keyMaker.getMd5(argpart);
		}
		logger.debug("CACHEID : {}.{}_{} = {}", cls.getName(), methodName, argpart, cachekey);
		return cachekey;
	}
}
