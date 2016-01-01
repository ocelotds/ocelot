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
	
	private static final String NAME = "francois";
	
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
		ThreadLocalContextHolder.put(Constants.PRINCIPAL, NAME);
		Object result = ThreadLocalContextHolder.get(Constants.PRINCIPAL);
		assertThat(result).isEqualTo(NAME);
	}

	/**
	 * Test of put method, of class ThreadLocalContextHolder.
	 */
	@Test
	public void testCleanExist() {
		System.out.println("put");
		ThreadLocalContextHolder.put(Constants.PRINCIPAL, NAME);
		ThreadLocalContextHolder.put(Constants.PRINCIPAL, null);
		Object result = ThreadLocalContextHolder.get(Constants.PRINCIPAL);
		assertThat(result).isEqualTo(null);
	}

	/**
	 * Test of put method, of class ThreadLocalContextHolder.
	 */
	@Test
	public void testCleanNotExist() {
		System.out.println("put");
		ThreadLocalContextHolder.put(Constants.PRINCIPAL, NAME);
		ThreadLocalContextHolder.put("FOO", null);
		Object result = ThreadLocalContextHolder.get("FOO");
		assertThat(result).isEqualTo(null);
		result = ThreadLocalContextHolder.get(Constants.PRINCIPAL);
		assertThat(result).isEqualTo(NAME);		
	}

	/**
	 * Test of cleanupThread method, of class ThreadLocalContextHolder.
	 */
	@Test
	public void testCleanupThread() {
		System.out.println("cleanupThread");
		ThreadLocalContextHolder.put(Constants.PRINCIPAL, NAME);
		ThreadLocalContextHolder.cleanupThread();
		Object result = ThreadLocalContextHolder.get(Constants.PRINCIPAL);
		assertThat(result).isNull();
	}
	
}
