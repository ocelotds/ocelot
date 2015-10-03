/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

import java.security.Principal;
import javax.security.auth.Subject;

/**
 *
 * @author hhfrancois
 */
public interface SecurityContext {
	Principal getPrincipal();
	Subject getSubject();
}
