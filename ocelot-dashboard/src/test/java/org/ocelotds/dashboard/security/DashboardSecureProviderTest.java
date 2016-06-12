/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.dashboard.security;

import java.security.Principal;
import java.util.Arrays;
import javax.inject.Inject;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.context.OcelotContext;
import org.ocelotds.security.InvocationContext;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class DashboardSecureProviderTest {

	@InjectMocks
	@Spy
	DashboardSecureProvider instance;

	@Mock
	OcelotContext context;
	
	@Mock
	RoleConfigurationManager rcm;

	/**
	 * Test of checkAccess method, of class DashboardSecureProvider.
	 * @throws java.lang.IllegalAccessException
	 */
	@Test(expected = IllegalAccessException.class)
	public void testCheckAccessFailed() throws IllegalAccessException {
		System.out.println("checkAccess");
		InvocationContext ctx = mock(InvocationContext.class);
		Principal principal = mock(Principal.class);
		String[] roles = null;
		when(context.isUserInRole(eq("ROLE"))).thenReturn(Boolean.FALSE);
		when(context.getPrincipal()).thenReturn(principal);
		when(rcm.getRoles()).thenReturn(Arrays.asList("ROLE"));
		instance.checkAccess(ctx, roles);
	}
	
	/**
	 * Test of checkAccess method, of class.
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void checkAccessTest() throws IllegalAccessException {
		System.out.println("checkAccess");
		InvocationContext ctx = mock(InvocationContext.class);
		Principal principal = mock(Principal.class);
		String[] roles = null;
		when(context.isUserInRole(eq("ROLE"))).thenReturn(Boolean.TRUE);
		when(context.getPrincipal()).thenReturn(principal);
		when(rcm.getRoles()).thenReturn(Arrays.asList("ROLE"));
		instance.checkAccess(ctx, roles);
	}

}