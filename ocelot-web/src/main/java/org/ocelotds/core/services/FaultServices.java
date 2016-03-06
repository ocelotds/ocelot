/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.services;

import javax.inject.Inject;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.configuration.OcelotConfiguration;
import org.ocelotds.messaging.Fault;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
public class FaultServices {
	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	private OcelotConfiguration configuration;

	/**
	 * Build an fault Object from exception with stacktrace length from configuration
	 * @param ex
	 * @return 
	 */
	public Fault buildFault(Throwable ex) {
		Fault fault;
		int stacktracelength = configuration.getStacktracelength();
		if (stacktracelength == 0 || logger.isDebugEnabled()) {
			logger.error("Invocation failed", ex);
		}
		fault = new Fault(ex, stacktracelength);
		return fault;
	}
	
}
