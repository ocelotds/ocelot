/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.integration.objects;

import javax.validation.constraints.NotNull;

/**
 *
 * @author hhfrancois
 */
public class WithConstraint {
	@NotNull
	String name = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
