/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import java.util.HashSet;
import java.util.Set;
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
public class RSConfigTest {

	@InjectMocks
	@Spy
	RSConfig instance;

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
		Set<Class<?>> set = new HashSet<>();
		instance.addRestResourceClasses(set);
		assertThat(set).isNotEmpty();
		assertThat(set).hasSize(3);
		assertThat(set).contains(RSEndpoint.class);
		assertThat(set).contains(RsJsCore.class);
		assertThat(set).contains(RsJsServices.class);
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