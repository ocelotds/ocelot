/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web.rest;

import java.io.IOException;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.ocelotds.annotations.OcelotResource;

/**
 *
 * @author hhfrancois
 * The core include ocelotServices.js
 */
@Path("core")
@RequestScoped
@OcelotResource
public class RsJsCoreUrl {
	/**
	 * Get resource
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getJs() {
		return "// ok";
	}
}

