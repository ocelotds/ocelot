/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot;

import fr.hhdev.ocelot.annotations.DataService;
import fr.hhdev.ocelot.annotations.JsCacheRemove;
import fr.hhdev.ocelot.annotations.JsCacheResult;
import fr.hhdev.ocelot.annotations.TransientDataService;
import fr.hhdev.ocelot.core.MethodWithSessionInjection;
import fr.hhdev.ocelot.core.UpdatedCacheManager;
import fr.hhdev.ocelot.i18n.Locale;
import fr.hhdev.ocelot.i18n.ThreadLocalContextHolder;
import java.util.Collection;
import java.util.Map;
import javax.inject.Inject;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.CDI)
public class OcelotServices {

	private static final Logger logger = LoggerFactory.getLogger(OcelotServices.class);

	@Inject
	private UpdatedCacheManager updatedCacheManager;

	@MethodWithSessionInjection
	@JsCacheRemove(cls = OcelotServices.class, methodName = "getLocale")
	public void setLocale(Locale locale) {
	}

	@TransientDataService
	public void setLocale(Locale l, Session session) {
		System.out.println("METHODE DETOURNE PAR LA  DEMANDE D'INJECTION");
		java.util.Locale locale = new java.util.Locale(l.getLanguage(), l.getCountry());
		logger.debug("Receive setLocale({}) call from client.", locale);
		session.getUserProperties().put(Constants.LOCALE, locale);
		ThreadLocalContextHolder.put(Constants.LOCALE, locale);
	}

	@MethodWithSessionInjection
	@JsCacheResult(year = 1)
	public Locale getLocale() {
		return null;
	}

	@TransientDataService
	public Locale getLocale(Session session) {
		System.out.println("METHODE DETOURNE PAR LA  DEMANDE D'INJECTION");
		logger.debug("Receive getLocale call from client.");
		java.util.Locale locale = (java.util.Locale) session.getUserProperties().get(Constants.LOCALE);
		Locale l = new Locale();
		l.setLanguage(locale.getLanguage());
		l.setCountry(locale.getCountry());
		logger.debug("getLocale() = {}", l);
		return l;
	}
	
	public Collection<String> getOutDatedCache(Map<String, Long> states) {
		return updatedCacheManager.getOutDatedCache(states);
	}
	
	public void subscribe(String topic) {
		
	}

	public void unsubscribe(String topic) {
		
	}
}
