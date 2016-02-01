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
import org.ocelotds.annotations.RolesAllowed;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.CDI)
public class AccessDataService {
	
	@Inject
	Principal principal;
	
	@RolesAllowed("TESTR")
	public  void methodAllowedToTest() {
	}

	@RolesAllowed("USERR")
	public  void methodAllowedToUSer() {
	}
	
	@RolesAllowed("ADMINR")
	public void methodAllowedToAdmin() {
	}
	
	public String getUsername() {
		System.out.println("PRINCIPAL "+principal);
		return principal.getName();
	}
}
