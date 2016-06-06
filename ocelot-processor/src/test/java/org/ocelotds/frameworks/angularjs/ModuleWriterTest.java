/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.frameworks.angularjs;

import java.io.Writer;
import java.util.List;
import org.junit.Test;
import static org.mockito.Mockito.*;
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
public class ModuleWriterTest {

	@InjectMocks
	@Spy
	ModuleWriter instance;

	/**
	 * Test of writeModule method, of class ModuleWriter.
	 */
	@Test
	public void testWriteModule() throws Exception {
		System.out.println("writeModule");
		Writer writer = WriterTest.getMockWriter();
		instance.writeModule(writer);
		List<String> allValues = WriterTest.testBraces(writer);
	}

	/**
	 * Test of writeAddition method, of class ModuleWriter.
	 */
	@Test
	public void testWriteAddition() throws Exception {
		System.out.println("writeAddition");
		Writer writer = WriterTest.getMockWriter();
		String type = "TYPE";
		String name = "NAME";
		instance.writeAddition(writer, type, name);
		List<String> allValues = WriterTest.testBraces(writer);
		assertThat(allValues).contains(type, name);
	}

}