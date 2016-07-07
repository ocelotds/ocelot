/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.dashboard.decorators;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.dashboard.services.MonitorSessionManager;
import org.ocelotds.messaging.MessageEvent;
import org.ocelotds.messaging.MessageToClient;
import org.slf4j.Logger;
import org.ocelotds.web.rest.IRSEndpoint;

/**
 *
 * @author hhfrancois
 */
@Decorator
@Priority(1)
public abstract class IRSEndpointMonitor implements IRSEndpoint {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	@Delegate
	@Any
	IRSEndpoint iRSEndpoint;

	@Inject
	MonitorSessionManager monitorSessionManager;
	
	@Inject
	@MessageEvent
	Event<MessageToClient> wsEvent;

	/**
	 * Decorate method
	 *
	 * @param mfc
	 * @return
	 */
	@Override
	public String getMessageToClient(String mfc) {
		HttpSession httpSession = iRSEndpoint.getHttpSession();
		String httpid = httpSession.getId();
		boolean monitored = monitorSessionManager.isMonitored(httpid);
		long t0 = 0;
		if(monitored) {
			t0 = System.currentTimeMillis();
		}
		String mtc = iRSEndpoint.getMessageToClient(mfc);
		if(monitored) {
			long t1 = System.currentTimeMillis();
			publish("request-event-"+httpid, mfc, mtc, t1 - t0);
		}
		return mtc;
	}

	public void publish(String topic, String mfc, String mtc, long delay) {
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(topic);
		messageToClient.setJson("{\"t\":"+delay+",\"mfc\":"+mfc+", \"mtc\":"+mtc+"}" ); // You can send only serializable objects
		wsEvent.fire(messageToClient);
	}
}
