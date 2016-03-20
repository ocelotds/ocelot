/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.web;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.ocelotds.Constants;
import org.ocelotds.OcelotServices;

/**
 *
 * @author hhfrancois
 */
@Path("cor{min}.js")
@RequestScoped
public class RsJsCore extends AbstractRsJs {
	@Context
	private UriInfo context;
	
	@Override
	List<InputStream> getStreams() {
		List<InputStream> streams = new ArrayList<>();
		addStream(streams, getJsFilename(OcelotServices.class.getName()));
		addStream(streams, getJsCore());
		return streams;
	}
	
	String getJsCore() {
		if(context.getPath().contains(".min")) {
			return Constants.SLASH + Constants.OCELOT_CORE_MIN + Constants.JS;
		}
		return Constants.SLASH + Constants.OCELOT_CORE + Constants.JS;
	}
}

