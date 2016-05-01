/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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

