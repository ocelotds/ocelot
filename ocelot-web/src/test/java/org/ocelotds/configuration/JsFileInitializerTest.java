/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import javax.enterprise.inject.Instance;
import javax.servlet.ServletContext;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.IServicesProvider;
import org.ocelotds.objects.FakeCDI;
import org.slf4j.Logger;
import org.ocelotds.Constants;
import static org.ocelotds.configuration.JsFileInitializer.OCELOT_CORE_RESOURCE;
import org.ocelotds.objects.JsServiceProviderImpl;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class JsFileInitializerTest {

	private String ocelotjspath = null;
	private String ocelotminjspath = null;

	@Mock
	private Logger logger;

	@Spy
	private Instance<IServicesProvider> jsServicesProviders = new FakeCDI<>();

	@InjectMocks
	@Spy
	private JsFileInitializer instance;

	/**
	 * Test of initOcelotJsFile method, of class JsFileInitializer.
	 * @throws java.io.IOException
	 */
	@Test
	public void testInitOcelotJsFile() throws IOException	{
		System.out.println("initOcelotJsFile");
		ServletContext sc = mock(ServletContext.class);
		File file = mock(File.class);

		doReturn(file).when(instance).createOcelotJsFile(anyString());
		doNothing().when(instance).setInitParameterAnMinifyJs(any(ServletContext.class), any(File.class));

		instance.initOcelotJsFile(sc);
		
		verify(instance).setInitParameterAnMinifyJs(any(ServletContext.class), any(File.class));
	}

	/**
	 * Test of createJsFile method, of class ContextListener.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testInitOcelotJsFileFailed() throws IOException {
		System.out.println("initOcelotJsFileFailed");
		ServletContext sc = mock(ServletContext.class);

		doThrow(IOException.class).when(instance).createOcelotJsFile(anyString());
		doNothing().when(instance).setInitParameterAnMinifyJs(any(ServletContext.class), any(File.class));

		instance.initOcelotJsFile(sc);
		
		ArgumentCaptor<String> captureLog = ArgumentCaptor.forClass(String.class);
		verify(logger).error(captureLog.capture(), any(IOException.class));
	}

	/**
	 * Test of deleteJsFile method, of class JsFileInitializer.
	 * @throws java.io.IOException
	 */
	@Test
	public void testDeleteJsFile() throws IOException {
		System.out.println("deleteJsFile");
		ServletContext sc = mock(ServletContext.class);
		File js = File.createTempFile("file", ".js");
		String jsfilename = js.getAbsolutePath();
		File jsmin = File.createTempFile("file-min", ".js");
		String jsminfilename = jsmin.getAbsolutePath();

		when(sc.getInitParameter(Constants.OCELOT)).thenReturn(null).thenReturn("").thenReturn("file.js").thenReturn(jsfilename);
		when(sc.getInitParameter(Constants.OCELOT_MIN)).thenReturn(null).thenReturn("").thenReturn("file-min.js").thenReturn(jsminfilename);

		instance.deleteJsFile(sc);
		instance.deleteJsFile(sc);
		instance.deleteJsFile(sc);
		instance.deleteJsFile(sc);
		
		assertThat(js).doesNotExist();
		assertThat(jsmin).doesNotExist();
	}

	/**
	 * Test of createOcelotJsFile method, of class ContextListener.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testCreateOcelotJsFile() throws IOException {
		System.out.println("createOcelotJsFile");
		((FakeCDI) jsServicesProviders).add(new JsServiceProviderImpl());
		instance.OCELOT_CORE_RESOURCE = Constants.SLASH + Constants.OCELOT_CORE + Constants.JS;
		File file = instance.createOcelotJsFile("/");
		assertThat(file).exists();
	}

	/**
	 * Test of writeOcelotCoreJsFile method, of class ContextListener.
	 *
	 * @throws java.io.IOException
	 */
	@Test(expected = IOException.class)
	public void testWriteOcelotCoreJsFile() throws IOException {
		System.out.println("writeOcelotCoreJsFile");
		OutputStream out = mock(OutputStream.class);
		instance.OCELOT_CORE_RESOURCE = "/badfile";
		instance.writeOcelotCoreJsFile(out, "/");
	}
	
	@Test
	public void testSetInitParameterAnMinifyJs() throws IOException {
		System.out.println("setInitParameterAnMinifyJs");
		ServletContext sc = mock(ServletContext.class);
		File js = mock(File.class);
		File minjs = mock(File.class);
		String pathjs = "/path/file.js";
		String pathminjs = "/path/filemin.js";
		when(js.getAbsolutePath()).thenReturn(pathjs);
		when(minjs.getAbsolutePath()).thenReturn(pathminjs);
		
		doReturn(minjs).when(instance).minifyJs(anyString());
		
		instance.setInitParameterAnMinifyJs(sc, js);
		
		ArgumentCaptor<String> capturePathJs = ArgumentCaptor.forClass(String.class);
		verify(sc).setInitParameter(eq(Constants.OCELOT), capturePathJs.capture());
		ArgumentCaptor<String> capturePathMinJs = ArgumentCaptor.forClass(String.class);
		verify(sc).setInitParameter(eq(Constants.OCELOT_MIN), capturePathMinJs.capture());
		assertThat(capturePathJs.getValue()).isEqualTo(pathjs);
		assertThat(capturePathMinJs.getValue()).isEqualTo(pathminjs);
	}

	@Test
	public void testSetInitParameterAnMinifyJsFailed() throws IOException {
		System.out.println("setInitParameterAnMinifyJs");
		ServletContext sc = mock(ServletContext.class);
		File js = mock(File.class);
		File minjs = mock(File.class);
		String pathjs = "/path/file.js";
		String pathminjs = "/path/filemin.js";
		when(js.getAbsolutePath()).thenReturn(pathjs);
		when(minjs.getAbsolutePath()).thenReturn(pathminjs);
		
		doThrow(IOException.class).when(instance).minifyJs(anyString());
		
		instance.setInitParameterAnMinifyJs(sc, js);
		
		verify(logger).error(anyString());
		ArgumentCaptor<String> capturePathJs = ArgumentCaptor.forClass(String.class);
		verify(sc).setInitParameter(eq(Constants.OCELOT), capturePathJs.capture());
		ArgumentCaptor<String> capturePathMinJs = ArgumentCaptor.forClass(String.class);
		verify(sc).setInitParameter(eq(Constants.OCELOT_MIN), capturePathMinJs.capture());
		assertThat(capturePathJs.getValue()).isEqualTo(pathjs);
		assertThat(capturePathMinJs.getValue()).isEqualTo(pathjs);
	}

	@Test
	public void testMinifyJs() throws IOException {
		System.out.println("minifyJs");
		URL js = this.getClass().getResource(OCELOT_CORE_RESOURCE);

		File minifyJs = instance.minifyJs(js.getFile());
		
		assertThat(minifyJs).isFile();
		assertThat(minifyJs).exists();
		assertThat(minifyJs.getAbsolutePath()).isNotEqualTo(js.getFile());
	}
}
