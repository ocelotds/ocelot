/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.web;

import java.io.BufferedReader;
import org.ocelotds.Constants;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ocelotds.annotations.OcelotLogger;
import org.slf4j.Logger;

/**
 * Servlet to serve ocelot.js
 *
 * @author hhfrancois
 */
public abstract class AbstractFileServlet extends HttpServlet {

	private static final long serialVersionUID = 1973549844535787671L;

	protected abstract String getFilename(HttpServletRequest request);

	protected abstract String getMimetype(HttpServletRequest request);

	@Inject
	@OcelotLogger
	private transient Logger logger;

	/**
	 *
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean first = true;
		response.setContentType(getMimetype(request));
		response.setCharacterEncoding(Constants.UTF_8);
		try (Writer writer = response.getWriter(); 
				  BufferedReader in = Files.newBufferedReader(FileSystems.getDefault().getPath(getFilename(request)), StandardCharsets.UTF_8)) {
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				if (!first) {
					writer.write(Constants.BACKSLASH_N);
				}
				first = false;
				writer.write(inputLine);
			}
		}
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
}
