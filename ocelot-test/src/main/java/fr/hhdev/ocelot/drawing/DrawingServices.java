/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.drawing;

import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.annotations.DataService;
import fr.hhdev.ocelot.messaging.MessageEvent;
import fr.hhdev.ocelot.messaging.MessageToClient;
import fr.hhdev.ocelot.messaging.MessageType;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.CDI)
public class DrawingServices {
	@Inject
	@MessageEvent
	Event<MessageToClient> wsEvent;

	public void pushCanvasEvent(int x, int y, String type) {
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId("eventCanvas");
		messageToClient.setResponse(new CanvasEvent(x, y, type));
		wsEvent.fire(messageToClient);
	}
}
