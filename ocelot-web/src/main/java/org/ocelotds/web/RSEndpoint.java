/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.web;

import java.util.Enumeration;
import java.util.Locale;
import javax.ws.rs.core.Context;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.configuration.LocaleExtractor;
import org.ocelotds.context.ThreadLocalContextHolder;
import org.ocelotds.core.mtc.RSMessageToClientService;
import org.ocelotds.exceptions.LocaleNotFoundException;
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
public class RSEndpoint {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Context
	private HttpServletRequest request;

	@Inject
	private RSMessageToClientService messageToClientService;

	@Inject
	private RequestManager requestManager;

	@Inject
	private LocaleExtractor localeExtractor;

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
