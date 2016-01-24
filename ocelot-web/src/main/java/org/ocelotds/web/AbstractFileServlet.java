/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.web;

import java.io.BufferedReader;
import org.ocelotds.Constants;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to serve ocelot.js
 *
 * @author hhfrancois
 */
public abstract class AbstractFileServlet extends HttpServlet {

	private static final long serialVersionUID = 1973549844535787671L;

	protected abstract String getFilename(HttpServletRequest request);
	protected abstract String getMimetype(HttpServletRequest request);

	/**
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException 
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int count = 0;
		response.setContentType(getMimetype(request));
		String protocol = getProtocol(request);
		try (Writer writer = response.getWriter()) {
			try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(getFilename(request)), Constants.UTF_8))) {
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					if(count!=0) {
						writer.write(Constants.BACKSLASH_N);
						count++;
					}
					String result = inputLine.replaceAll(Constants.PROTOCOL, protocol);
					writer.write(result);
					count += result.length();
				}
			}
			writer.flush();
		}
		response.setContentLength(count);
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			  throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			  throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "ocelot-servlet";
	}

	String getProtocol(HttpServletRequest request) {
		String protocol = Constants.WS;
		if(request.isSecure()) {
			protocol = Constants.WSS;
		}
		return protocol;
	}
}
