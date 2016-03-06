/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.integration.dataservices.security;

import java.security.Principal;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
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
