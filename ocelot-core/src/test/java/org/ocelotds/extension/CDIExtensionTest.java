/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.extension;

import java.util.ArrayList;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class CDIExtensionTest {
	
	@Spy
	private CDIExtension instance;

	/**
	 * Test of processAnnotatedType method, of class CDIExtension.
	 */
	@Test
	public void testProcessAnnotatedType() {
		System.out.println("processAnnotatedType");
		AfterTypeDiscovery afd = mock(AfterTypeDiscovery.class);
		when(afd.getInterceptors()).thenReturn(new ArrayList<Class<?>>());
		BeanManager beanManager = null;
		instance.afterTypeDiscovery(afd, beanManager);
		assertThat(afd.getInterceptors()).hasSize(2);
	}
	
}
