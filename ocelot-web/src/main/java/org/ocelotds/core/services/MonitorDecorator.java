/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.services;

import java.util.Map;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.websocket.Session;
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
public abstract class MonitorDecorator implements MessageToClientService {

	@Inject
	@Delegate
	@Any
	MessageToClientService messageToClientService;

	@Inject
	@OcelotLogger
	private Logger logger;

	@Override
	public MessageToClient createMessageToClient(MessageFromClient message, Session client) {
		MessageToClient mtc = null;
		if (null != client) {
			Map<String, Object> sessionProperties = client.getUserProperties();
			boolean monitor = (Boolean) sessionProperties.get(Constants.Options.MONITOR);
			logger.debug("Monitor is enabled : {}", monitor);
			long t0 = 0;
			if (monitor) {
				t0 = System.currentTimeMillis();
			}
			mtc = messageToClientService.createMessageToClient(message, client);
			if (monitor) {
				long t1 = System.currentTimeMillis();
				mtc.setTime(t1 - t0);
			}
		}
		return mtc;
	}
}
