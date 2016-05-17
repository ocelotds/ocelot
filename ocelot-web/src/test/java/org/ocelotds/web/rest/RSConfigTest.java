/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web.rest;

import java.util.HashSet;
import java.util.Set;
import javax.enterprise.inject.Instance;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.core.UnProxyClassServices;
import org.ocelotds.objects.FakeCDI;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class RSConfigTest {

	@InjectMocks
	@Spy
	RSConfig instance;

	@Spy
	Instance<Object> restEndpoints = new FakeCDI<>();

	@Mock
	UnProxyClassServices unProxyClassServices;

	@Mock
	Logger logger;

	/**
	 * Test of getClasses method, of class RSConfig.
	 */
	@Test
	public void testGetClasses() {
		System.out.println("getClasses");
		Set<Class<?>> resources = mock(Set.class);
		doReturn(resources).when(instance).getHashSet();
		doNothing().when(instance).addRestResourceClasses(eq(resources));
		Set<Class<?>> result = instance.getClasses();
		
		assertThat(result).isEqualTo(resources);
	}

	/**
	 * Test of addRestResourceClasses method, of class RSConfig.
	 */
	@Test
	public void testAddRestResourceClasses() {
		System.out.println("addRestResourceClasses");
		((FakeCDI) restEndpoints).add(new RSEndpoint());
		when(unProxyClassServices.getRealClass(eq(RSEndpoint.class))).thenReturn(RSEndpoint.class);
		Set<Class<?>> set = new HashSet<>();
		instance.addRestResourceClasses(set);
		assertThat(set).isNotEmpty();
		assertThat(set).hasSize(1);
		assertThat(set).contains(RSEndpoint.class);
	}

	/**
	 * Test of getHashSet method, of class RSConfig.
	 */
	@Test
	public void testGEtHashSet() {
		System.out.println("getHashSet");
		Set<Class<?>> result = instance.getHashSet();
		assertThat(result).isInstanceOf(Set.class);
	}
	
}