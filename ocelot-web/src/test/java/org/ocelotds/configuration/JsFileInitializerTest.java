/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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
import java.util.List;
import org.ocelotds.Constants;
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
	 */
	@Test
	public void testInitOcelotJsFile() {
		System.out.println("initOcelotJsFile");
		ServletContext sc = mock(ServletContext.class);

		when(sc.getContextPath()).thenReturn("/");
		when(sc.getInitParameter(Constants.Options.SECURE)).thenReturn(Constants.TRUE);

		instance.initOcelotJsFile(sc);
		
		ArgumentCaptor<String> captureKey = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> capturePath = ArgumentCaptor.forClass(String.class);
		verify(sc, times(2)).setInitParameter(captureKey.capture(), capturePath.capture());
		List<String> keys = captureKey.getAllValues();
		List<String> paths = capturePath.getAllValues();

		assertThat(keys.get(0)).isEqualTo(Constants.OCELOT);
		assertThat(keys.get(1)).isEqualTo(Constants.OCELOT_MIN);
	
		ocelotjspath = paths.get(0);
		ocelotminjspath = paths.get(1);
	
		File ocelotjs = new File(ocelotjspath);
		assertThat(ocelotjs).exists();
		File ocelotminjs = new File(ocelotminjspath);
		assertThat(ocelotminjs).exists();
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
	 * Test of createJsFile method, of class ContextListener.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testInitOcelotJsFileFailed() throws IOException {
		System.out.println("initOcelotJsFileFailed");
		ServletContext sc = mock(ServletContext.class);
		when(sc.getContextPath()).thenReturn("/");
		when(instance.createOcelotJsFile(anyString())).thenThrow(IOException.class);

		instance.initOcelotJsFile(sc);

		ArgumentCaptor<String> captureLog = ArgumentCaptor.forClass(String.class);
		verify(logger).error(captureLog.capture(), any(IOException.class));
		assertThat(captureLog.getValue()).isEqualTo("Fail to create ocelot.js.");
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
}
