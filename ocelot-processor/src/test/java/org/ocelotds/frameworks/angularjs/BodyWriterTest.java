/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.frameworks.angularjs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.frameworks.WriterTest;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class BodyWriterTest {

	@InjectMocks
	@Spy
	BodyWriter instance;

	/**
	 * Test of write method, of class BodyWriter.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testWrite() throws Exception {
		System.out.println("write");
		InputStream body = new ByteArrayInputStream("LINE1\nLINE2\n".getBytes(Charset.defaultCharset()));
		Writer writer = WriterTest.getMockWriter();
		instance.write(writer, body);
		List<String> allValues = WriterTest.captureWrite(writer);
		assertThat(allValues).contains("LINE1", "LINE2");
	}

	/**
	 * Test of getInputStream method, of class BodyWriter.
	 */
	@Test
	public void testGetInputStream() {
		System.out.println("getInputStream");
		InputStream result = instance.getInputStream("/test.js");
		assertThat(result).isNotNull();
	}

}