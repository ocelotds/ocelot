/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.frameworks.angularjs;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.frameworks.WriterTest;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class FunctionWriterTest {

	@InjectMocks
	@Spy
	FunctionWriter instance;

	/**
	 * Test of writeInjectDependenciesOnObject method, of class FunctionWriter.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testWriteInjectDependenciesOnObject() throws Exception {
		System.out.println("writeInjectDependenciesOnObject");
		Writer writer = WriterTest.getMockWriter();
		String object = "FACTORY";
		doNothing().when(instance).writeDependencies(eq(writer), eq("'"), eq("dep1"), eq("dep2"));
		instance.writeInjectDependenciesOnObject(writer, object, "dep1", "dep2");
		verify(instance).writeDependencies(eq(writer), eq("'"), eq("dep1"), eq("dep2"));
		List<String> allValues = WriterTest.testBraces(writer);
	}

	/**
	 * Test of writeDependencies method, of class FunctionWriter.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testWriteDependencies() throws Exception {
		System.out.println("writeDependencies");
		Writer writer = new StringWriter();
		instance.writeDependencies(writer, "'");
		assertThat(writer.toString()).matches("");

		writer = new StringWriter();
		instance.writeDependencies(writer, "'", "dep1", "dep2");
		assertThat(writer.toString()).matches("'dep1',\\s+'dep2'");

		writer = new StringWriter();
		instance.writeDependencies(writer, "'", "dep1");
		assertThat(writer.toString()).matches("'dep1'");

		writer = new StringWriter();
		instance.writeDependencies(writer, "", "dep1");
		assertThat(writer.toString()).matches("dep1");
	}

	/**
	 * Test of writeOpenFunctionWithDependencies method, of class FunctionWriter.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testWriteOpenFunctionWithDependencies() throws Exception {
		System.out.println("writeOpenFunctionWithDependencies");
		Writer writer = WriterTest.getMockWriter();
		String object = "FACTORY";
		doNothing().when(instance).writeDependencies(eq(writer), eq(""), eq("dep1"), eq("dep2"));
		instance.writeOpenFunctionWithDependencies(writer, object, "dep1", "dep2");
		instance.writeCloseFunction(writer);
		verify(instance).writeDependencies(eq(writer), eq(""), eq("dep1"), eq("dep2"));
		WriterTest.testBraces(writer);
	}

}