/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.Principal;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.websocket.server.HandshakeRequest;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotLogger;
import org.slf4j.Logger;
import org.ocelotds.annotations.RolesAllowed;
import org.ocelotds.context.ThreadLocalContextHolder;

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
	private Principal principal;

	/**
	 *
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	@AroundInvoke
	public Object checkRolesAllowed(InvocationContext ctx) throws Exception {
		Method method = ctx.getMethod();
		RolesAllowed rolesAllowedAnno = method.getAnnotation(RolesAllowed.class);
		String[] rolesAllowed = rolesAllowedAnno.value();
		HandshakeRequest handshakeRequest = getHandshakeRequest();
		for (String roleAllowed : rolesAllowed) {
			if(handshakeRequest.isUserInRole(roleAllowed)) {
				if(logger.isDebugEnabled()) {
					logger.debug("Check method {}.{} role {} is allowed", method.getDeclaringClass().getSimpleName(), method.getName(), roleAllowed);
				}
				return ctx.proceed();
			}
		}
		throw new IllegalAccessException("'"+principal+"' is not allowed "+method.getDeclaringClass().getSimpleName()+"."+method.getName());
	}

	HandshakeRequest getHandshakeRequest() {
		return (HandshakeRequest) ThreadLocalContextHolder.get(Constants.HANDSHAKEREQUEST);
	}
}
