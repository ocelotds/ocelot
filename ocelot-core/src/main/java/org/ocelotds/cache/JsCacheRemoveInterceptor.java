/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import org.ocelotds.annotations.JsCacheRemove;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.marshalling.ArgumentServices;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@Interceptor
@JsCacheRemove(cls = JsCacheRemoveInterceptor.class, methodName = "")
public class JsCacheRemoveInterceptor  implements Serializable {

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
	public Object processJsCacheRemove(InvocationContext ctx) throws Exception {
		Method method = ctx.getMethod();
		List<String> jsonArgs = argumentServices.getJsonParameters(ctx.getParameters());
		List<String> paramNames = cacheParamNameServices.getMethodParamNames(method.getDeclaringClass(), method.getName());
		JsCacheRemove jcr = method.getAnnotation(JsCacheRemove.class);
		jsCacheAnnotationServices.processJsCacheRemove(jcr, paramNames, jsonArgs);
		return ctx.proceed();
	}
	
}
