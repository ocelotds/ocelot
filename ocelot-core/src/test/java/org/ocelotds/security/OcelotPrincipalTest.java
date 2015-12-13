/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

import java.security.Principal;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.ocelotds.context.ThreadLocalContextHolder;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class OcelotPrincipalTest {

	@Mock
	private Logger logger;

	@InjectMocks
	private OcelotPrincipal instance;

	/**
	 * Test of getName method, of class OcelotPrincipal.
	 */
	@Test
	public void testGetName() {
		System.out.println("getName");
		ThreadLocalContextHolder.cleanupThread();
		String result = instance.getName();
		assertThat(result).isEqualTo(Constants.ANONYMOUS);
		String expResult = "username";
		Principal p = mock(Principal.class);
		when(p.getName()).thenReturn(expResult);
		ThreadLocalContextHolder.put(Constants.PRINCIPAL, p);
		result = instance.getName();
		assertThat(result).isEqualTo(expResult);
	}
	
	@Test
	public void testToString() {
		System.out.println("toString");
		ThreadLocalContextHolder.cleanupThread();
		String result = instance.toString();
		assertThat(result).isEqualTo("OcelotPrincipal("+Constants.ANONYMOUS+")");
		String expResult = "username";
		Principal p = mock(Principal.class);
		when(p.getName()).thenReturn(expResult);
		ThreadLocalContextHolder.put(Constants.PRINCIPAL, p);
		result = instance.toString();
		assertThat(result).isEqualTo("OcelotPrincipal("+expResult+")");
	}

}
