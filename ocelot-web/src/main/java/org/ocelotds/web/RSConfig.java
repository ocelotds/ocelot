/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.web;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 *
 * @author hhfrancois
 */
@ApplicationPath("ocelot")
public class RSConfig extends Application {

	Set<Class<?>>  getHashSet() {
		return new HashSet<>();
	}
	
	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> resources = getHashSet();
		addRestResourceClasses(resources);
		return resources;
	}

	/**
	 * Do not modify addRestResourceClasses() method.
	 * It is automatically populated with
	 * all resources defined in the project.
	 * If required, comment out calling this method in getClasses().
	 */
	void addRestResourceClasses(Set<Class<?>> resources) {
		resources.add(org.ocelotds.web.RSEndpoint.class);
		resources.add(org.ocelotds.web.RsJsCore.class);
		resources.add(org.ocelotds.web.RsJsServices.class);
	}
	
}
