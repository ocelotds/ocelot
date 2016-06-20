/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.cache;

import org.ocelotds.annotations.JsCacheRemove;
import org.ocelotds.annotations.JsCacheRemoveAll;
import org.ocelotds.annotations.JsCacheRemoves;
import org.ocelotds.annotations.JsCacheResult;
import java.lang.reflect.Method;
import java.util.List;
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

	/**
	 * Process annotations JsCacheResult, JsCacheRemove and JsCacheRemoves
	 * @param nonProxiedMethod
	 * @param parameterNames
	 * @param parameters
	 * @return 
	 */
	public long processCacheAnnotations(Method nonProxiedMethod, List<String> parameterNames, List<String> parameters) {
		processCleanCacheAnnotations(nonProxiedMethod, parameterNames, parameters);
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
	 * @param paramNames
	 * @param jsonArgs
	 */
	void processCleanCacheAnnotations(Method nonProxiedMethod, List<String> paramNames, List<String> jsonArgs) {
		boolean cleanAllCache = nonProxiedMethod.isAnnotationPresent(JsCacheRemoveAll.class);
		if (cleanAllCache) {
			jsCacheAnnotationServices.processJsCacheRemoveAll();
		}
		boolean simpleCleancache = nonProxiedMethod.isAnnotationPresent(JsCacheRemove.class);
		if (simpleCleancache) {
			JsCacheRemove jcr = nonProxiedMethod.getAnnotation(JsCacheRemove.class);
			jsCacheAnnotationServices.processJsCacheRemove(jcr, paramNames, jsonArgs);
		}
		boolean multiCleancache = nonProxiedMethod.isAnnotationPresent(JsCacheRemoves.class);
		if (multiCleancache) {
			JsCacheRemoves jcrs = nonProxiedMethod.getAnnotation(JsCacheRemoves.class);
			for (JsCacheRemove jcr : jcrs.value()) {
				jsCacheAnnotationServices.processJsCacheRemove(jcr, paramNames, jsonArgs);
			}
		}
		if (logger.isDebugEnabled() && (simpleCleancache || multiCleancache)) {
			logger.debug("The method {} will remove cache{} entr{} on clients side.", nonProxiedMethod.getName(), multiCleancache ? "s" : "", multiCleancache ? "ies" : "y");
		}
	}
}
