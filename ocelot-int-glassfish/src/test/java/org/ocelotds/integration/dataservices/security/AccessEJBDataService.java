/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.integration.dataservices.security;

import java.security.Principal;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.ocelotds.Constants;
import org.ocelotds.annotations.DataService;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.EJB)
@Stateless
public class AccessEJBDataService {
	
	@Inject
	Principal principal;
	
	@Resource
	private SessionContext sessionContext;
	
	@RolesAllowed("TESTR")
	public  void methodAllowedToTest() {
	}

	@RolesAllowed("USERR")
	public  void methodAllowedToUSer() {
	}
	
	@RolesAllowed("ADMINR")
	public void methodAllowedToAdmin() {
	}
	
	public String getPrincipalName() {
		return principal.getName();
	}

	public String getCallerName() {
		return sessionContext.getCallerPrincipal().getName();
	}

	public boolean isCallerInRole(String role) {
		return sessionContext.isCallerInRole(role);
	}
}
