/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.configuration;

import java.io.File;
import java.io.IOException;
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
import org.ocelotds.Constants;
import org.ocelotds.IServicesProvider;
import org.ocelotds.objects.FakeCDI;
import org.ocelotds.objects.HtmlServiceProviderImpl;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class HtmlFileInitializerTest {

	@Mock
	private Logger logger;

	private String ocelothtmlpath = null;

	@Spy
	private Instance<IServicesProvider> htmlServicesProviders = new FakeCDI<>();

	@InjectMocks
	@Spy
	private HtmlFileInitializer instance;

	/**
	 * Test of initHtmlFile method, of class HtmlFileInitializer.
	 */
	@Test
	public void testInitHtmlFile() {
		System.out.println("initHtmlFile");
		ServletContext sc = mock(ServletContext.class);
		when(sc.getContextPath()).thenReturn("/");

		instance.initHtmlFile(sc);
		
		ArgumentCaptor<String> captureKey = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> capturePath = ArgumentCaptor.forClass(String.class);
		verify(sc).setInitParameter(captureKey.capture(), capturePath.capture());

		assertThat(captureKey.getValue()).isEqualTo(Constants.OCELOT_HTML);

		ocelothtmlpath = capturePath.getValue();

		File ocelothtml = new File(ocelothtmlpath);
		assertThat(ocelothtml).exists();
	}

	/**
	 * Test of deleteHtmlFile method, of class HtmlFileInitializer.
	 * @throws java.io.IOException
	 */
	@Test
	public void testDeleteHtmlFile() throws IOException {
		System.out.println("deleteHtmlFile");
		ServletContext sc = mock(ServletContext.class);
		File f = File.createTempFile("file", ".html");
		String filename = f.getAbsolutePath();

		when(sc.getInitParameter(Constants.OCELOT_HTML)).thenReturn(null).thenReturn("").thenReturn("file.html").thenReturn(filename);

		instance.deleteHtmlFile(sc);
		instance.deleteHtmlFile(sc);
		instance.deleteHtmlFile(sc);
		instance.deleteHtmlFile(sc);
		
		assertThat(f).doesNotExist();
	}

	/**
	 * Test of createHtmlFile method, of class ContextListener.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testCreateHtmlFileFailed() throws IOException {
		System.out.println("createHtmlFile");
		ServletContext sc = mock(ServletContext.class);
		when(sc.getContextPath()).thenReturn("/");
		when(instance.createOcelotHtmlFile(anyString())).thenThrow(IOException.class);

		instance.initHtmlFile(sc);

		ArgumentCaptor<String> captureLog = ArgumentCaptor.forClass(String.class);
		verify(logger).error(captureLog.capture(), any(IOException.class));
		assertThat(captureLog.getValue()).isEqualTo("Fail to create ocelot.html.");
	}
	
	/**
	 * Test of createOcelotHtmlFile method, of class ContextListener.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testCreateOcelotHtmlFile() throws IOException {
		System.out.println("createOcelotHtmlFile");
		((FakeCDI) htmlServicesProviders).add(new HtmlServiceProviderImpl());
		File file = instance.createOcelotHtmlFile("/");
		assertThat(file).exists();
	}

	/**
	 * Test of createOcelotHtmlFile method, of class ContextListener.
	 *
	 * @throws java.io.IOException
	 */
	@Test(expected = IOException.class)
	public void testCreateOcelotHtmlFileFail() throws IOException {
		System.out.println("createOcelotHtmlFileFail");
		doReturn(null).when(instance).getContentURL();
		((FakeCDI) htmlServicesProviders).add(new HtmlServiceProviderImpl());
		instance.createOcelotHtmlFile("/");
	}
}