/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.resolvers;

import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.objects.Result;
import org.ocelotds.spi.DataServiceException;
import org.ocelotds.spi.Scope;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class EJBResolverTest {

	String PREFIX = "java:global/";
	String APP_NAME = "java:app/AppName";
	String PATH_SEPARATOR = "/";

	@Mock
	private InitialContext initialContext;

	@InjectMocks
	private EJBResolver ejbResolver = new EJBResolver();

	/**
	 * Test of initJNDIPath method, of class EJBResolver.
	 * @throws javax.naming.NamingException
	 */
	@Test
	public void testInitJNDIPath() throws NamingException {
		System.out.println("initJNDIPath");
		when(initialContext.lookup(APP_NAME)).thenReturn("application-name");
		String expResult = PREFIX + "application-name";
		ejbResolver.initJNDIPath();
		String result = ejbResolver.getJndiPath();
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of initJNDIPath method, of class EJBResolver.
	 * @throws javax.naming.NamingException
	 */
	@Test
	public void testInitJNDIPathFail() throws NamingException {
		System.out.println("initJNDIPathFail");
		when(initialContext.lookup(APP_NAME)).thenThrow(NamingException.class);
		ejbResolver.initJNDIPath();
		String result = ejbResolver.getJndiPath();
		assertThat(result).isEqualTo("");
	}

	/**
	 * Test of resolveDataService method, of class EJBResolver.
	 * @throws javax.naming.NamingException
	 * @throws org.ocelotds.spi.DataServiceException
	 */
	@Test
	public void testResolveDataService() throws NamingException, DataServiceException {
		System.out.println("resolveDataService");
		testInitJNDIPath();
		NamingEnumeration<Binding> list = mock(NamingEnumeration.class);
		when(list.hasMore()).thenReturn(true);
		Binding item = mock(Binding.class);
		when(item.getName()).thenReturn(Result.class.getName());
		when(list.next()).thenReturn(item);
		when(initialContext.listBindings(eq(PREFIX + "application-name"))).thenReturn(list);
		Object expResult = new Result();
		when(initialContext.lookup(PREFIX+"application-name"+PATH_SEPARATOR+Result.class.getName())).thenReturn(expResult);
		Object result = ejbResolver.resolveDataService(Result.class);
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getScope method, of class EJBResolver.
	 */
	@Test
	public void testGetScope() {
		System.out.println("getScope");
		Scope expResult = Scope.MANAGED;
		Scope result = ejbResolver.getScope(Result.class);
		assertThat(result).isEqualTo(expResult);
	}
}
