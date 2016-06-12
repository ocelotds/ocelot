/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.security;

import java.lang.reflect.Method;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class InvocationContextTest {

	/**
	 * Test of getMethod method, of class InvocationContext.
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testGetMethod() throws NoSuchMethodException {
		System.out.println("getMethod");
		Method method = this.getClass().getMethod("testGetMethod");
		Object[] parameters = new Object[] {"FOO1", "FOO2"};
		InvocationContext instance = new InvocationContext(method, parameters);
		Method result = instance.getMethod();
		assertThat(result).isEqualTo(method);
	}

	/**
	 * Test of getParameters method, of class InvocationContext.
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testGetParameters() throws NoSuchMethodException {
		System.out.println("getParameters");
		Method method = this.getClass().getMethod("testGetMethod");
		Object[] parameters = new Object[] {"FOO1", "FOO2"};
		InvocationContext instance = new InvocationContext(method, parameters);
		Object[] result = instance.getParameters();
		assertThat(result).isEqualTo(parameters);
	}
	
	/**
	 * Test of toString method, of class.
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void toStringTest() throws NoSuchMethodException {
		System.out.println("toString");
		Method method = this.getClass().getMethod("testGetMethod");
		InvocationContext instance = new InvocationContext(method, null);
		Object result = instance.toString();
		assertThat(result).isEqualTo("org.ocelotds.security.InvocationContextTest.testGetMethod");
		instance = new InvocationContext(null, null);
		result = instance.toString();
		assertThat(result).isNull();
	}

}