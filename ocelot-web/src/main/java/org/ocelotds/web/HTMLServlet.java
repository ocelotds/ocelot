/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.web;

import org.ocelotds.Constants;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to serve ocelot.js
 *
 * @author hhfrancois
 */
@WebServlet(urlPatterns = {Constants.SLASH_OCELOT_HTML})
public class HTMLServlet extends AbstractServlet {

	private static final long serialVersionUID = 1973549844535787671L;

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String filename = request.getServletContext().getInitParameter(Constants.OCELOT_HTML);
		processFile(filename, Constants.HTMLTYPE, request, response);
	}
}
