/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author hhfrancois
 */
public class CDIBeanResolverTest {

	/**
	 * Test of getBeanManager method, of class CDIBeanResolver.
	 * @throws javax.naming.NamingException
	 */
	@Test
	public void testGetBeanManager() throws NamingException {
		System.out.println("getBeanManager");
		CDIBeanResolver instance = spy(new CDIBeanResolver());
		InitialContext ic = mock(InitialContext.class);

		doReturn(ic).when(instance).getInitialContext();
		when(ic.lookup(anyString())).thenThrow(NamingException.class).thenThrow(NamingException.class);

		BeanManager result = instance.getBeanManager();
		assertThat(result).isNull();
	}
}
