/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

import org.ocelotds.security.containers.ContainerSubjectServices;
import java.security.Principal;
import java.util.regex.Pattern;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.security.auth.Subject;
import org.ocelotds.annotations.ContainerQualifier;
import org.ocelotds.logger.OcelotLogger;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@Singleton
public class SubjectServices {

	@Any
	@Inject
	private Instance<ContainerSubjectServices> instances;

	@Inject
	@OcelotLogger
	private Logger logger;

	private ContainerSubjectServices current = null;

	/**
	 * Define the current provider for the current container
	 *
	 * @param serverInfo
	 */
	public void setServerInfo(String serverInfo) {
		for (ContainerSubjectServices instance : instances) {
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
			logger.info("No ContainerSubjectServices implementation found in classpath for server '{}'. Implement it or contact ocelot team leader for implements it.", serverInfo);
		}
	}

	/**
	 * get current ContainerSubjectServices using the container implementation
	 *
	 * @return
	 */
	public ContainerSubjectServices getContainerSubjectServices() {
		return current;
	}

	/**
	 * get current Subject using the container implementation
	 *
	 * @return
	 */
	public Subject getSubject() {
		ContainerSubjectServices c = getContainerSubjectServices();
		try {
			if (c != null) {
				return c.getSubject();
			}
		} catch (Exception ex) {
		}
		return null;
	}

	/**
	 * set current Subject using the container implementation
	 *
	 * @param subject
	 * @param principal
	 */
	public void setSubject(Subject subject, Principal principal) {
		ContainerSubjectServices c = getContainerSubjectServices();
		try {
			if (c != null) {
				c.setSubject(subject, principal);
			}
		} catch (Exception ex) {
		}
	}
}
