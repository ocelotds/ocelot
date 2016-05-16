/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author hhfrancois
 */
@RequestScoped
@Path("app")
public class RsApp {

	@Context
	private UriInfo context;

	@GET
	@Path("{name}.{type}")
	public Response getRoot(@PathParam("type") String type) throws IOException {
		return getResponse(type);
	}
	@GET
	@Path("css/{name}.{type}")
	public Response getCss(@PathParam("type") String type) throws IOException {
		return getResponse(type);
	}
	@GET
	@Path("js/{name}.{type}")
	public Response getJs(@PathParam("type") String type) throws IOException {
		return getResponse(type);
	}
	@GET
	@Path("fonts/{name}.{type}")
	public Response getFonts(@PathParam("type") String type) throws IOException {
		return getResponse(type);
	}

	public Response getResponse(String type) throws IOException {
		MediaType mtype = new MediaType("text", "js".equals(type) ? "javascript" : type);
		return Response.ok((Object) getResource("/" + context.getPath()).openStream(), mtype).build();
	}

	/**
	 * GEt URL Resource
	 *
	 * @param name
	 * @return
	 */
	URL getResource(String name) {
		return RsApp.class.getResource(name);
	}
}
