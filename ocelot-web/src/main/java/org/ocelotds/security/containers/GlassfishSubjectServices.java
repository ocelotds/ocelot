/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security.containers;

import java.security.Principal;
import javax.inject.Inject;
import javax.security.auth.Subject;
import org.ocelotds.Constants;
import org.ocelotds.logger.OcelotLogger;
import org.ocelotds.annotations.ContainerQualifier;
import org.ocelotds.security.SecurityContext;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@ContainerQualifier(Constants.Container.GLASSFISH)
public class GlassfishSubjectServices implements ContainerSubjectServices {
	
	@Inject
	@OcelotLogger
	private Logger logger;

	@Override
	public SecurityContext getSecurityContext() {
		com.sun.enterprise.security.SecurityContext current = com.sun.enterprise.security.SecurityContext.getCurrent();
		return new GlassfishSecurityContext(current.getCallerPrincipal(), current.getSubject());
	}

	@Override
	public void setSecurityContext(SecurityContext securityContext) {
		com.sun.enterprise.security.SecurityContext context = new com.sun.enterprise.security.SecurityContext(securityContext.getSubject());
		com.sun.enterprise.security.SecurityContext.setCurrent(context);
	}
	
	/**
	 * private glassfish implementation
	 */
	static class GlassfishSecurityContext implements org.ocelotds.security.SecurityContext {

		private final Principal principal;
		private final Subject subject;

		public GlassfishSecurityContext(Principal principal, Subject subject) {
			this.principal = principal;
			this.subject = subject;
		}
		

		@Override
		public Principal getPrincipal() {
			return principal;
		}

		@Override
		public Subject getSubject() {
			return subject;
		}
		@Override
		public String toString() {
			return "{\"principal\":"+principal+",\"subject\":"+subject+"}";
		}
	}
}
