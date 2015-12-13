/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

import java.security.Principal;
import javax.enterprise.inject.Alternative;
import org.ocelotds.Constants;
import org.ocelotds.context.ThreadLocalContextHolder;

/**
 *
 * @author hhfrancois
 */
@Alternative
public class OcelotPrincipal implements Principal {

	@Override
	public String getName() {
		String name = Constants.ANONYMOUS;
		Principal p = (Principal) ThreadLocalContextHolder.get(Constants.PRINCIPAL);
		if (null != p) {
			name = p.getName();
		}
		return name;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()+"("+getName()+")";
	}
}
