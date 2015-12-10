/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.resolvers;

import java.util.HashSet;
import java.util.Set;
import javax.ejb.Stateful;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import org.junit.Test;
import org.ocelotds.spi.Scope;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.ocelotds.annotations.DataService;
import org.ocelotds.spi.DataServiceException;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class CdiResolverTest {
	
	@Mock
	private BeanManager beanManager;

	@InjectMocks
	private CdiResolver cdiResolver;

	/**
	 * Test of resolveDataService method, of class CdiResolver.
	 * @throws org.ocelotds.spi.DataServiceException
	 */
	@Test(expected = DataServiceException.class)
	public void testResolveDataServiceFail() throws DataServiceException {
		System.out.println("resolveDataServiceFail");
		when(beanManager.getBeans(CDIBeanSession.class)).thenReturn(new HashSet<Bean<?>>());
		cdiResolver.resolveDataService(CDIBeanSession.class);
	}

	/**
	 * Test of resolveDataService method, of class CdiResolver.
	 * @throws org.ocelotds.spi.DataServiceException
	 */
	@Test
	public void testResolveDataService() throws DataServiceException  {
		System.out.println("resolveDataService");
		CDIBeanSession expResult = new CDIBeanSession();
		Set<Bean<?>> set = new HashSet<>();
		Bean b = mock(Bean.class);
		set.add(b);
		when(beanManager.getBeans(CDIBeanSession.class)).thenReturn(set);
		when(beanManager.createCreationalContext(any(Bean.class))).thenReturn(null);
		when(beanManager.getReference(any(Bean.class), any(Class.class), any(CreationalContext.class))).thenReturn(expResult);
		Object result = cdiResolver.resolveDataService(CDIBeanSession.class);
		assertThat(result).isInstanceOf(CDIBeanSession.class);
	}

	/**
	 * Test of getScope method, of class PojoResolver.
	 */
	@Test
	public void testGetScopeSession() {
		System.out.println("getScopeSession");
		Scope expResult = Scope.SESSION;
		Scope result = cdiResolver.getScope(CDIBeanSession.class);
		assertThat(result).isEqualTo(expResult);
	}
	
	/**
	 * Test of getScope method, of class PojoResolver.
	 */
	@Test
	public void testGetScopeSession2() {
		System.out.println("getScopeSession");
		Scope expResult = Scope.SESSION;
		Scope result = cdiResolver.getScope(EJBBeanSession.class);
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getScope method, of class PojoResolver.
	 */
	@Test
	public void testGetScopeManaged() {
		System.out.println("getScopeManaged");
		Scope expResult = Scope.MANAGED;
		Scope result = cdiResolver.getScope(CDIBeanManaged.class);
		assertThat(result).isEqualTo(expResult);
	}

	@DataService(resolver = "CDI")
	@Dependent
	public static class CDIBeanSession {}

	@DataService(resolver = "cdi")
	@Stateful
	public static class EJBBeanSession {}

	@DataService(resolver = "CDI")
	public static class CDIBeanManaged {}
}
