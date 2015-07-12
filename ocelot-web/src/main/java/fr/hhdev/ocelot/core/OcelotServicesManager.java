/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.i18n.ThreadLocalContextHolder;
import fr.hhdev.ocelot.messaging.MessageFromClient;
import fr.hhdev.ocelot.messaging.MessageToClient;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class managing the fake OcelotServices Exposed on client
 *
 * @author hhfrancois
 */
public class OcelotServicesManager {

	private static final Logger logger = LoggerFactory.getLogger(OcelotServicesManager.class);

	@Inject
	private UpdatedCacheManager updatedCacheManager;
	/**
	 * The EndPoint receive a call for ocelotServices
	 *
	 * @param client
	 * @param message
	 */
	public void processOcelotServices(Session client, MessageFromClient message) {
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(message.getId());
		if (null != message.getOperation()) switch (message.getOperation()) {
			case "setLocale":
				try {
					ObjectMapper mapper = new ObjectMapper();
					fr.hhdev.ocelot.i18n.Locale l = mapper.readValue(message.getParameters().get(0), fr.hhdev.ocelot.i18n.Locale.class);
					Locale locale = new Locale(l.getLanguage(), l.getCountry());
					logger.debug("Receive setLocale({}) call from client.", locale);
					client.getUserProperties().put(Constants.LOCALE, locale);
					ThreadLocalContextHolder.put(Constants.LOCALE, locale);
				} catch (IOException ex) {
					logger.error("Fail read argument(0) : " + message.getParameters().get(0), ex);
				}	
				break;
			case "getLocale":
				logger.debug("Receive getLocale call from client.");
				Locale locale = (Locale) client.getUserProperties().get(Constants.LOCALE);
				fr.hhdev.ocelot.i18n.Locale l = new fr.hhdev.ocelot.i18n.Locale();
				l.setLanguage(locale.getLanguage());
				l.setCountry(locale.getCountry());
				Calendar deadline = Calendar.getInstance();
				deadline.add(Calendar.YEAR, 1);
				messageToClient.setDeadline(deadline.getTime().getTime());
				messageToClient.setResult(l);
				logger.debug("getLocale() = {}", l);
				break;
			case "sendLastUpdateCacheStates":
				logger.debug("Receive sendLastUpdateCacheStates call from client.");
				try {
					ObjectMapper mapper = new ObjectMapper();
					Map<String, Long> states = mapper.readValue(message.getParameters().get(0), new TypeReference<HashMap<String,Long>>() {});
					for (String id : states.keySet()) {
						logger.debug("The client has this cache {} since {}.", id, states.get(id));
					}
					messageToClient.setResult(updatedCacheManager.getOutDatedCache(states));
				} catch (IOException ex) {
					logger.error("Fail read argument(0) : " + message.getParameters().get(0), ex);
				}	
				break;
		}
		logger.debug("Send response from inner ocelotServices.");
		client.getAsyncRemote().sendObject(messageToClient);
	}

}
