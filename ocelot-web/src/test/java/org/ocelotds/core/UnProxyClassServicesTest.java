/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.core;

import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class UnProxyClassServicesTest {

	@InjectMocks
	@Spy
	UnProxyClassServices instance;

	/**
	 * Test of getRealClass method, of class ServiceTools.
	 *
	 * @throws java.lang.ClassNotFoundException
	 */
	@Test
	public void testGetRealClass() throws ClassNotFoundException {
		System.out.println("getRealClass");
		doReturn("java.lang.String").doThrow(ClassNotFoundException.class).when(instance).getRealClassname(anyString());
		Class result = instance.getRealClass(Integer.class);
		assertThat(result).isEqualTo(String.class);

		result = instance.getRealClass(Integer.class);
		assertThat(result).isEqualTo(Integer.class);
	}

	/**
	 * Test of getRealClassname method, of class ServiceTools.
	 *
	 * @throws java.lang.ClassNotFoundException
	 */
	@Test
	public void testGetRealClassname() throws ClassNotFoundException {
		System.out.println("getRealClassname");
		String proxyname = "java.lang.String$Proxy";

		String result = instance.getRealClassname(proxyname);
		assertThat(result).isEqualTo("java.lang.String");

	}

	/**
	 * Test of getRealClassname method, of class ServiceTools.
	 *
	 * @throws java.lang.ClassNotFoundException
	 */
	@Test(expected = ClassNotFoundException.class)
	public void testGetRealClassnameNotProxy() throws ClassNotFoundException {
		System.out.println("getRealClassname");
		String proxyname = "java.lang.String";

		instance.getRealClassname(proxyname);
	}
}