/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.i18n;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.ocelotds.Constants;

/**
 *
 * @author hhfrancois
 */
public class ThreadLocalContextHolderTest {
	
	private final String EXPECTED = "fr";
	
	@Before
	public void setUp() {
		ThreadLocalContextHolder.put(Constants.LOCALE, EXPECTED);
	}
	
	/**
	 * Test of put method, of class ThreadLocalContextHolder.
	 * @throws java.lang.InstantiationException
	 * @throws java.lang.IllegalAccessException
	 */
	@Test(expected = IllegalAccessException.class)
	public void testPrivateConstructor() throws InstantiationException, IllegalAccessException  {
	   ThreadLocalContextHolder.class.newInstance();
	}

	/**
	 * Test of put method, of class ThreadLocalContextHolder.
	 */
	@Test
	public void testPut() {
		System.out.println("put");
		Object result = ThreadLocalContextHolder.get(Constants.LOCALE);
		assertThat(result).isEqualTo(EXPECTED);
	}

	/**
	 * Test of cleanupThread method, of class ThreadLocalContextHolder.
	 */
	@Test(expected = NullPointerException.class)
	public void testCleanupThread() {
		System.out.println("cleanupThread");
		ThreadLocalContextHolder.cleanupThread();
		ThreadLocalContextHolder.get(Constants.LOCALE);
	}
	
}
