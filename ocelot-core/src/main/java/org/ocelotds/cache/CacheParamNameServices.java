/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.cache;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotLogger;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@ApplicationScoped
public class CacheParamNameServices {
	Map<String, List<String>> map = new HashMap();
	
	@Inject
	@OcelotLogger
	private Logger logger;

	public List<String> getMethodParamNames(Class cls, String methodName) {
		String key = cls.getName()+"."+methodName;
		if(!map.containsKey(key)) {
			String fn = cls.getSimpleName()+".properties";
			List<String> result = new ArrayList();
			try (InputStream in = cls.getResourceAsStream(fn);) {
				Properties properties = new Properties();
				properties.load(in);
				String property = properties.getProperty(key);
				result = Arrays.asList(property.split(Constants.Cache.PARAMNAME_SEPARATOR));
			} catch (Throwable ex) {
				logger.error("Error, loading "+key+"/"+fn, ex);
			}
			map.put(key, result);
		}
		return map.get(key);
	}
}
