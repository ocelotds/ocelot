/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.dashboard.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotConfiguration;
import org.ocelotds.annotations.OcelotLogger;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@ApplicationScoped
public class RoleConfigurationManager {

	private final Collection<String> roles = new HashSet<>();

	@Inject
	@OcelotLogger
	private Logger logger;

	@Any
	@Inject
	@OcelotConfiguration(Constants.Options.DASHBOARD_ROLES)
	private Instance<Collection<String>> ocelotConfigurationRoles;

	@Any
	@Inject
	@OcelotConfiguration(Constants.Options.DASHBOARD_ROLES)
	private Instance<String> ocelotConfigurationRole;

	/**
	 * Read in web.xml and differents producers the optional DASHBOARD_ROLES config and set it in OcelotConfiguration
	 *
	 * @param sc
	 */
	public void readDashboardRolesConfig(@Observes @Initialized(ApplicationScoped.class) ServletContext sc) {
		readFromConfigurationRoles();
		readFromConfigurationRole();
		readFromInitParameter(sc);
		logger.debug("'{}' value : '{}'.", Constants.Options.DASHBOARD_ROLES, roles);
	}

	void readFromConfigurationRoles() {
		if (!ocelotConfigurationRoles.isUnsatisfied()) {
			Collection<String> get = ocelotConfigurationRoles.get();
			logger.debug("Read '{}' option from Producer Collection<String> : '{}'.", Constants.Options.DASHBOARD_ROLES, get);
			roles.addAll(get);
		}
	}

	void readFromConfigurationRole() {
		if (!ocelotConfigurationRole.isUnsatisfied()) {
			String get = ocelotConfigurationRole.get();
			logger.debug("Read '{}' option from Producer String : '{}'.", Constants.Options.DASHBOARD_ROLES, get);
			roles.add(get);
		}
	}

	void readFromInitParameter(ServletContext sc) {
		String param = sc.getInitParameter(Constants.Options.DASHBOARD_ROLES);
		if (param != null) {
			logger.debug("Read '{}' option from web.xml : '{}'.", Constants.Options.DASHBOARD_ROLES, param);
			roles.addAll(Arrays.asList(param.split(Constants.Options.SEPARATOR)));
		}
	}

	public Collection<String> getRoles() {
		return roles;
	}

}
