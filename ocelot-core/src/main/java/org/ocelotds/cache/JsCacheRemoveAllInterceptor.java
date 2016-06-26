/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.cache;

import java.io.Serializable;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import org.ocelotds.annotations.JsCacheRemoveAll;
import org.ocelotds.annotations.OcelotLogger;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@Interceptor
@JsCacheRemoveAll
public class JsCacheRemoveAllInterceptor  implements Serializable {

	private static final long serialVersionUID = -8497629774754796875L;

	@Inject
	@OcelotLogger
	private transient Logger logger;
	
	@Inject
	private JsCacheAnnotationServices jsCacheAnnotationServices;
	/**
	 *
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	@AroundInvoke
	public Object processJsCacheRemove(InvocationContext ctx) throws Exception {
		jsCacheAnnotationServices.processJsCacheRemoveAll();
		return ctx.proceed();
	}
	
}
