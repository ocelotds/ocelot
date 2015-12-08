/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotLogger;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@ApplicationScoped
public class OcelotConfiguration {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Any
	@Inject
	@org.ocelotds.configuration.annotations.OcelotConfiguration(OcelotConfigurationName.STACKTRACELENGTH)
	private Instance<String> ocelotConfigurationsStack;

	/**
	 * Default size of stacktrace include in messageToClient fault
	 */
	private static final String DEFAULTSTACKTRACE = "50";

	/**
	 * Read in web.xml the optional STACKTRACE_LENGTH config and set it in OcelotConfiguration
	 * @param sc 
	 */
	public void readStacktraceConfig(@Observes @Initialized(ApplicationScoped.class) ServletContext sc) {
		String stacktrace;
		if(ocelotConfigurationsStack.isUnsatisfied()) {
			stacktrace = sc.getInitParameter(Constants.Options.STACKTRACE_LENGTH);
			if(stacktrace==null) {
				stacktrace = DEFAULTSTACKTRACE;
			} else {
				logger.debug("Read '{}' option in web.xml : '{}'.", Constants.Options.STACKTRACE_LENGTH, stacktrace);
			}
		} else {
			stacktrace = ocelotConfigurationsStack.get();
			logger.debug("Read '{}' option from producer : '{}'.", Constants.Options.STACKTRACE_LENGTH, stacktrace);
		}
		int stacktracelenght = Integer.parseInt(stacktrace);
		logger.debug("'{}' value : '{}'.", Constants.Options.STACKTRACE_LENGTH, stacktracelenght);
		setStacktracelength(stacktracelenght);
	}

	private int stacktracelength = 50;

	public int getStacktracelength() {
		return stacktracelength;
	}

	public void setStacktracelength(int stacktracelength) {
		this.stacktracelength = stacktracelength;
	}

}
