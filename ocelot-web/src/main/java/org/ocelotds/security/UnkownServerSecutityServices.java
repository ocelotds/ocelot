/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

import org.ocelotds.spi.security.ContainerSecurityServices;
import org.ocelotds.spi.security.SecurityContext;

/**
 *
 * @author hhfrancois
 */
public class UnkownServerSecutityServices implements ContainerSecurityServices {

	@Override
	public SecurityContext getSecurityContext() {
		return null;
	}

	@Override
	public void setSecurityContext(SecurityContext securityContext) {
	}
	
}
