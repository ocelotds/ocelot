/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web.rest;

import javax.servlet.http.HttpSession;
import javax.ws.rs.FormParam;
import org.ocelotds.Constants;

/**
 *
 * @author hhfrancois
 */
public interface IEndpoint {
	public String getMessageToClient(String json);
	
	public HttpSession getHttpSession();
	
}
