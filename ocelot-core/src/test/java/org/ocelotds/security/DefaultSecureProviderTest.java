/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.security;

import java.lang.reflect.Method;
import java.security.Principal;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.context.OcelotContext;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultSecureProviderTest {

	@InjectMocks
	@Spy
	DefaultSecureProvider instance;

	@Mock
	OcelotContext context;

	@Before
	public void initContext() {
		Principal p = mock(Principal.class);
		when(context.getPrincipal()).thenReturn(p);
	}

	/**
	 * Test of checkAccess method, of class DefaultSecureProvider.
	 */
	@Test
	public void testCheckAccess1() throws Exception {
		System.out.println("checkAccess");
		when(context.isUserInRole("ROLE1")).thenReturn(true);
		when(context.isUserInRole("ROLE2")).thenReturn(false);
		instance.checkAccess(getCtx(), new String[] {"ROLE1", "ROLE2"});
	}
	
	/**
	 * Test of checkAccess method, of class DefaultSecureProvider.
	 */
	@Test
	public void testCheckAccess2() throws Exception {
		System.out.println("checkAccess");
		when(context.isUserInRole("ROLE1")).thenReturn(false);
		when(context.isUserInRole("ROLE2")).thenReturn(true);
		instance.checkAccess(getCtx(), new String[] {"ROLE1", "ROLE2"});
	}
	
	/**
	 * Test of checkAccess method, of class DefaultSecureProvider.
	 */
	@Test
	public void testCheckAccess3() throws Exception {
		System.out.println("checkAccess");
		instance.checkAccess(getCtx(), new String[] {});
	}

	/**
	 * Test of checkAccess method, of class DefaultSecureProvider.
	 */
	@Test(expected = IllegalAccessException.class)
	public void testCheckAccess4() throws Exception {
		System.out.println("checkAccess");
		when(context.isUserInRole("ROLE1")).thenReturn(false);
		when(context.isUserInRole("ROLE2")).thenReturn(false);
		instance.checkAccess(getCtx(), new String[] {"ROLE1", "ROLE2"});
	}
	
	InvocationContext getCtx() throws NoSuchMethodException {
		Method m = DefaultSecureProviderTest.class.getDeclaredMethod("getCtx");
		return new InvocationContext(m, new Object[]{"PARAM1", "PARAM2"});
	}

}