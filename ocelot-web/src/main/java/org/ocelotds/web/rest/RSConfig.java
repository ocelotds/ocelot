/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web.rest;

import java.util.HashSet;
import java.util.Set;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.annotations.OcelotResource;
import org.ocelotds.core.UnProxyClassServices;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@ApplicationPath("ocelot")
public class RSConfig extends Application {

	@Inject
	@Any
	@OcelotResource
	Instance<Object> restEndpoints;
	
	@Inject
	UnProxyClassServices unProxyClassServices;

	@Inject
	@OcelotLogger
	Logger logger;

	Set<Class<?>> getHashSet() {
		return new HashSet<>();
	}

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> resources = getHashSet();
		addRestResourceClasses(resources);
		return resources;
	}

	/**
	 * Do not modify addRestResourceClasses() method. It is automatically populated with all resources defined in the project. If required, comment out calling
	 * this method in getClasses().
	 */
	void addRestResourceClasses(Set<Class<?>> resources) {
		logger.info("Register ocelot resources...");
		for (Object restEndpoint : restEndpoints) {
			Class cls = unProxyClassServices.getRealClass(restEndpoint.getClass());
			logger.info("Register ocelot resource {}", cls.getName());
			resources.add(cls);
		}
	}

}
