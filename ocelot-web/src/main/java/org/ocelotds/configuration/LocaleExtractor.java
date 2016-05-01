/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ocelotds.exceptions.LocaleNotFoundException;

/**
 *
 * @author hhfrancois
 */
public class LocaleExtractor {

	public Locale extractFromAccept(String accept) throws LocaleNotFoundException {
		if(null != accept) {
			Pattern pattern = Pattern.compile(".*(\\w\\w)-(\\w\\w).*");
			Matcher matcher = pattern.matcher(accept);
			if (matcher.matches()) {
				return new Locale(matcher.group(1), matcher.group(2));
			}
		}
		throw new LocaleNotFoundException();
	}
	
}
