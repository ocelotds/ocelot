/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.cache;

import java.io.IOException;
import java.io.InputStream;
import org.ocelotds.annotations.JsCacheRemove;
import org.ocelotds.annotations.JsCacheRemoveAll;
import org.ocelotds.annotations.JsCacheRemoves;
import org.ocelotds.annotations.JsCacheResult;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.inject.Inject;
import org.ocelotds.annotations.OcelotLogger;
import org.slf4j.Logger;

/**
 * Class managing frond-end ccache
 *
 * @author hhfrancois
 */
public class CacheManager {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	private JsCacheAnnotationServices jsCacheAnnotationServices;
	
	@Inject
	private CacheParamNameServices cacheParamNameServices;

	/**
	 * Process annotations JsCacheResult, JsCacheRemove and JsCacheRemoves
	 * @param nonProxiedMethod
	 * @param parameters
	 * @return 
	 */
	public long processCacheAnnotations(Method nonProxiedMethod, List<String> parameters) {
		processCleanCacheAnnotations(nonProxiedMethod, parameters);
		if (isJsCached(nonProxiedMethod)) {
			return jsCacheAnnotationServices.getJsCacheResultDeadline(nonProxiedMethod.getAnnotation(JsCacheResult.class));
		}
		return 0L;
	}

	/**
	 * Check if result should be cached in front-end
	 *
	 * @param nonProxiedMethod : method non proxified
	 * @return boolean
	 */
	boolean isJsCached(Method nonProxiedMethod) {
		boolean cached = nonProxiedMethod.isAnnotationPresent(JsCacheResult.class);
		logger.debug("The result of the method {} should be cached on client side {}.", nonProxiedMethod.getName(), cached);
		return cached;
	}

	/**
	 * Process annotations JsCacheRemove and JsCacheRemoves
	 *
	 * @param nonProxiedMethod
	 * @param jsonArgs
	 */
	void processCleanCacheAnnotations(Method nonProxiedMethod, List<String> jsonArgs) {
		boolean cleanAllCache = nonProxiedMethod.isAnnotationPresent(JsCacheRemoveAll.class);
		if (cleanAllCache) {
			jsCacheAnnotationServices.processJsCacheRemoveAll();
			logger.debug("Method {} removed all cache{} entries on all clients.");
		}
		boolean simpleCleancache = nonProxiedMethod.isAnnotationPresent(JsCacheRemove.class);
		boolean multiCleancache = nonProxiedMethod.isAnnotationPresent(JsCacheRemoves.class);
		if (simpleCleancache || multiCleancache) {
			List<String> paramNames = cacheParamNameServices.getMethodParamNames(nonProxiedMethod.getDeclaringClass(), nonProxiedMethod.getName());
			if (simpleCleancache) {
				JsCacheRemove jcr = nonProxiedMethod.getAnnotation(JsCacheRemove.class);
				jsCacheAnnotationServices.processJsCacheRemove(jcr, paramNames, jsonArgs);
			}
			if (multiCleancache) {
				JsCacheRemoves jcrs = nonProxiedMethod.getAnnotation(JsCacheRemoves.class);
				for (JsCacheRemove jcr : jcrs.value()) {
					jsCacheAnnotationServices.processJsCacheRemove(jcr, paramNames, jsonArgs);
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Method {} removed related cache{} entr{} on all clients.", nonProxiedMethod.getName(), multiCleancache ? "s" : "", multiCleancache ? "ies" : "y");
			}
		}
	}
}
