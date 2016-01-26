/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
