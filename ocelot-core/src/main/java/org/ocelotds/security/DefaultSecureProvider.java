/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

import javax.inject.Inject;
import org.ocelotds.context.OcelotContext;
import org.ocelotds.security.InvocationContext;

/**
 *
 * @author hhfrancois
 */
public class DefaultSecureProvider implements SecureProvider {
	
	@Inject
	OcelotContext context;

	@Override
	public void checkAccess(InvocationContext ctx, String[] roles) throws IllegalAccessException {
		boolean allowed = roles.length == 0;
		for (String role : roles) {
			allowed |= context.isUserInRole(role);
			if(allowed) {
				break;
			}
		}
		if(!allowed) {
			throw new IllegalAccessException(context.getPrincipal().getName()+" no access to "+ctx.getMethod().getDeclaringClass().getName()+"."+ctx.getMethod().getName());
		}
	}
	
}
