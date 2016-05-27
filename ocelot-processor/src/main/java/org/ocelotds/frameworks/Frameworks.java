/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.frameworks;

/**
 *
 * @author hhfrancois
 */
public enum Frameworks {
	ANGULARJS(new AngularFwk()), NOFWK(new NoFwk());
	FwkWriter instance;

	Frameworks(FwkWriter instance) {
		this.instance = instance;
	}

	public FwkWriter fwkWriter() {
		return instance;
	}
	
	

}
