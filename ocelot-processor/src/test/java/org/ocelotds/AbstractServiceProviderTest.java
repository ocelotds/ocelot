/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.objects.AbstractServiceProviderImpl;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractServiceProviderTest {

	@Mock
	private Logger logger;

	@InjectMocks
	@Spy
	private AbstractServiceProviderImpl instance;

	/**
	 * Test of getJsFilename method, of class AbstractServiceProvider.
	 */
	@Test
	public void testGetJsFilename() {
		System.out.println("getJsFilename");
		String result = instance.getJsFilename();
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
	 */
	@Test
	public void testStreamJavascriptServicesFail() {
		when(instance.getJsFilename()).thenThrow(IOException.class).thenCallRealMethod();
		System.out.println("streamJavascriptServices");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		instance.streamJavascriptServices(out);
		ArgumentCaptor<String> captureString = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Throwable> captureError = ArgumentCaptor.forClass(Throwable.class);
		verify(logger).error(captureString.capture(), captureError.capture());
		assertThat(captureString.getValue()).isEqualTo("Generation of '"+AbstractServiceProviderImpl.FILENAME+"' failed.");
	}


}
