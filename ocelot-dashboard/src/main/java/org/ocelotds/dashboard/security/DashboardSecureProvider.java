/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.dashboard.security;

import javax.inject.Inject;
import org.ocelotds.context.OcelotContext;
import org.ocelotds.security.InvocationContext;
import org.ocelotds.security.SecureProvider;

/**
 *
 * @author hhfrancois
 */
public class DashboardSecureProvider implements SecureProvider {
	
	@Inject
	OcelotContext context;
	
	@Inject
	RoleConfigurationManager rcm;
	
	@Override
	public void checkAccess(InvocationContext ctx, String[] roles) throws IllegalAccessException {
		boolean allowed = rcm.getRoles().isEmpty();
		for (String role : rcm.getRoles()) {
			allowed |= context.isUserInRole(role);
			if(allowed) {
				break;
			}
		}
		if(!allowed) {
			throw new IllegalAccessException(context.getPrincipal().getName()+" no access to "+ctx);
		}
	}
}
