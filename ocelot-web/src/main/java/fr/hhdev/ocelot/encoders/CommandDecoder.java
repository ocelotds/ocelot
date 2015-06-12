/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.encoders;

import fr.hhdev.ocelot.messaging.Command;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

/**
 * Decoder for class Command for webSocket endpoint
 * @author hhfrancois
 */
public class CommandDecoder implements Decoder.Text<Command>{

	@Override
	public Command decode(String commandMessage) throws DecodeException {
		return Command.createFromJson(commandMessage);
	}

	@Override
	public boolean willDecode(String commandMessage) {
		return true;
	}

	@Override
	public void init(EndpointConfig config) {
	}

	@Override
	public void destroy() {
	}
	
}
