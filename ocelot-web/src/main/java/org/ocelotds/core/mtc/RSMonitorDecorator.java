/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.mtc;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.messaging.MessageToClient;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@Decorator
@Priority(0)
public abstract class RSMonitorDecorator implements RSMessageToClientService {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	@Delegate
	@Any
	RSMessageToClientService messageToClientService;

	/**
	 * determine if request is monitored
	 * @param session
	 * @return 
	 */
	public boolean isMonitored(HttpSession session) {
		boolean monitor  =false;
		if (null != session) {
			monitor = (boolean) session.getAttribute(Constants.Options.MONITOR);
		}
		return monitor;
	}

	/**
	 * get t0
	 * @param monitor
	 * @return 
	 */
	protected long getT0(boolean monitor) {
		long t0 = 0;
		if (monitor) {
			t0 = System.currentTimeMillis();
		}
		return t0;
	}

	/**
	 * set timing
	 * @param monitor
	 * @param t0
	 * @param mtc 
	 */
	protected void setTiming(boolean monitor, long t0, MessageToClient mtc) {
		if (monitor) {
			long t1 = System.currentTimeMillis();
			mtc.setTime(t1 - t0);
		}
	}

	/**
	 * Decorate method
	 * @param message
	 * @param session
	 * @return 
	 */
	@Override
	public MessageToClient createMessageToClient(MessageFromClient message, HttpSession session) {
		boolean monitor  = isMonitored(session);
		logger.debug("Monitor is enabled : {}", monitor);
		long t0 = getT0(monitor);
		MessageToClient mtc = messageToClientService.createMessageToClient(message, session);
		setTiming(monitor, t0, mtc);
		return mtc;
	}
}
