/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.web;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.ssi.ByteArrayServletOutputStream;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.mockito.ArgumentCaptor;

/**
 *
 * @author hhfrancois
 */
public class AbstractFileServletTest {
	private final String EXPECTED = "line1;\nline2;\nline3;";

	private final AbstractFileServlet instance = new AbstractServletImpl();

	HttpServletRequest request;
	HttpServletResponse response;
	ByteArrayServletOutputStream out;
	
	private String filepath;
	
	public void init() throws IOException {
		File file = File.createTempFile("ocelot", ".txt");
		try(FileWriter writer = new FileWriter(file)) {
			writer.write(EXPECTED);
		}
		filepath = file.getAbsolutePath();
		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
		out = new ByteArrayServletOutputStream();
		when(response.getOutputStream()).thenReturn(out);
	}
	
	public void test() {
		ArgumentCaptor<String> captureType = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Integer> captureLength = ArgumentCaptor.forClass(Integer.class);
		verify(response).setContentType(captureType.capture());
		verify(response).setContentLength(captureLength.capture());
		assertThat(captureType.getValue()).isEqualTo("text/plain");
		assertThat(captureLength.getValue()).isEqualTo((int)EXPECTED.length());
		assertThat(new String(out.toByteArray())).isEqualTo(EXPECTED);
	}

	/**
	 * Test of processRequest method, of class AbstractFileServlet.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testProcessRequest() throws Exception {
		System.out.println("processRequest");
		init();
		instance.processRequest(request, response);
		test();
	}

	/**
	 * Test of doGet method, of class AbstractFileServlet.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testDoGet() throws Exception {
		System.out.println("doGet");
		init();
		instance.doGet(request, response);
		test();
	}

	/**
	 * Test of doPost method, of class AbstractFileServlet.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testDoPost() throws Exception {
		System.out.println("doPost");
		init();
		instance.doPost(request, response);
		test();
	}

	/**
	 * Test of getServletInfo method, of class AbstractServlet.
	 */
	/**
	 * Test of getServletInfo method, of class JSServlet.
	 */
	@Test
	public void testGetServletInfo() {
		System.out.println("getServletInfo");
		String result = instance.getServletInfo();
		assertThat(result).isEqualTo("ocelot-servlet");
	}
	
	public class AbstractServletImpl extends AbstractFileServlet {
		@Override
		protected String getFilename(HttpServletRequest request) {
			return filepath;
		}

		@Override
		protected String getMimetype(HttpServletRequest request) {
			return "text/plain";
		}
	}
}