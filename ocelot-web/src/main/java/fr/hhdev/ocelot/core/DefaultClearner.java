/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.core;

/**
 *
 * @author hhfrancois
 */
public class DefaultClearner implements Cleaner {

	/**
	 * Method allow cleaning all extra fields on arguments from framework web For sample angularjs add some variables begin $$ So replace : ,"$$hashKey":"object:\d" 
	 *
	 * @param arg
	 * @return
	 */
	@Override
	public String cleanArg(String arg) {
		String angularvar = "(,\"\\$\\$\\w+\":\".*\")";
		return arg.replaceAll(angularvar, "");
	}
	
}
