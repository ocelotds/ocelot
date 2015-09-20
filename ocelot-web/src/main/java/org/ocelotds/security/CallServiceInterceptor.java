/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.security.auth.Subject;
import javax.websocket.Session;
import org.ocelotds.Constants;
import org.ocelotds.context.ThreadLocalContextHolder;
import org.ocelotds.logger.OcelotLogger;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@CallService
@Interceptor
public class CallServiceInterceptor {

	@Inject
	@OcelotLogger
	private Logger logger;

	@AroundInvoke
	public Object intercept(InvocationContext ctx) throws Exception {
		final Object[] params = ctx.getParameters();
		Session session = null;
		for (Object param : params) {
			if (param instanceof Session) {
				session = (Session) param;
				break;
			}
		}
		logger.debug("Interceptor set security context for session {}", session);
		if (session != null) {
			Map<String, Object> sessionProperties = session.getUserProperties();
			// Get subject from config and set in session
			final Principal principal = (Principal) sessionProperties.get(Constants.PRINCIPAL);
			ThreadLocalContextHolder.put(Constants.PRINCIPAL, principal);
			final Locale locale = (Locale) sessionProperties.get(Constants.LOCALE);
			ThreadLocalContextHolder.put(Constants.LOCALE, locale);

			final Subject subject = (Subject) sessionProperties.get(Constants.SUBJECT);

			// Glassfish implementation
			try {
				Class<?> secuCtxClass = Class.forName("com.sun.enterprise.security.SecurityContext");
				Constructor constructor = secuCtxClass.getConstructor(Subject.class);
				Object secuCtx = constructor.newInstance(subject); // SecurityContext secuCtx = new SecurityContext(subject);
				Method setCurrent = secuCtxClass.getMethod("setCurrent", secuCtxClass);
				setCurrent.invoke(null, secuCtx); // SecurityContext.setCurrent(secuCtx);
				logger.debug("Use glassfish implementation for set security context");
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
		}

		return ctx.proceed();
	}

}
