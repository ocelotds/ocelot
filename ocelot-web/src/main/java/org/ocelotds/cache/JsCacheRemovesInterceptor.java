/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.cache;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import org.ocelotds.annotations.JsCacheRemove;
import org.ocelotds.annotations.JsCacheRemoves;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.marshalling.ArgumentServices;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@Interceptor
@Priority(0)
@JsCacheRemoves(value = {})
public class JsCacheRemovesInterceptor  implements Serializable {

	private static final long serialVersionUID = -8497629774754796875L;

	@Inject
	@OcelotLogger
	private transient Logger logger;
	
	@Inject
	private CacheParamNameServices cacheParamNameServices;
	
	@Inject
	private JsCacheAnnotationServices jsCacheAnnotationServices;
	
	@Inject
	ArgumentServices argumentServices;
	
	/**
	 *
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	@AroundInvoke
	public Object processJsCacheRemoves(InvocationContext ctx) throws Exception {
		Method method = ctx.getMethod();
		List<String> jsonArgs = argumentServices.getJsonParameters(ctx.getParameters(), method.getParameterAnnotations());
		List<String> paramNames = cacheParamNameServices.getMethodParamNames(method.getDeclaringClass(), method.getName());
		JsCacheRemoves jcrs = method.getAnnotation(JsCacheRemoves.class);
		for (JsCacheRemove jcr : jcrs.value()) {
			jsCacheAnnotationServices.processJsCacheRemove(jcr, paramNames, jsonArgs);
		}
		return ctx.proceed();
	}
	
}
