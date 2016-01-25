/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractFileServletTest {
	private final String EXPECTED = "line1;\nline2;\nline3;";

	@Mock
	private Logger logger;

	@InjectMocks
	@Spy
	private AbstractFileServlet instance = new AbstractServletImpl();

	HttpServletRequest request;
	HttpServletResponse response;
	ByteArrayOutputStream out;
	
	private String filepath;
	
	@Before
	public void init() throws IOException {
		File file = File.createTempFile("ocelot", ".txt");
		try(FileWriter writer = new FileWriter(file)) {
			writer.write(EXPECTED);
		}
		filepath = file.getAbsolutePath();
		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
		out = new ByteArrayOutputStream();
		when(response.getWriter()).thenReturn(new PrintWriter(out));
	}
	
	@After
	public void after() {
		File file = new File(filepath);
		if(file.exists()) {
			file.delete();
		}
	}
	
	public void test() throws IOException {
		ArgumentCaptor<String> captureType = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Integer> captureLength = ArgumentCaptor.forClass(Integer.class);
		verify(response).setContentType(captureType.capture());
		verify(response).setContentLength(captureLength.capture());
		assertThat(captureType.getValue()).isEqualTo("text/plain");
		assertThat(captureLength.getValue()).isEqualTo((int)EXPECTED.length());
		response.getWriter().close();
		assertThat(new String(out.toByteArray())).isEqualTo(EXPECTED);
	}

	/**
	 * Test of processRequest method, of class AbstractFileServlet.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testProcessRequest() throws Exception {
		System.out.println("processRequest");
		instance.processRequest(request, response);
		test();
	}

	/**
	 * Test of processRequest method, of class AbstractFileServlet.
	 * @throws java.lang.Exception
	 */
	@Test(expected = FileNotFoundException.class)
	public void testProcessRequestFailed() throws Exception {
		System.out.println("processRequestFailed");
		doThrow(FileNotFoundException.class).when(instance).getInputStream(any(HttpServletRequest.class));
		instance.processRequest(request, response);
	}

	/**
	 * Test of getInputStream method, of class AbstractFileServlet.
	 * @throws java.io.FileNotFoundException
	 */
	@Test
	public void testGetInputStream() throws FileNotFoundException, IOException {
		System.out.println("getInputStream");
		try (InputStream inputStream = instance.getInputStream(request)) {
			assertThat(inputStream).isNotNull();
		}
	}
	
	/**
	 * Test of getInputStream method, of class AbstractFileServlet.
	 * @throws java.io.FileNotFoundException
	 */
	@Test(expected = FileNotFoundException.class)
	public void testGetInputStreamFailed() throws FileNotFoundException {
		System.out.println("getInputStreamFailed");
		after(); // remove file before launch
		instance.getInputStream(request);
	}
	
	/**
	 * Test of doGet method, of class AbstractFileServlet.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testDoGet() throws Exception {
		System.out.println("doGet");
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
	
	@Test
	public void testGetProtocol() throws IOException {
		System.out.println("getServletInfo");
		when(request.isSecure()).thenReturn(false).thenReturn(true);

		String protocol = instance.getProtocol(request);
		assertThat(protocol).isEqualTo(Constants.WS);
		
		protocol = instance.getProtocol(request);
		assertThat(protocol).isEqualTo(Constants.WSS);
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