/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security.containers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import javax.inject.Inject;
import javax.security.auth.Subject;
import org.ocelotds.Constants;
import org.ocelotds.logger.OcelotLogger;
import org.ocelotds.annotations.ContainerQualifier;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@ContainerQualifier(Constants.Container.GLASSFISH)
public class GlassfishSubjectServices implements ContainerSubjectServices {
	
	private static final String SECURIT_CONTEXT = "com.sun.enterprise.security.SecurityContext";

	@Inject
	@OcelotLogger
	private Logger logger;

	@Override
	public Subject getSubject() throws Exception {
		Subject subject = null;
		Class<?> secuCtxClass = Class.forName(SECURIT_CONTEXT);
		Method getCurrent = secuCtxClass.getMethod("getCurrent");
		Object current = getCurrent.invoke(null);
		Method getSubject = current.getClass().getMethod("getSubject");
		subject = (Subject) getSubject.invoke(current);
		logger.debug("Use glassfish implementation for get security context");
		return subject;
	}

	@Override
	public void setSubject(Subject subject, Principal principal) throws Exception {
		Class<?> secuCtxClass = Class.forName(SECURIT_CONTEXT);
		Constructor constructor = secuCtxClass.getConstructor(Subject.class);
		Object secuCtx = constructor.newInstance(subject); // SecurityContext secuCtx = new SecurityContext(subject);
		Method setCurrent = secuCtxClass.getMethod("setCurrent", secuCtxClass);
		setCurrent.invoke(null, secuCtx); // SecurityContext.setCurrent(secuCtx);
		logger.debug("Use glassfish implementation for set security context");
	}
	
}
