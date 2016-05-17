/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web.rest;

import javax.ws.rs.core.Context;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.annotations.OcelotResource;
import org.ocelotds.context.ThreadLocalContextHolder;
import org.ocelotds.core.mtc.RSMessageToClientService;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.messaging.MessageToClient;
import org.slf4j.Logger;

/**
 * REST Web Service
 *
 * @author hhfrancois
 */
@Path("endpoint")
@RequestScoped
@OcelotResource
public class RSEndpoint {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Context
	private HttpServletRequest request;

	@Inject
	private RSMessageToClientService messageToClientService;

	/**
	 * Retrieves representation of an instance of org.ocelotds.GenericResource
	 *
	 * @param json
	 * @return an instance of java.lang.String
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String getMessageToClient(@FormParam(Constants.Message.MFC) String json) {
		HttpSession httpSession = request.getSession();
		setContext(httpSession);
		MessageFromClient message = MessageFromClient.createFromJson(json);
		MessageToClient createMessageToClient = messageToClientService.createMessageToClient(message, httpSession);
		return createMessageToClient.toJson();
	}

	void setContext(HttpSession httpSession) {
		ThreadLocalContextHolder.put(Constants.HTTPREQUEST, request);
		ThreadLocalContextHolder.put(Constants.HTTPSESSION, httpSession);
	}
}
