/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.security;

import java.lang.reflect.Method;
import java.security.Principal;
import javax.interceptor.InvocationContext;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.annotations.RolesAllowed;
import org.ocelotds.context.OcelotContext;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class RolesAllowedInterceptorTest {

	@Mock
	private Logger logger;

	@InjectMocks
	@Spy
	private RolesAllowedInterceptor instance;

	@Mock
	private OcelotContext ocelotContext;


	@Before
	public void init() {
		when(ocelotContext.isUserInRole("OK")).thenReturn(Boolean.TRUE);
		when(ocelotContext.isUserInRole("NOK")).thenReturn(Boolean.FALSE);
		Principal p = mock(Principal.class);
		when(p.getName()).thenReturn("USERNAME");
		when(ocelotContext.getPrincipal()).thenReturn(p);
	}
	/**
	 * Test of checkRolesAllowed method, of class RolesAllowedInterceptor.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testCheckRolesAllowed() throws Exception {
		System.out.println("checkRolesAllowed");
		Method method = this.getClass().getMethod("methodAllowed");
		InvocationContext ctx = mock(InvocationContext.class);
		when(ctx.getMethod()).thenReturn(method);

		instance.checkRolesAllowed(ctx);
	}
	
	/**
	 * Test of checkRolesAllowed method, of class RolesAllowedInterceptor.
	 * @throws java.lang.Exception
	 */
	@Test(expected = IllegalAccessException.class)
	public void testCheckRolesNotAllowed() throws Exception {
		System.out.println("checkRolesNotAllowed");
		Method method = this.getClass().getMethod("methodNotAllowed");
		InvocationContext ctx = mock(InvocationContext.class);
		when(ctx.getMethod()).thenReturn(method);

		instance.checkRolesAllowed(ctx);
	}

	@RolesAllowed("OK")
	public void methodAllowed() {

	}
	
	@RolesAllowed("NOK")
	public void methodNotAllowed() {

	}

}