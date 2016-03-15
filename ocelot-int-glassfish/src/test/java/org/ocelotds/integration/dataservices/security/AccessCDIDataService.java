/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
