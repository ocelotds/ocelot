/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.processors.stringDecorators;

/**
 *
 * @author hhfrancois
 */
public class NothingDecorator implements StringDecorator {

	@Override
	public String decorate(String str) {
		return str;
	}
	
}
