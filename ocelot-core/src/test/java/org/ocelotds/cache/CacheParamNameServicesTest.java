/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.cache;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class CacheParamNameServicesTest {

	@InjectMocks
	@Spy
	CacheParamNameServices instance;

	@Mock
	Logger logger;
	
	/**
	 * Test of getMethodParamNames method, of class CacheParamNameServices.
	 */
	@Test
	public void testGetMethodParamNames() {
		System.out.println("getMethodParamNames");
		instance.map.clear();
		List<String> result = instance.getMethodParamNames(CacheAnnotedClass.class, "jsCacheRemovesAnnotatedMethod");
		assertThat(result).hasSize(2);
	}

	/**
	 * Test of getMethodParamNames method, of class CacheParamNameServices.
	 */
	@Test
	public void testGetMethodParamNamesInCache() {
		System.out.println("getMethodParamNames");
		instance.map.put(CacheAnnotedClass.class.getName()+".jsCacheRemovesAnnotatedMethod", Arrays.asList("a"));
		List<String> result = instance.getMethodParamNames(CacheAnnotedClass.class, "jsCacheRemovesAnnotatedMethod");
		assertThat(result).hasSize(1);
	}

	/**
	 * Test of getMethodParamNames method, of class CacheParamNameServices.
	 */
	@Test
	public void testGetMethodParamNamesFail() {
		System.out.println("getMethodParamNames");
		List<String> result = instance.getMethodParamNames(CacheAnnotedClass.class, "unknown");
		assertThat(result).hasSize(0);
	}
}