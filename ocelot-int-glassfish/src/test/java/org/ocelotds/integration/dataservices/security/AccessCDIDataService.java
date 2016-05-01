/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.integration.dataservices.security;

import java.security.Principal;
import javax.inject.Inject;
import org.ocelotds.Constants;
import org.ocelotds.annotations.DataService;
import org.ocelotds.context.OcelotContext;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.CDI)
public class AccessCDIDataService {
	
	@Inject
	Principal principal;
	
	@Inject
	private OcelotContext ocelotContext;
	
	public String getPrincipalName() {
		return principal.getName();
	}

	public String getOcelotContextName() {
		return ocelotContext.getPrincipal().getName();
	}

	public boolean isUserInRole(String role) {
		return ocelotContext.isUserInRole(role);
	}
}
