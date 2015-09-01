/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.encoders;

import java.util.UUID;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.ocelotds.messaging.MessageToClient;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hhfrancois
 */
public class MessageToClientEncoderTest {

	private final MessageToClientEncoder messageToClientEncoder = new MessageToClientEncoder();

	/**
	 * Test of encode method, of class MessageToClientEncoder.
	 * @throws javax.websocket.EncodeException
	 */
	@Test
	public void testEncode() throws EncodeException {
		System.out.println("encode");
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setDeadline(1000);
		messageToClient.setId(UUID.randomUUID().toString());
		messageToClient.setResult("RESULT");
		String result = messageToClientEncoder.encode(messageToClient);
		assertThat(result).isEqualTo(messageToClient.toJson());
	}

	/**
	 * Test of init method, of class MessageToClientEncoder.
	 */
	@Test
	public void testInit() {
		System.out.println("init");
		EndpointConfig config = mock(EndpointConfig.class);
		messageToClientEncoder.init(config);
	}

	/**
	 * Test of destroy method, of class MessageToClientEncoder.
	 */
	@Test
	public void testDestroy() {
		System.out.println("destroy");
		messageToClientEncoder.destroy();
	}
	
}
