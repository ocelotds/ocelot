/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.mockito.ArgumentCaptor;
import org.ocelotds.Constants;

/**
 *
 * @author hhfrancois
 */
public class JSServletTest {

	private final JSServlet jsServlet = new JSServlet();
	
	HttpServletRequest request;
	HttpServletResponse response;
	
	public StringWriter setUp(String minify, final String ocelotjs, final String ocelotjsmin) throws IOException {
		request = mock(HttpServletRequest.class);
		ServletContext servletContext = mock(ServletContext.class);
		response = mock(HttpServletResponse.class);
		when(request.getParameter(Constants.MINIFY_PARAMETER)).thenReturn(minify);
		StringWriter result = new StringWriter();
		PrintWriter writer = new PrintWriter(result);
		when(response.getWriter()).thenReturn(writer);
		when(request.getServletContext()).thenReturn(servletContext);
		when(servletContext.getInitParameter(eq(Constants.OCELOT_MIN))).thenReturn(ocelotjsmin);
		when(servletContext.getInitParameter(eq(Constants.OCELOT))).thenReturn(ocelotjs);
		return result;
	}

	/**
	 * Test of doGet method, of class JSServlet.
	 * @throws java.io.IOException
	 * @throws javax.servlet.ServletException
	 */
	@Test
	public void testDoGet() throws  IOException, ServletException  {
		System.out.println("doGet");
		String expected = "line1;\nline2;\nline3;";
		testProcessRequest(expected, false);
	}

	/**
	 * Test of doPost method, of class JSServlet.
	 * @throws java.io.IOException
	 * @throws javax.servlet.ServletException
	 */
	@Test
	public void testDoPost() throws  IOException, ServletException  {
		System.out.println("doPost");
		String expected = "line1;line2;line3;";
		testProcessRequest(expected, true);
	}

	/**
	 * Test of getServletInfo method, of class JSServlet.
	 */
	@Test
	public void testGetServletInfo() {
		System.out.println("getServletInfo");
		String expResult = "ocelot.js";
		String result = jsServlet.getServletInfo();
		assertThat(result).isEqualTo(expResult);
	}

	private void testProcessRequest(String expected, boolean minify) throws IOException, ServletException {
		System.out.println("processRequest");
		File file = File.createTempFile("ocelot", ".js");
		try(FileWriter writer = new FileWriter(file)) {
			writer.write(expected);
		}
		StringWriter result;
		if(minify) {
			result = setUp(Constants.TRUE, "none", file.getAbsolutePath());
		}else {
			result = setUp(Constants.FALSE, file.getAbsolutePath(), "none");
		}
		jsServlet.processRequest(request, response);
		ArgumentCaptor<String> captureType = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Integer> captureLength = ArgumentCaptor.forClass(Integer.class);
		verify(response).setContentType(captureType.capture());
		verify(response).setContentLength(captureLength.capture());
		assertThat(captureType.getValue()).isEqualTo(Constants.JSTYPE);
		assertThat(captureLength.getValue()).isEqualTo((int)file.length());
		assertThat(result.toString()).isEqualTo(expected);
	}
}
