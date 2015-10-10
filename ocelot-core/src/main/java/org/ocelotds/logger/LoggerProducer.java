/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.logger;

import org.ocelotds.annotations.OcelotLogger;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
public class LoggerProducer {
	
	
	/**
	 *
	 * @param injectionPoint : argument injected
	 * @return
	 */
	@Produces
	@OcelotLogger
	public Logger getLogger(InjectionPoint injectionPoint) {
		Annotated annotated = injectionPoint.getAnnotated();
		OcelotLogger annotation = annotated.getAnnotation(OcelotLogger.class);
		String loggerName = annotation.name();
		if(loggerName.isEmpty()) {
			loggerName = injectionPoint.getMember().getDeclaringClass().getName();
		}
		return LoggerFactory.getLogger(loggerName);
	}
}
