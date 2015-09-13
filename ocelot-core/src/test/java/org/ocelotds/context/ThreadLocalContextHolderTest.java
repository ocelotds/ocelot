/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.context;

import org.ocelotds.context.ThreadLocalContextHolder;
import java.lang.reflect.Constructor;
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
	 * Test constructor is private
	 * @throws java.lang.InstantiationException
	 * @throws java.lang.IllegalAccessException
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testPrivateConstructor() throws InstantiationException, IllegalAccessException, NoSuchMethodException  {
		Constructor<?>[] constructors = ThreadLocalContextHolder.class.getConstructors();
		assertThat(constructors.length).isEqualTo(0);
		Constructor<ThreadLocalContextHolder> constructor = ThreadLocalContextHolder.class.getDeclaredConstructor();
		assertThat(constructor.getModifiers()).isEqualTo(0);
	   ThreadLocalContextHolder.class.newInstance();
	}

	/**
	 * Test constructor is private
	 * @throws java.lang.InstantiationException
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testPrivateConstructor1() throws InstantiationException, IllegalAccessException  {
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
	@Test
	public void testCleanupThread() {
		System.out.println("cleanupThread");
		ThreadLocalContextHolder.cleanupThread();
		Object locale = ThreadLocalContextHolder.get(Constants.LOCALE);
		assertThat(locale).isNull();
	}
	
}
