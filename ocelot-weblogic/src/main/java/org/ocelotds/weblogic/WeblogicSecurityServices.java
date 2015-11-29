/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.weblogic;

import org.ocelotds.spi.security.ContainerSecurityServices;
import java.security.Principal;
import java.security.PrivilegedAction;
import javax.inject.Inject;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.annotations.ContainerQualifier;
import org.slf4j.Logger;
import sun.security.action.GetBooleanAction;
import weblogic.security.Security;

/**
 *
 * @author hhfrancois
 */
@ContainerQualifier(WeblogicSecurityServices.NAME)
public class WeblogicSecurityServices implements ContainerSecurityServices {

	protected static final String NAME = "WEBLOGIC";

	@Inject
	@OcelotLogger
	private Logger logger;

	/**
	 *
	 * @return
	 */
	@Override
	public org.ocelotds.spi.security.SecurityContext getSecurityContext() {
//		Subject subject = (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
		return new WeblogicSecurityContext(Security.getCurrentSubject());
	}

	/**
	 *
	 * @param securityContext
	 */
	@Override
	public void setSecurityContext(org.ocelotds.spi.security.SecurityContext securityContext) {
		final WeblogicSecurityContext context = (WeblogicSecurityContext) securityContext;
		Subject source = context.getSubject();
		
		Security.runAs(source, new GetBooleanAction("OTHER"));
	}

	/**
	 * Private implementation for wildfly
	 */
	static class WeblogicSecurityContext implements org.ocelotds.spi.security.SecurityContext {

		private final Subject subject;

		public WeblogicSecurityContext(Subject subject) {
			this.subject = subject;
		}

		public Subject getSubject() {
			return subject;
		}

		@Override
		public String toString() {
			return "{\"subject\":" + subject + "}";
		}
	}
}
