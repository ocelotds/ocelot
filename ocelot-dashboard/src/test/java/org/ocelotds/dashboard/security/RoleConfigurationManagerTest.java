/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.dashboard.security;

import java.util.Arrays;
import java.util.Collection;
import javax.enterprise.inject.Instance;
import javax.servlet.ServletContext;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class RoleConfigurationManagerTest {

	@InjectMocks
	@Spy
	RoleConfigurationManager instance;

	@Mock
	private Logger logger;

	@Mock
	private Instance<Collection<String>> ocelotConfigurationRoles;

	@Mock
	private Instance<String> ocelotConfigurationRole;

	/**
	 * Test of readDashboardRolesConfig method, of class RoleConfigurationManager.
	 */
	@Test
	public void testReadDashboardRolesConfig() {
		System.out.println("readDashboardRolesConfig");
		ServletContext sc = mock(ServletContext.class);
		doNothing().when(instance).readFromConfigurationRoles();
		doNothing().when(instance).readFromInitParameter(sc);
		instance.readDashboardRolesConfig(sc);
	}

	/**
	 * Test of readFromConfigurationRoles method, of class RoleConfigurationManager.
	 */
	@Test
	public void testReadFromConfigurationRoles() {
		System.out.println("readFromConfigurationRoles");
		when(ocelotConfigurationRoles.isUnsatisfied()).thenReturn(Boolean.FALSE).thenReturn(Boolean.TRUE);
		when(ocelotConfigurationRoles.get()).thenReturn(Arrays.asList("R1", "R2"));
		instance.getRoles().clear();
		instance.readFromConfigurationRoles();
		assertThat(instance.getRoles()).hasSize(2);
		instance.getRoles().clear();
		instance.readFromConfigurationRoles();
		assertThat(instance.getRoles()).isEmpty();
	}

	/**
	 * Test of readFromInitParameter method, of class RoleConfigurationManager.
	 */
	@Test
	public void testReadFromInitParameter() {
		System.out.println("readFromInitParameter");
		ServletContext sc = mock(ServletContext.class);
		when(sc.getInitParameter(eq(Constants.Options.DASHBOARD_ROLES))).thenReturn(null).thenReturn("R1,R2");
		instance.getRoles().clear();
		instance.readFromInitParameter(sc);
		assertThat(instance.getRoles()).isEmpty();
		instance.getRoles().clear();
		instance.readFromInitParameter(sc);
		assertThat(instance.getRoles()).hasSize(2);
	}

	/**
	 * Test of getRoles method, of class RoleConfigurationManager.
	 */
	@Test
	public void testGetRoles() {
		System.out.println("getRoles");
		Collection<String> result = instance.getRoles();
		assertThat(result).isInstanceOf(Collection.class);
	}

}