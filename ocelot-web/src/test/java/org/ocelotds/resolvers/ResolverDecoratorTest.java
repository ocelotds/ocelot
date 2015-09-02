/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.resolvers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.spi.Scope;
import static org.assertj.core.api.Assertions.*;
import org.mockito.Spy;
import org.ocelotds.annotations.DataService;
import org.ocelotds.spi.DataServiceException;
import org.ocelotds.spi.IDataServiceResolver;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class ResolverDecoratorTest {

	@DataService(resolver = "pojo")
	public static class ClassOk {}
	public static class ClassNok {}
	
	@Spy 
	private IDataServiceResolver resolver = new PojoResolver();
	
	@InjectMocks
	private final ResolverDecoratorImpl resolverDecoratorImpl = new ResolverDecoratorImpl();

	/**
	 * Test of resolveDataService method, of class ResolverDecorator.
	 * @throws org.ocelotds.spi.DataServiceException
	 */
	@Test
	public void testResolveDataServiceOk() throws DataServiceException {
		System.out.println("resolveDataService");
		Class expResult = ClassOk.class;
		Object result = resolverDecoratorImpl.resolveDataService(expResult);
		assertThat(result).isInstanceOf(expResult);
}

	/**
	 * Test of resolveDataService method, of class ResolverDecorator.
	 * @throws org.ocelotds.spi.DataServiceException
	 */
	@Test(expected = DataServiceException.class)
	public void testResolveDataServiceNok() throws DataServiceException {
		System.out.println("resolveDataService");
		Class expResult = ClassNok.class;
		resolverDecoratorImpl.resolveDataService(expResult);
	}
	
	private static class ResolverDecoratorImpl extends ResolverDecorator {
		@Override
		public Scope getScope(Class clazz) {
			return Scope.MANAGED;
		}
	}
	
}
