/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot;

import javax.inject.Singleton;

/**
 *
 * @author hhfrancois
 */
@Singleton
public class OcelotConfiguration {
	private int stacktracedeep = 50;

	public int getStacktracedeep() {
		return stacktracedeep;
	}

	public void setStacktracedeep(int stacktracedeep) {
		this.stacktracedeep = stacktracedeep;
	}
	
}
