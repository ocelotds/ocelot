/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.extension;

import java.util.ArrayList;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author hhfrancois
 */
public class CDIExtensionTest {
	
	/**
	 * Test of processAnnotatedType method, of class CDIExtension.
	 */
	@Test
	public void testProcessAnnotatedType() {
		System.out.println("processAnnotatedType");
		AfterTypeDiscovery afd = mock(AfterTypeDiscovery.class);
		when(afd.getInterceptors()).thenReturn(new ArrayList<Class<?>>());
		BeanManager beanManager = null;
		CDIExtension instance = new CDIExtension();
		instance.afterTypeDiscovery(afd, beanManager);
		assertEquals("We add one interceptor", 1, afd.getInterceptors().size());
	}
	
}
