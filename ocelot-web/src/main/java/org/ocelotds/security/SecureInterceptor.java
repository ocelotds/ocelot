/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

import java.lang.reflect.Method;
import javax.annotation.Priority;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * This class transform a simple method to chanel for topic
 *
 * @author hhfrancois
 */
@Interceptor
@Priority(0)
@OcelotSecured
public class SecureInterceptor {

	@Inject
	@Any
	Instance<SecureProvider> providers;

	/**
	 *
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	@AroundInvoke
	public Object processSecure(InvocationContext ctx) throws Exception {
		OcelotSecured ocelotSecured = getOcelotSecuredAnnotation(ctx.getMethod());
		checkAccess(ctx, 
				  getSecureProviderImpl(ocelotSecured.provider()), 
				  ocelotSecured.roles());
		return ctx.proceed();
	}

	OcelotSecured getOcelotSecuredAnnotation(Method method) {
		if (method.isAnnotationPresent(OcelotSecured.class)) {
			return method.getAnnotation(OcelotSecured.class);
		} else {
			return method.getDeclaringClass().getAnnotation(OcelotSecured.class);
		}
	}

	org.ocelotds.security.InvocationContext checkAccess(InvocationContext ctx, SecureProvider secureProvider, String[] roles) throws IllegalAccessException {
		if (secureProvider != null) {
			org.ocelotds.security.InvocationContext ic = new org.ocelotds.security.InvocationContext(ctx.getMethod(), ctx.getParameters());
			secureProvider.checkAccess(ic, roles);
			return ic;
		}
		return null;
	}

	SecureProvider getSecureProviderImpl(Class<? extends SecureProvider> providerClass) {
		if (!providers.select(providerClass).isUnsatisfied()) {
			return providers.select(providerClass).get();
		}
		return null;
	}

}
