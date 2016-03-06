/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.context;

import java.lang.reflect.Constructor;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.ocelotds.Constants;

/**
 *
 * @author hhfrancois
 */
public class ThreadLocalContextHolderTest {
	
	private static final String KEY = "KEY";
	private static final String VALUE = "VALUE";
	
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
	 * Test of put method, of class ThreadLocalContextHolder.
	 */
	@Test
	public void testPut() {
		System.out.println("put");
		ThreadLocalContextHolder.put(KEY, VALUE);
		Object result = ThreadLocalContextHolder.get(KEY);
		assertThat(result).isEqualTo(VALUE);
	}

	/**
	 * Test of put method, of class ThreadLocalContextHolder.
	 */
	@Test
	public void testCleanExist() {
		System.out.println("put");
		ThreadLocalContextHolder.put(KEY, VALUE);
		ThreadLocalContextHolder.put(KEY, null);
		Object result = ThreadLocalContextHolder.get(KEY);
		assertThat(result).isEqualTo(null);
	}

	/**
	 * Test of put method, of class ThreadLocalContextHolder.
	 */
	@Test
	public void testCleanNotExist() {
		System.out.println("put");
		ThreadLocalContextHolder.put(KEY, VALUE);
		ThreadLocalContextHolder.put("FOO", null);
		Object result = ThreadLocalContextHolder.get("FOO");
		assertThat(result).isEqualTo(null);
		result = ThreadLocalContextHolder.get(KEY);
		assertThat(result).isEqualTo(VALUE);		
	}

	/**
	 * Test of cleanupThread method, of class ThreadLocalContextHolder.
	 */
	@Test
	public void testCleanupThread() {
		System.out.println("cleanupThread");
		ThreadLocalContextHolder.put(KEY, VALUE);
		ThreadLocalContextHolder.cleanupThread();
		Object result = ThreadLocalContextHolder.get(KEY);
		assertThat(result).isNull();
	}
	
}
