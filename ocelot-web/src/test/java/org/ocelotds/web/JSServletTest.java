/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.ssi.ByteArrayServletOutputStream;
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

	private static final String EXPECTED = "line1;\nline2;\nline3;";

	private JSServlet jsServlet = new JSServlet();
	
	HttpServletRequest request;
	HttpServletResponse response;
	
	public ByteArrayServletOutputStream setUp(String minify) throws IOException {
		File file = File.createTempFile("ocelot", ".js");
		try(FileWriter writer = new FileWriter(file)) {
			writer.write(EXPECTED);
		}
		request = mock(HttpServletRequest.class);
		ServletContext servletContext = mock(ServletContext.class);
		response = mock(HttpServletResponse.class);
		when(request.getParameter(Constants.MINIFY_PARAMETER)).thenReturn(minify);
		ByteArrayServletOutputStream out = new ByteArrayServletOutputStream();
		when(response.getOutputStream()).thenReturn(out);
		when(request.getServletContext()).thenReturn(servletContext);
		when(servletContext.getInitParameter(eq(Constants.OCELOT_MIN))).thenReturn(file.getAbsolutePath());
		when(servletContext.getInitParameter(eq(Constants.OCELOT))).thenReturn(file.getAbsolutePath());
		return out;
	}

	/**
	 * Test of doGet method, of class JSServlet.
	 * @throws java.io.IOException
	 * @throws javax.servlet.ServletException
	 */
	@Test
	public void testProcessRequest_MinFalse() throws  IOException, ServletException  {
		System.out.println("processRequest");
		ByteArrayServletOutputStream result = setUp(Constants.FALSE);
		jsServlet.doGet(request, response);
		ArgumentCaptor<String> captureType = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Integer> captureLength = ArgumentCaptor.forClass(Integer.class);
		verify(response).setContentType(captureType.capture());
		verify(response).setContentLength(captureLength.capture());
		assertThat(captureType.getValue()).isEqualTo(Constants.JSTYPE);
		assertThat(captureLength.getValue()).isEqualTo((int)EXPECTED.length());
		assertThat(new String(result.toByteArray())).isEqualTo(EXPECTED);
	}

	/**
	 * Test of doPost method, of class JSServlet.
	 * @throws java.io.IOException
	 * @throws javax.servlet.ServletException
	 */
	@Test
	public void testProcessRequest_MinTrue() throws  IOException, ServletException  {
		System.out.println("processRequest");
		ByteArrayServletOutputStream result = setUp(Constants.TRUE);
		jsServlet.doPost(request, response);
		ArgumentCaptor<String> captureType = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Integer> captureLength = ArgumentCaptor.forClass(Integer.class);
		verify(response).setContentType(captureType.capture());
		verify(response).setContentLength(captureLength.capture());
		assertThat(captureType.getValue()).isEqualTo(Constants.JSTYPE);
		assertThat(captureLength.getValue()).isEqualTo((int)EXPECTED.length());
		assertThat(new String(result.toByteArray())).isEqualTo(EXPECTED);
	}
}
