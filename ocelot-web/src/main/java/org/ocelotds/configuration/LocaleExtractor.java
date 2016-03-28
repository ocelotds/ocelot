/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
