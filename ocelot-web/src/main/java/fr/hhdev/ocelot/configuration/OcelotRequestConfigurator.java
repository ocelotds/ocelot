/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.ws.rs.core.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extract info in request, and expose them to userProperties ServerEnpoint
 * 
 * @author hhfrancois
 */
public class OcelotRequestConfigurator extends ServerEndpointConfig.Configurator {

	private final static Logger logger = LoggerFactory.getLogger(OcelotRequestConfigurator.class);

	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		Map<String, List<String>> headers = request.getHeaders();
		List<String> accept = headers.getOrDefault(HttpHeaders.ACCEPT_LANGUAGE, Arrays.asList(new String[] {"en-US;q=1"}));
		logger.debug("Get accept-language from client headers : {}", accept);
		sec.getUserProperties().put(HttpHeaders.ACCEPT_LANGUAGE, accept); // accept-language : [fr, fr-FR;q=0.8, en-US;q=0.5, en;q=0.3]
		super.modifyHandshake(sec, request, response);
	}
}
