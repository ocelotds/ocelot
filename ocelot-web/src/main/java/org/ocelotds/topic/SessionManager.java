/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.topic;

import java.util.Collection;
import java.util.Map;
import javax.websocket.Session;

/**
 * Link httpSession with wsSession
 * @author hhfrancois
 */
public interface SessionManager {
	/**
	 * return map link httpid and wssession
	 * @return 
	 */
	Map<String, Session> getMap();
	
	/**
	 * link wssession and http session
	 * @param httpid
	 * @param session 
	 */
	void addSession(String httpid, Session session);
	
	/**
	 * If for httpid there is already a old wssession, close it before replace
	 * @param httpid 
	 * @return  
	 */
	Session closeOldSessionForHttp(String httpid);
	
	/**
	 * Remove link wssession and httpid
	 * @param session 
	 * @return  
	 */
	Collection<String> removeSession(Session session);

	/**
	 * get wssession for a httpid
	 * @param httpid
	 * @return 
	 */
	Session getSessionById(String httpid);
	
	/**
	 * Get username from wssession
	 * @param session
	 * @return 
	 */
	String getUsername(Session session);
}
