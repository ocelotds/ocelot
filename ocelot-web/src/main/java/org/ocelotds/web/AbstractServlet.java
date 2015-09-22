/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.web;

import org.ocelotds.Constants;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to serve ocelot.js
 *
 * @author hhfrancois
 */
public abstract class AbstractServlet extends HttpServlet {

	private static final long serialVersionUID = 1973549844535787671L;

	protected abstract void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

	/**
	 * 
	 * @param filename
	 * @param mimetype
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException 
	 */
	protected void processFile(String filename, String mimetype, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType(mimetype);
		File source = new File(filename);
		OutputStream out = response.getOutputStream();
		int count = 0;
		try (InputStream in = new FileInputStream(source)) {
			byte[] buffer = new byte[Constants.DEFAULT_BUFFER_SIZE];
			int n = 0;
			while (-1 != (n = in.read(buffer))) {
				out.write(buffer, 0, n);
				count += n;
			}
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
		return "ocelot.js";
	}// </editor-fold>

}
