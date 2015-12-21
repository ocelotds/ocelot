/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.cache;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotLogger;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
public class CacheArgumentServices {

	@Inject
	@OcelotLogger
	private Logger logger;

	/**
	 * Compute the part of cache key that depends of arguments
	 *
	 * @param keys : key from annotation
	 * @param jsonArgs : actual args
	 * @param paramNames : parameter name of concern method
	 * @return
	 */
	public String computeArgPart(String[] keys, List<String> jsonArgs, List<String> paramNames) {
		if (keys.length == 0) {
			return "";
		} else if (Constants.Cache.USE_ALL_ARGUMENTS.equals(keys[0])) {
			return Arrays.toString(jsonArgs.toArray(new String[jsonArgs.size()]));
		} else {
			return computeSpecifiedArgPart(keys, jsonArgs, paramNames);
		}
	}

	/**
	 * Compute the part of cache key that depends of arguments
	 *
	 * @param keys : key from annotation
	 * @param jsonArgs : actual args
	 * @param paramNames : parameter name of concern method
	 * @return
	 */
	String computeSpecifiedArgPart(String[] keys, List<String> jsonArgs, List<String> paramNames) {
		StringBuilder sb = new StringBuilder("[");
		boolean first = true;
		for (String key : keys) {
			if (!first) {
				sb.append(",");
			}
			first = false;
			String[] path = key.split("\\.");
			logger.debug("Process '{}' : {} token(s)", key, path.length);
			String paramName = path[0];
			int idx = paramNames.indexOf("\"" + paramName + "\"");
			logger.debug("Index of param '{}' : '{}'", paramName, idx);
			String jsonArg = jsonArgs.get(idx);
			logger.debug("Param '{}' : '{}'", paramName, jsonArg);
			sb.append(processArg(path, jsonArg));
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Extract considerate value from json arg for compute cache key and function of key<br>
	 * Example : if key = "a.i" and jsonarg = {\"i\":5} then return 5
	 *
	 * @param key
	 * @param jsonArg
	 * @return
	 */
	String processArg(String[] path, String jsonArg) {
		String keyArg = jsonArg;
		if (path.length > 1) {
			String[] subpath = Arrays.copyOfRange(path, 1, path.length);
			try {
				keyArg = processSubFieldsOfArg(subpath, jsonArg);
			} catch (Throwable ex) {
				logger.warn("Fail to access to field for '{}'", jsonArg, ex);
			}
		}
		return keyArg;
	}

	String processSubFieldsOfArg(String[] path, String jsonArg) {
		try (JsonReader reader = Json.createReader(new StringReader(jsonArg))) {
			JsonValue jsonObject = reader.readObject();
			for (String p : path) {
				logger.debug("Access to '{}' for '{}'", p, jsonObject.toString());
				jsonObject = ((JsonObject) jsonObject).get(p);
			}
			return jsonObject.toString();
		}
	}
}
