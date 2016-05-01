/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.objects;

import javax.servlet.http.HttpServletRequest;
import org.ocelotds.web.AbstractFileServlet;

/**
 *
 * @author hhfrancois
 */
public class AbstractServletImpl extends AbstractFileServlet {

	private String filepath;

	public void setFilename(String filepath) {
		this.filepath = filepath;
	}

	@Override
	protected String getFilename(HttpServletRequest request) {
		return filepath;
	}

	@Override
	protected String getMimetype(HttpServletRequest request) {
		return "text/plain";
	}
}
