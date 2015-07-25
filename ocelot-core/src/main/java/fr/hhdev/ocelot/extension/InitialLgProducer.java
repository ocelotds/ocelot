/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.extension;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 *
 * @author hhfrancois
 */
public class InitialLgProducer {

	@Produces
	@InitialLanguage 
	public String lg(InjectionPoint ip) {
		return "fr";
	}
}
