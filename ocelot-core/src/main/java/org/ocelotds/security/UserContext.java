/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

import java.security.Principal;

/**
 *
 * @author hhfrancois
 */
public interface UserContext {

	public Principal getPrincipal();

	public boolean isUserInRole(String role);

}
