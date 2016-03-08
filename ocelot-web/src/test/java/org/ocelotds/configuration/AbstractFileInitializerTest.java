/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
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
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractFileInitializerTest {

	@Mock
	private Logger logger;

	@InjectMocks
	@Spy
	private AbstractFileInitializerImpl instance;

	/**
	 * Test of deleteFile method, of class AbstractFileInitializer.
	 * @throws java.io.IOException
	 */
	@Test
	public void testDeleteFile() throws IOException {
		System.out.println("deleteFile");
		File f = File.createTempFile("test", "tmp");
		String filename = f.getAbsolutePath();
		boolean result = instance.deleteFile("unknownfile");
		assertThat(result).isFalse();
		
		result = instance.deleteFile(filename);		
		assertThat(result).isTrue();
		assertThat(f).doesNotExist();
	}

	/**
	 * Test of getContentURL method, of class AbstractFileInitializer.
	 */
	@Test
	public void testGetContentURL() {
		System.out.println("getContentURL");
		URL result = instance.getContentURL(Constants.SLASH + Constants.OCELOT_CORE + Constants.JS);
		assertThat(result).isNotNull();
	}
	
	/**
	 * Test of writeStreamToOutputStream method, of class AbstractFileInitializer.
	 * @throws java.io.IOException
	 */
	@Test
	public void testWriteStreamToOutputStreamFailed() throws IOException {
		System.out.println("writeStreamToOutputStream");
		OutputStream out = mock(OutputStream.class);
		InputStream in = mock(InputStream.class);
		boolean result = instance.writeStreamToOutputStream(null, null);
		assertThat(result).isFalse();
		
		result = instance.writeStreamToOutputStream(in, null);
		assertThat(result).isFalse();

		result = instance.writeStreamToOutputStream(null, out);
		assertThat(result).isFalse();		

		when(in.read(any(byte[].class))).thenThrow(IOException.class);
		result = instance.writeStreamToOutputStream(in, out);
		assertThat(result).isFalse();		
	}

	/**
	 * Test of writeStreamToOutputStream method, of class AbstractFileInitializer.
	 * @throws java.io.IOException
	 */
	@Test
	public void testWriteStreamToOutputStream() throws IOException {
		System.out.println("writeStreamToOutputStream");
		OutputStream out = mock(OutputStream.class);
		InputStream in = mock(InputStream.class);
		when(in.read(any(byte[].class))).thenReturn(5).thenReturn(-1);
		boolean result = instance.writeStreamToOutputStream(in, out);
		assertThat(result).isTrue();
	}
}