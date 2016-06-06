/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.frameworks.angularjs;

import java.io.Writer;
import org.junit.Test;
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
public class ClosureWriterTest {

	@InjectMocks
	@Spy
	ClosureWriter instance;

	/**
	 * Test of writeOpen method, of class ClosureWriter.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testWriteOpenClose() throws Exception {
		System.out.println("writeOpen");
		Writer writer = WriterTest.getMockWriter();
		instance.writeOpen(writer);
		instance.writeClose(writer);
		WriterTest.testBraces(writer);
	}

}