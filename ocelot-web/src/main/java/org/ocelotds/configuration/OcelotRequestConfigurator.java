/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.ws.rs.core.HttpHeaders;
import org.ocelotds.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extract info in request, and expose them to userProperties ServerEnpoint
 *
 * @author hhfrancois
 */
public class OcelotRequestConfigurator extends ServerEndpointConfig.Configurator {

	private final Logger logger = LoggerFactory.getLogger(OcelotRequestConfigurator.class);

	/**
	 * Set user information from open websocket
	 *
	 * @param sec
	 * @param request
	 * @param response
	 */
	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		sec.getUserProperties().put(Constants.SESSION_BEANS, getSessionBeansMap(request.getHttpSession()));
		sec.getUserProperties().put(Constants.HANDSHAKEREQUEST, request);
		sec.getUserProperties().put(Constants.LOCALE, getLocale(request));
		sec.getUserProperties().put(Constants.Options.MONITOR, isMonitored(request));
		super.modifyHandshake(sec, request, response);
	}
	
	/**
	 * Return locale of client
	 * @param request
	 * @return 
	 */
	Locale getLocale(HandshakeRequest request) {
		Map<String, List<String>> headers = request.getHeaders();
		Locale locale = new Locale("en", "US");
		List<String> accepts = headers.get(HttpHeaders.ACCEPT_LANGUAGE);
		logger.debug("Get accept-language from client headers : {}", accepts);
		if (null != accepts) {
			for (String accept : accepts) {
				Pattern pattern = Pattern.compile(".*(\\w\\w)-(\\w\\w).*");
				Matcher matcher = pattern.matcher(accept);
				if (matcher.matches() && matcher.groupCount() == 2) {
					locale = new Locale(matcher.group(1), matcher.group(2));
					break;
				}
			}
		}
		return locale;
	}

	/**
	 * Check if option monitor is enabled
	 * @param request
	 * @return 
	 */
	boolean isMonitored(HandshakeRequest request) {
		boolean monitor = false;
		if (request != null) {
			List<String> options = request.getParameterMap().get("option");
			if (options != null && !options.isEmpty()) {
				monitor = options.contains(Constants.Options.MONITOR);
			}
		}
		return monitor;
	}

	/**
	 * Return the map for storing session beans. this map is get and store in httpSession
	 *
	 * @param session
	 * @return
	 */
	Object getSessionBeansMap(Object session) {
		Map attribute;
		if (session != null && HttpSession.class.isInstance(session)) {
			HttpSession httpSession = (HttpSession) session;
			attribute = (Map) httpSession.getAttribute(Constants.SESSION_BEANS);
			if (attribute == null) {
				attribute = new HashMap();
				httpSession.setAttribute(Constants.SESSION_BEANS, attribute);
			}
		} else {
			attribute = new HashMap();
		}
		logger.debug("Get from session the beans declared with sessionscoped : {} entries", attribute.size());
		return attribute;
	}
}
