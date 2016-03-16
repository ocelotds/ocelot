/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.security;

import java.security.Principal;

/**
 *
 * @author hhfrancois
 */
public interface UserContext {

	public Principal getPrincipal();

	public boolean isUserInRole(String role);

}
