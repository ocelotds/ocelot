/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.weblogic;

import org.ocelotds.spi.security.ContainerSecurityServices;
import java.security.Principal;
import javax.inject.Inject;
import javax.security.auth.Subject;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.annotations.ContainerQualifier;
import org.slf4j.Logger;

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
		return new WeblogicSecurityContext(null, null, null);
	}

	/**
	 *
	 * @param securityContext
	 */
	@Override
	public void setSecurityContext(org.ocelotds.spi.security.SecurityContext securityContext) {
		final WeblogicSecurityContext context = (WeblogicSecurityContext) securityContext;
	}

	/**
	 * Private implementation for wildfly
	 */
	static class WeblogicSecurityContext implements org.ocelotds.spi.security.SecurityContext {

		private final Principal principal;
		private final Subject subject;
		private final Object credential;

		public WeblogicSecurityContext(Principal principal, Subject subject, Object credential) {
			this.principal = principal;
			this.subject = subject;
			this.credential = credential;
		}

		public Principal getPrincipal() {
			return principal;
		}

		public Subject getSubject() {
			return subject;
		}

		public Object getCredential() {
			return credential;
		}

		@Override
		public String toString() {
			return "{\"principal\":" + principal + ",\"subject\":" + subject + ",\"credential\":" + credential + "}";
		}
	}
}
