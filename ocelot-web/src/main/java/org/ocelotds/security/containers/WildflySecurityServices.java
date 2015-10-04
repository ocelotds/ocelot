/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security.containers;

import org.jboss.security.identity.Identity;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Set;
import javax.inject.Inject;
import javax.security.auth.Subject;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SecurityContextUtil;
import org.jboss.security.identity.Role;
import org.jboss.security.identity.extensions.CredentialIdentityFactory;
import org.jboss.security.identity.plugins.SimpleRoleGroup;
import org.ocelotds.Constants;
import org.ocelotds.logger.OcelotLogger;
import org.ocelotds.annotations.ContainerQualifier;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@ContainerQualifier(Constants.Container.WILDFLY)
public class WildflySecurityServices implements ContainerSecurityServices {

	@Inject
	@OcelotLogger
	private Logger logger;

	/**
	 *
	 * @return
	 */
	@Override
	public org.ocelotds.security.SecurityContext getSecurityContext() {
		return new WildflySecurityContext(SecurityContextAssociation.getPrincipal(), SecurityContextAssociation.getSubject(), SecurityContextAssociation.getCredential());
	}

	/**
	 *
	 * @param securityContext
	 */
	@Override
	public void setSecurityContext(org.ocelotds.security.SecurityContext securityContext) {
		final WildflySecurityContext context = (WildflySecurityContext) securityContext;
		final SecurityContext secuContext = SecurityContextAssociation.getSecurityContext();
		final Role roleGroup = getRoleGroup(context.getSubject());
		final Identity identity = CredentialIdentityFactory.createIdentity(context.getPrincipal(), context.getCredential(), roleGroup);
		if (null != secuContext) {
			SecurityContextUtil util = secuContext.getUtil();
			util.createSubjectInfo(identity, context.getSubject());
		}
	}

	/**
	 * Compute Role
	 *
	 * @param subject
	 * @return
	 */
	public static Role getRoleGroup(final Subject subject) {
		final Set<Group> groups = subject.getPrincipals(Group.class);
		for (Group group : groups) {
			if ("Roles".equals(group.getName())) {
				return new SimpleRoleGroup(group);
			}
		}
		return null;
	}

	/**
	 * Private implementation for wildfly
	 */
	static class WildflySecurityContext implements org.ocelotds.security.SecurityContext {

		private final Principal principal;
		private final Subject subject;
		private final Object credential;

		public WildflySecurityContext(Principal principal, Subject subject, Object credential) {
			this.principal = principal;
			this.subject = subject;
			this.credential = credential;
		}

		@Override
		public Principal getPrincipal() {
			return principal;
		}

		@Override
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
