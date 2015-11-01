/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.picketlink;

import org.ocelotds.spi.security.ContainerSecurityServices;
import javax.inject.Inject;
import org.ocelotds.annotations.ContainerQualifier;
import org.ocelotds.spi.security.SecurityContext;
import org.picketlink.Identity;
import org.picketlink.idm.model.Account;

/**
 *
 * @author hhfrancois
 */
@ContainerQualifier("PICKETLINK")
public class PicketlinkSecurityServices implements ContainerSecurityServices {

	@Inject
	private Identity identity;
	
	@Override
	public SecurityContext getSecurityContext() {
		SecurityContext securityContext = null;
		if(identity.isLoggedIn()) {
			securityContext = new PicketlinkSecurityContext(identity.getAccount());
		}
		return securityContext;
	}

	@Override
	public void setSecurityContext(SecurityContext securityContext) {
		if(securityContext!=null) {
			PicketlinkSecurityContext picketlinkSecurityContext = (PicketlinkSecurityContext) securityContext;
			picketlinkSecurityContext.getAccount();
		}
	}
	
	/**
	 * private glassfish implementation
	 */
	static class PicketlinkSecurityContext implements org.ocelotds.spi.security.SecurityContext {

		private final Account account;

		public PicketlinkSecurityContext(Account account) {
			this.account = account;
		}
		

		public Account getAccount() {
			return account;
		}

		@Override
		public String toString() {
			return "{\"account\":\""+account.getId()+"\"}";
		}
	}
}
