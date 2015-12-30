/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

import java.io.Serializable;
import java.lang.reflect.Method;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import org.ocelotds.annotations.OcelotLogger;
import org.slf4j.Logger;
import org.ocelotds.annotations.RolesAllowed;
import org.ocelotds.context.OcelotContext;

/**
 * This class apply security check
 * @author hhfrancois
 */
@Interceptor
@RolesAllowed
public class RolesAllowedInterceptor implements Serializable {

	private static final long serialVersionUID = -849762977471230875L;

	@Inject
	@OcelotLogger
	private transient Logger logger;
	
	@Inject
	private OcelotContext ocelotContext;

	/**
	 *
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	@AroundInvoke
	public Object checkRolesAllowed(InvocationContext ctx) throws Exception {
		Method method = ctx.getMethod();
		String methodid = String.format("%s.%s", method.getDeclaringClass().getSimpleName(), method.getName());
		RolesAllowed rolesAllowedAnno = method.getAnnotation(RolesAllowed.class);
		String[] rolesAllowed = rolesAllowedAnno.value();
		for (String roleAllowed : rolesAllowed) {
			if(ocelotContext.isUserInRole(roleAllowed)) {
				logger.debug("Check method {} : role {} is allowed", methodid, roleAllowed);
				return ctx.proceed();
			}
		}
		throw new IllegalAccessException("'"+ocelotContext.getPrincipal()+"' is not allowed to execute "+methodid);
	}
}
