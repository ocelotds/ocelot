/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.resolvers;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.ocelotds.spi.DataServiceException;
import org.ocelotds.spi.Scope;

/**
 *
 * @author hhfrancois
 */
public class PojoResolverTest {
	
	public static class ClassOk {}
	public static class ClassNok {
		public ClassNok(int i) {}
	}

	/**
	 * Test of resolveDataService method, of class PojoResolver.
	 * @throws org.ocelotds.spi.DataServiceException
	 */
	@Test
	public void testResolveDataServiceClassOk() throws DataServiceException, InstantiationException, IllegalAccessException {
		System.out.println("resolveDataServiceClassOk");
		PojoResolver instance = new PojoResolver();
		Class expResult = ClassOk.class;
		Object result = instance.resolveDataService(expResult);
		assertThat(result).isInstanceOf(expResult);
	}

	/**
	 * Test of resolveDataService method, of class PojoResolver.
	 * @throws org.ocelotds.spi.DataServiceException
	 */
	@Test(expected = DataServiceException.class)
	public void testResolveDataServiceClassNok() throws DataServiceException {
		System.out.println("resolveDataServiceClassNok");
		PojoResolver instance = new PojoResolver();
		instance.resolveDataService(ClassNok.class);
	}
	
	/**
	 * Test of getScope method, of class PojoResolver.
	 */
	@Test
	public void testGetScope() {
		System.out.println("getScope");
		PojoResolver instance = new PojoResolver();
		Scope expResult = Scope.MANAGED;
		Scope result = instance.getScope(null);
		assertThat(result).isEqualTo(expResult);
	}
	
}
