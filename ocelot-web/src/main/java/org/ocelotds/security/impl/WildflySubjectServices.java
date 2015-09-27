/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import javax.inject.Inject;
import javax.security.auth.Subject;
import org.ocelotds.Constants;
import org.ocelotds.logger.OcelotLogger;
import org.ocelotds.annotations.ContainerQualifier;
import org.ocelotds.security.ContainerSubjectServices;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@ContainerQualifier(Constants.Container.WILDFLY)
public class WildflySubjectServices implements ContainerSubjectServices {
	
	private static final String SECURITY_CONTEXT_ASSOCIATION = "org.jboss.security.SecurityContextAssociation";
	private static final String IDENTITY_FACTORY = "org.jboss.security.identity.extensions.CredentialIdentityFactory";
	private static final String IDENTITY = "org.jboss.security.identity.Identity";
	private static final String SECURITY_CONTEXT = "org.jboss.security.SecurityContext";

	@Inject
	@OcelotLogger
	private Logger logger;

	@Override
	public Subject getSubject() {
		Subject subject = null;
		try {
			Class<?> secuCtxClass = Class.forName(SECURITY_CONTEXT_ASSOCIATION);
			Method getSubject = secuCtxClass.getMethod("getSubject");
			subject = (Subject) getSubject.invoke(null);
			logger.debug("Use wildfly implementation for get security context");
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
		}
		return subject;
	}

	@Override
	public void setSubject(Subject subject, Principal principal) {
		try {
			Class<?> identityClass = Class.forName(IDENTITY);
			Class<?> secuCtxAssClass = Class.forName(SECURITY_CONTEXT_ASSOCIATION);
			Class<?> factoryIdentityClass = Class.forName(IDENTITY_FACTORY);
			Class<?> secuCtxClass = Class.forName(SECURITY_CONTEXT);
			
			Method getSecurityContext = secuCtxAssClass.getMethod("getSecurityContext");
			Object securityContext = getSecurityContext.invoke(null);

			Method getUtil = secuCtxClass.getMethod("getUtil");
			Object util = getUtil.invoke(securityContext);

			Method createSubjectInfo = util.getClass().getMethod("createSubjectInfo", identityClass, Subject.class);
			Method getIdentity = factoryIdentityClass.getMethod("getIdentity", Principal.class, Object.class);

			Object identity = getIdentity.invoke(null, principal, null);
			createSubjectInfo.invoke(util, identity, subject);
			logger.debug("Use wildfly implementation for set security context");
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
}
