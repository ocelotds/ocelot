/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

/**
 *
 * @author hhfrancois
 */
public interface SecureProvider {

	/**
	 *
	 * @param ic
	 * @param roles
	 * @throws IllegalAccessException
	 */
	void checkAccess(InvocationContext ic, String[] roles) throws IllegalAccessException;
	
}
