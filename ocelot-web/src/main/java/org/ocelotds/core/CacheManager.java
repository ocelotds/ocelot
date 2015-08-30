/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core;

import org.ocelotds.Constants;
import org.ocelotds.annotations.JsCacheRemove;
import org.ocelotds.annotations.JsCacheRemoveAll;
import org.ocelotds.annotations.JsCacheRemoves;
import org.ocelotds.annotations.JsCacheResult;
import org.ocelotds.messaging.CacheEvent;
import org.ocelotds.messaging.MessageEvent;
import org.ocelotds.messaging.MessageToClient;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class managing frond-end ccache
 *
 * @author hhfrancois
 */
public class CacheManager {

	private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);

	@Inject
	@MessageEvent
	Event<MessageToClient> wsEvent;

	@Inject
	@CacheEvent
	Event<String> cacheEvent;

	/**
	 * Check if resultshould be cached in front-end
	 *
	 * @param nonProxiedMethod
	 * @return
	 */
	public boolean isJsCached(Method nonProxiedMethod) {
		boolean cached = nonProxiedMethod.isAnnotationPresent(JsCacheResult.class);
		logger.debug("The result of the method {} should be cached on client side {}.", nonProxiedMethod.getName(), cached);
		return cached;
	}

	/**
	 * Get deadline for cache if 0 : is 1 year cache
	 *
	 * @param jcr
	 * @return
	 */
	public long getJsCacheResultDeadline(JsCacheResult jcr) {
		Calendar deadline = Calendar.getInstance();
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
	 * Process annotations JsCacheRemove and JsCacheRemoves
	 *
	 * @param nonProxiedMethod
	 * @param paramNames
	 * @param jsonArgs
	 */
	public void processCleanCacheAnnotations(Method nonProxiedMethod, List<String> paramNames, List<String> jsonArgs) {
		boolean cleanAllCache = nonProxiedMethod.isAnnotationPresent(JsCacheRemoveAll.class);
		if (cleanAllCache) {
			JsCacheRemoveAll jcra = nonProxiedMethod.getAnnotation(JsCacheRemoveAll.class);
			processJsCacheRemoveAll(jcra);
		}
		boolean simpleCleancache = nonProxiedMethod.isAnnotationPresent(JsCacheRemove.class);
		if (simpleCleancache) {
			JsCacheRemove jcr = nonProxiedMethod.getAnnotation(JsCacheRemove.class);
			processJsCacheRemove(jcr, paramNames, jsonArgs);
		}
		boolean multiCleancache = nonProxiedMethod.isAnnotationPresent(JsCacheRemoves.class);
		if (multiCleancache) {
			JsCacheRemoves jcrs = nonProxiedMethod.getAnnotation(JsCacheRemoves.class);
			for (JsCacheRemove jcr : jcrs.value()) {
				processJsCacheRemove(jcr, paramNames, jsonArgs);
			}
		}
		if (simpleCleancache || multiCleancache) {
			logger.debug("The method {} will remove cache{} entr{} on clients side.", nonProxiedMethod.getName(), multiCleancache ? "s" : "", multiCleancache ? "ies" : "y");
		}
	}

	/**
	 * Process annotation JsCacheRemoveAll and send message for suppress all the cache
	 *
	 * @param jcra : the annotation
	 */
	private void processJsCacheRemoveAll(JsCacheRemoveAll jcra) {
		logger.debug("Process JsCacheRemoveAll annotation : {}", jcra);
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(Constants.Cache.CLEANCACHE_TOPIC);
		messageToClient.setResponse("ALL");
		wsEvent.fire(messageToClient);
	}

	/**
	 * Process an annotation JsCacheRemove and send a removeCache message to all clients connected
	 *
	 * @param jcr : l'annotation
	 * @param paramNames : name of parameters
	 * @param jsonArgs : method arguments json format
	 */
	private void processJsCacheRemove(JsCacheRemove jcr, List<String> paramNames, List<String> jsonArgs) {
		logger.debug("Process JsCacheRemove annotation : {}", jcr);
		StringBuilder sb;
		MessageToClient messageToClient = new MessageToClient();
		if(logger.isDebugEnabled()) {
			logger.debug("JsonArgs from Call : {}", Arrays.toString(jsonArgs.toArray(new String[jsonArgs.size()])));
			logger.debug("ParamName from considerated method : {}", Arrays.toString(paramNames.toArray(new String[paramNames.size()])));
		}
		String[] keys = jcr.keys();
		if (keys.length == 0) {
			sb = new StringBuilder("");
		} else {
			if (Constants.Cache.USE_ALL_ARGUMENTS.equals(keys[0])) {
				sb = new StringBuilder(Arrays.toString(jsonArgs.toArray(new String[jsonArgs.size()])));
			} else {
				sb = new StringBuilder("[");
				for (int idKey = 0; idKey < keys.length; idKey++) {
					String key = keys[idKey];
					logger.debug("Process {} : ", key);
					String[] path = key.split("\\.");
					logger.debug("Process '{}' : token nb '{}'", key, path.length);
					String paramName = path[0];
					logger.debug("Looking for index of param '{}'", paramName);
					int idx = paramNames.indexOf("\"" + paramName + "\"");
					logger.debug("Index of param '{}' : '{}'", paramName, idx);
					String jsonArg = jsonArgs.get(idx);
					logger.debug("Param '{}' : '{}'", paramName, jsonArg);
					if (path.length > 1) {
						try (JsonReader reader = Json.createReader(new StringReader(jsonArg))) {
							JsonValue jsonObject = reader.readObject();
							for (int i = 1; i < path.length; i++) {
								String p = path[i];
								if (!(jsonObject instanceof JsonObject)) {
									logger.error("Impossible to get " + p + " on " + jsonObject.toString() + ". It's not an json objet.");
								}
								logger.debug("Access to '{}' for '{}'", p, jsonObject.toString());
								jsonObject = ((JsonObject) jsonObject).get(p);
							}
							jsonArg = jsonObject.toString();
						} catch (JsonParsingException exception) {
							logger.warn("Fail to access to field for '{}'", jsonArg);
						}
					}
					logger.debug("Add value for '{}' : '{}' to builder cache key", key, jsonArg);
					sb.append(jsonArg);
					if (idKey + 1 < keys.length) {
						sb.append(",");
					}
				}
				sb.append("]");
			}
		}
		messageToClient.setId(Constants.Cache.CLEANCACHE_TOPIC);
		String cachekey = getMd5(jcr.cls().getName() + "." + jcr.methodName());
		if (sb.length() > 0) {
			cachekey += "_" + getMd5(sb.toString());
		}
		messageToClient.setResponse(cachekey);
		logger.debug("CACHEID : {}.{}_{} = {}", jcr.cls().getName(), jcr.methodName(), sb.toString(), cachekey);
		wsEvent.fire(messageToClient);
		cacheEvent.fire(cachekey);
	}

	/**
	 * Create a md5 from string
	 *
	 * @param msg
	 * @return
	 */
	private String getMd5(String msg) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(msg.getBytes(Constants.UTF_8));
			//converting byte array to Hexadecimal String
			StringBuilder sb = new StringBuilder(2 * hash.length);
			for (byte b : hash) {
				sb.append(String.format("%02x", b & 0xff));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
			logger.error("Fail to get MD5 of String "+msg, ex);
		}
		return null;
	}
}
