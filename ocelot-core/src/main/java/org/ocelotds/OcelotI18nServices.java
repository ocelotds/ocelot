/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import org.ocelotds.context.ThreadLocalContextHolder;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.inject.Inject;
import org.ocelotds.logger.OcelotLogger;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
public class OcelotI18nServices {
	@Inject
	@OcelotLogger
	private Logger logger;
	
	public Locale getUserLocale() {
		logger.debug("Get locale from OcelotServices");
		Locale locale = (Locale) ThreadLocalContextHolder.get(Constants.LOCALE);
		if(null==locale) {
			logger.debug("Get locale from OcelotServices : default");
			locale =  new Locale("en", "US");
		}
		logger.debug("Get locale from OcelotServices : {}", locale);
		return locale;
		
	}
	
	public String getLocalizedMessage(String bundleName, String entry, Object[] args) {
		ResourceBundle bundle = ResourceBundle.getBundle(bundleName, getUserLocale());
		return MessageFormat.format(bundle.getString(entry), args);
	}
}
