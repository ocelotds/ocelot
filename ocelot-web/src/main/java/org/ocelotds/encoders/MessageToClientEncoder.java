/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.encoders;

import org.ocelotds.messaging.MessageToClient;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * Decoder for class MessageClient for webSocket endpoint
 *
 * @author hhfrancois
 */
public class MessageToClientEncoder implements Encoder.Text<MessageToClient> {

	@Override
	public String encode(MessageToClient object) throws EncodeException {
		return object.toJson();
	}

	@Override
	public void init(EndpointConfig config) {
	}

	@Override
	public void destroy() {
	}

}
