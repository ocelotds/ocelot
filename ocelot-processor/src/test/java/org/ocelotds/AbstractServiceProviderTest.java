/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.*;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractServiceProviderTest {

	@InjectMocks
	@Spy
	private AbstractServiceProviderImpl instance;

	/**
	 * Test of getFilename method, of class AbstractServiceProvider.
	 */
	@Test
	public void testGetJsFilename() {
		System.out.println("getJsFilename");
		String result = instance.getFilename();
		assertThat(result).isEqualTo(AbstractServiceProviderImpl.FILENAME);
	}

	/**
	 * Test of streamJavascriptServices method, of class AbstractServiceProvider.
	 * @throws java.io.UnsupportedEncodingException
	 */
	@Test
	public void testStreamJavascriptServices() throws UnsupportedEncodingException {
		System.out.println("streamJavascriptServices");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		instance.streamJavascriptServices(out);
		String result = new String(out.toByteArray(), "UTF-8");
		assertThat(result).isEqualTo("toto;");
	}

	/**
	 * Test of streamJavascriptServices method, of class AbstractServiceProvider.
	 * @throws java.io.IOException
	 */
	@Test
	public void testStreamJavascriptServicesIOException() throws IOException {
		System.out.println("streamJavascriptServices");
		OutputStream out = mock(OutputStream.class);
		Logger logger = mock(Logger.class);

		doThrow(IOException.class).when(out).write(any(byte[].class), anyInt(), anyInt());
		doReturn(logger).when(instance).getLogger();

		instance.streamJavascriptServices(out);
		ArgumentCaptor<String> captureString = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Throwable> captureError = ArgumentCaptor.forClass(Throwable.class);
		verify(logger).error(captureString.capture(), captureError.capture());
		assertThat(captureString.getValue()).isEqualTo("Generation of '"+AbstractServiceProviderImpl.FILENAME+"' failed.");
	}

	/**
	 * Test of streamJavascriptServices method, of class AbstractServiceProvider.
	 * @throws java.io.IOException
	 */
	@Test
	public void testStreamJavascriptServicesUnknownFile() throws IOException {
		System.out.println("streamJavascriptServices");
		Logger logger = mock(Logger.class);
		OutputStream out = new ByteArrayOutputStream();

		when(instance.getFilename()).thenReturn("unknowfile.js");
		doReturn(logger).when(instance).getLogger();

		instance.streamJavascriptServices(out);
		ArgumentCaptor<String> captureString = ArgumentCaptor.forClass(String.class);
		verify(logger).warn(anyString(), captureString.capture());
		assertThat(captureString.getValue()).isEqualTo("unknowfile.js");
	}

	/**
	 * Test of getLogger method, of class AbstractServiceProvider.
	 */
	@Test
	public void testGetLogger() {
		System.out.println("getLogger");
		Logger expected = LoggerFactory.getLogger(AbstractServiceProvider.class);
		Logger logger = instance.getLogger();
		assertThat(logger).isEqualTo(expected);
	}

	/**
	 * Test of getJsStream method, of class AbstractServiceProvider.
	 */
	@Test
	public void testGetJsStream() {
		System.out.println("getJsStream");
		InputStream in = instance.getJsStream(AbstractServiceProviderImpl.FILENAME);
		assertThat(in).isNotNull();
		in = instance.getJsStream("unknowfile.js");
		assertThat(in).isNull();

		doReturn(null).when(instance).getClassLoader();
		in = instance.getJsStream(AbstractServiceProviderImpl.FILENAME);
		assertThat(in).isNull();
	}

	/**
	 * Test of getClassLoader method, of class AbstractServiceProvider.
	 */
	@Test
	public void testGetClassLoader() {
		System.out.println("getClassLoader");
		ClassLoader classLoader = instance.getClassLoader();
		assertThat(classLoader).isEqualTo(AbstractServiceProvider.class.getClassLoader());
	}
}
