/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web.rest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotResource;

/**
 *
 * @author hhfrancois
 * The core include ocelotServices.js
 */
@Path("{a:core|core.min|core.ng|core.ng.min}.js")
@RequestScoped
@OcelotResource
public class RsJsCore extends AbstractRsJs {
	@Context
	private UriInfo context;
	
	@Override
	List<InputStream> getStreams() {
		List<InputStream> streams = new ArrayList<>();
		addStream(streams, getJsCore());
		return streams;
	}
	
	String getJsCore() {
		return Constants.SLASH+context.getPath();
	}
}

