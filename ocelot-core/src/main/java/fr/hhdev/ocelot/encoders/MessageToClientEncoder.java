/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.ocelot.encoders;

import fr.hhdev.ocelot.messaging.MessageToClient;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
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
