/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.cache;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class CacheArgumentServicesTest {

	@Mock
	private Logger logger;

	@InjectMocks
	@Spy
	private CacheArgumentServices instance;

	/**
	 * Test of computeArgPart method, of class CacheArgumentServices.
	 */
	@Test
	public void testComputeArgPart_none() {
		System.out.println("computeArgPart");
		String[] keys = new String[] {};
		String result = instance.computeArgPart(keys, null, null);
		assertThat(result).isEqualTo("");
	}

	/**
	 * Test of computeArgPart method, of class CacheArgumentServices.
	 */
	@Test
	public void testComputeArgPart_all() {
		System.out.println("computeArgPart");
		String[] keys = new String[] {"*"};
		List<String> jsonArgs = Arrays.asList("5", "\"foo\"");
		String result = instance.computeArgPart(keys, jsonArgs, null);
		assertThat(result).isEqualTo("[5, \"foo\"]");
	}

	/**
	 * Test of computeArgPart method, of class CacheArgumentServices.
	 */
	@Test
	public void testComputeArgPart() {
		System.out.println("computeArgPart");
		String[] keys = new String[] {"a", "b", "c.d"};
		List<String> jsonArgs = Arrays.asList("5", "\"foo\"", "{\"d\":5, \"e\":6}");
		List<String> paramNames = Arrays.asList("a", "b", "c");
		doReturn("computed").when(instance).computeSpecifiedArgPart(any(String[].class), anyListOf(String.class), anyListOf(String.class));
		String result = instance.computeArgPart(keys, jsonArgs, paramNames);
		assertThat(result).isEqualTo("computed");
	}

	/**
	 * Test of computeSpecifiedArgPart method, of class CacheArgumentServices.
	 */
	@Test
	public void testComputeSpecifiedArgPart1Arg() {
		System.out.println("computeSpecifiedArgPart");
		String[] keys = new String[] {"a"};
		List<String> jsonArgs = Arrays.asList("5");
		List<String> paramNames = Arrays.asList("a");
		doReturn("5").when(instance).processArg(any(String[].class), anyString());
		String result = instance.computeSpecifiedArgPart(keys, jsonArgs, paramNames);
		assertThat(result).isEqualTo("[5]");
	}

	/**
	 * Test of computeSpecifiedArgPart method, of class CacheArgumentServices.
	 */
	@Test
	public void testComputeSpecifiedArgPart2Arg() {
		System.out.println("computeSpecifiedArgPart");
		String[] keys = new String[] {"a", "b"};
		List<String> jsonArgs = Arrays.asList("5", "\"foo\"");
		List<String> paramNames = Arrays.asList("a", "b");
		doReturn("5").doReturn("\"foo\"").when(instance).processArg(any(String[].class), anyString());
		String result = instance.computeSpecifiedArgPart(keys, jsonArgs, paramNames);
		assertThat(result).isEqualTo("[5,\"foo\"]");
	}
	
	/**
	 * Test of computeSpecifiedArgPart method, of class CacheArgumentServices.
	 */
	@Test
	public void testComputeSpecifiedArgPart3Arg() {
		System.out.println("computeSpecifiedArgPart");
		String[] keys = new String[] {"a", "b", "c.d"};
		List<String> jsonArgs = Arrays.asList("5", "\"foo\"", "{\"d\":5, \"e\":6}");
		List<String> paramNames = Arrays.asList("a", "b", "c");
		doReturn("5").doReturn("\"foo\"").doReturn("5").when(instance).processArg(any(String[].class), anyString());
		String result = instance.computeSpecifiedArgPart(keys, jsonArgs, paramNames);
		assertThat(result).isEqualTo("[5,\"foo\",5]");
	}
	
	/**
	 * Test of processArg method, of class CacheArgumentServices.
	 */
	@Test
	public void testProcessArg() {
		System.out.println("processArg");
		String[] path = new String[] {};
		doReturn("jsonarg.b").when(instance).processSubFieldsOfArg(any(String[].class), anyString());
		String result = instance.processArg(path, "jsonarg");
		assertThat(result).isEqualTo("jsonarg");
		path = new String[] {"b"};
		result = instance.processArg(path, "jsonarg");
		assertThat(result).isEqualTo("jsonarg.b");
	}

	/**
	 * Test of processArg method, of class CacheArgumentServices.
	 */
	@Test
	public void testProcessArgFail() {
		System.out.println("processArg");
		String[] path = new String[] {"a", "b"};
		doThrow(Exception.class).when(instance).processSubFieldsOfArg(any(String[].class), anyString());
		String result = instance.processArg(path, "jsonarg");
		assertThat(result).isEqualTo("jsonarg");
	}

	/**
	 * Test of processSubFieldsOfArg method, of class CacheArgumentServices.
	 */
	@Test
	public void testProcessSubFieldsOfArg() {
		System.out.println("processSubFieldsOfArg");
		String[] path = new String[] {};
		String jsonArg = "{\"a\":{\"b\":6}}";
		String result = instance.processSubFieldsOfArg(path, jsonArg);
		assertThat(result).isEqualTo("{\"a\":{\"b\":6}}");
		path = new String[] {"a"};
		result = instance.processSubFieldsOfArg(path, jsonArg);
		assertThat(result).isEqualTo("{\"b\":6}");
		path = new String[] {"a", "b"};
		result = instance.processSubFieldsOfArg(path, jsonArg);
		assertThat(result).isEqualTo("6");
	}
}