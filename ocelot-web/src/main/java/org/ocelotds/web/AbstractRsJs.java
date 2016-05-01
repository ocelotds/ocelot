/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotLogger;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
public abstract class AbstractRsJs {

	@Inject
	@OcelotLogger
	Logger logger;

	/**
	 * Get stream list for this resource
	 * @return 
	 */
	abstract List<InputStream> getStreams();

	/**
	 * Get resource
	 * @return
	 * @throws IOException 
	 */
	@GET
	@Produces(Constants.JSTYPE)
	public Response getJs() throws IOException {
		return Response.ok((Object) getSequenceInputStream(getStreams())).build();
	}

	/**
	 * GEt URL Resource
	 * @param name
	 * @return 
	 */
	URL getResource(String name) {
		return AbstractRsJs.class.getResource(name);
	}

	/**
	 * p1.p2.p3.Cls1 -&gt; /p1/p2/p3/Cls1.js
	 * @param classname
	 * @return 
	 */
	String getJsFilename(String classname) {
		String path = classname.replaceAll("\\.", File.separator);
		return File.separator + path + Constants.JS;
	}

	void addStream(List<InputStream> streams, String filename) {
		try {
			URL resource = getResource(filename);
			streams.add(resource.openStream());
		} catch (Throwable ex) {
			logger.error("Fail to stream : " + filename, ex);
		}
	}
	
	protected SequenceInputStream getSequenceInputStream(List<InputStream> streams) {
		return new SequenceInputStream(Collections.enumeration(streams));
	}
}
