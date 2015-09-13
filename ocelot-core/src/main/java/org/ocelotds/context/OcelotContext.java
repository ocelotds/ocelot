/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Locale;
import javax.inject.Inject;
import org.ocelotds.Constants;
import org.ocelotds.logger.OcelotLogger;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
public class OcelotContext {

	@Inject
	@OcelotLogger
	private Logger logger;
	
	@Inject
	Principal principal;

	public Locale getLocale() {
		Locale locale = (Locale) ThreadLocalContextHolder.get(Constants.LOCALE);
		if (null == locale) {
			logger.debug("Get locale from OcelotServices : default");
			locale = new Locale("en", "US");
		}
		logger.debug("Get locale from OcelotServices : {}", locale);
		return locale;
	}

	public String getUsername() {
		return principal.getName();
	}

	public boolean isUserInRole(String role) {
		try {
			Object request = ThreadLocalContextHolder.get(Constants.REQUEST);
			if (null == request) {
				return true;
			}
			Method method = request.getClass().getMethod("isUserInRole", String.class);
			return (boolean) method.invoke(request, role);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
		}
		return true;
	}
}
