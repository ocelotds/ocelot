/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.extension;

import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.i18n.Locale;
import fr.hhdev.ocelot.i18n.ThreadLocalContextHolder;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
@Contextualized
@Interceptor
public class LocaleContextualizor {

	private final static Logger logger = LoggerFactory.getLogger(LocaleContextualizor.class);

	@AroundInvoke
	public Object contexualized(InvocationContext invocationContext) throws Exception {
		Locale locale = (Locale) ThreadLocalContextHolder.get(Constants.LOCALE);
		if(null!=locale) {
			logger.debug("SPI CONTEXTUALIZE LOCALE {}", locale);
			return invocationContext.getMethod().invoke(locale, invocationContext.getParameters());
		}
		return invocationContext.proceed();
	}
}