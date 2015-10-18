/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

import org.ocelotds.security.containers.ContainerSecurityServices;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import org.ocelotds.annotations.ContainerQualifier;
import org.ocelotds.annotations.OcelotLogger;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@ApplicationScoped
public class SecurityServices {

	@Any
	@Inject
	private Instance<ContainerSecurityServices> instances; // contains at least the UnkownServerSecutityServices

	@Inject
	@OcelotLogger
	private Logger logger;

	private ContainerSecurityServices current = null;

	/**
	 * Define the current provider for the current container
	 *
	 * @param sc
	 */
	public void setServerInfo(@Observes @Initialized(ApplicationScoped.class) ServletContext sc) {
		String serverInfo = sc.getServerInfo();
		boolean found = false;
		for (ContainerSecurityServices instance : instances) {
			ContainerQualifier annotation = instance.getClass().getAnnotation(ContainerQualifier.class);
			if (annotation != null) {
				String name = annotation.value();
				logger.debug("Container vendor {} candidate", name);
				Pattern p = Pattern.compile(".*" + name + ".*", Pattern.CASE_INSENSITIVE);
				if (p.matcher(serverInfo).matches()) {
					logger.info("{} ContainerSubjectServices implementation found in classpath.", annotation.value());
					current = instance;
					found = true;
					break;
				}
			} else {
				current = instance; // set the default ContainerSecurityServices
			}
		}
		if (!found) {
			logger.info("No ContainerSubjectServices implementation found in classpath for current server. Implement it or contact ocelot team leader for implements it.");
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
