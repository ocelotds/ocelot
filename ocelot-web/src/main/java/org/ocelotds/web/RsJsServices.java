/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.Path;
import org.ocelotds.OcelotServices;
import org.ocelotds.annotations.DataService;

/**
 *
 * @author hhfrancois
 */
@Path("services.js")
@RequestScoped
public class RsJsServices extends AbstractRsJs {

	@Any
	@Inject
	@DataService(resolver = "")
	private Instance<Object> dataservices;

	@Override
	List<InputStream> getStreams() {
		List<InputStream> streams = new ArrayList<>();
		for (Object dataservice : dataservices) {
			if (!OcelotServices.class.isInstance(dataservice)) {
				addStream(streams, getJsFilename(getClassnameFromProxy(dataservice)));
			}
		}
		return streams;
	}

	/**
	 * Return classname from proxy instance
	 *
	 * @param dataservice
	 * @return
	 */
	String getClassnameFromProxy(Object dataservice) {
		String ds = dataservice.toString();
		return getClassnameFromProxyname(ds);
	}

	/**
	 * Return classname from proxyname
	 *
	 * @param dataservice
	 * @return
	 */
	String getClassnameFromProxyname(String proxyname) {
		Pattern pattern = Pattern.compile("[@$]");
		Matcher matcher = pattern.matcher(proxyname);
		matcher.find();
		matcher.start();
		return proxyname.substring(0, matcher.start());
	}
}
