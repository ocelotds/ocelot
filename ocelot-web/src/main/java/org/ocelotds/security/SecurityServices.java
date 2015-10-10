/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

import org.ocelotds.security.containers.ContainerSecurityServices;
import java.util.regex.Pattern;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.ocelotds.Constants;
import org.ocelotds.annotations.ContainerQualifier;
import org.ocelotds.annotations.OcelotLogger;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@Singleton
public class SecurityServices {

	@Any
	@Inject
	private Instance<ContainerSecurityServices> instances;

	@Inject
	@ContainerQualifier(Constants.Container.UNKNOWN)
	private ContainerSecurityServices unknown;

	@Inject
	@OcelotLogger
	private Logger logger;

	private ContainerSecurityServices current = null;

	/**
	 * Define the current provider for the current container
	 *
	 * @param serverInfo
	 */
	public void setServerInfo(String serverInfo) {
		for (ContainerSecurityServices instance : instances) {
			ContainerQualifier annotation = instance.getClass().getAnnotation(ContainerQualifier.class);
			String name = annotation.value();
			logger.debug("Container vendor {} candidate", name);
			Pattern p = Pattern.compile(".*" + name + ".*", Pattern.CASE_INSENSITIVE);
			if (p.matcher(serverInfo).matches()) {
				logger.info("Container vendor {}", name);
				current = instance;
				break;
			}
		}
		if (null == current) {
			current = unknown;
			logger.info("No ContainerSubjectServices implementation found in classpath for server '{}'. Implement it or contact ocelot team leader for implements it.", serverInfo);
		}
	}

	/**
	 * get current ContainerSecurityServices using the container implementation
	 *
	 * @return
	 */
	ContainerSecurityServices getContainerSubjectServices() {
		return current;
	}

	/**
	 * get current Subject using the container implementation
	 *
	 * @return
	 */
	public SecurityContext getSecurityContext() {
		ContainerSecurityServices c = getContainerSubjectServices();
		try {
			if (c != null) {
				return c.getSecurityContext();
			}
		} catch (Exception ex) {
		}
		return null;
	}

	/**
	 * set current Subject using the container implementation
	 *
	 * @param securityContext
	 */
	public void setSecurityContext(SecurityContext securityContext) {
		ContainerSecurityServices c = getContainerSubjectServices();
		try {
			if (c != null) {
				c.setSecurityContext(securityContext);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
