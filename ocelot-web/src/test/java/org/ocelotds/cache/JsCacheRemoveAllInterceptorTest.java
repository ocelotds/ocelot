/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.cache;

import javax.interceptor.InvocationContext;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class JsCacheRemoveAllInterceptorTest {

	@InjectMocks
	@Spy
	JsCacheRemoveAllInterceptor instance;
	
	@Mock
	JsCacheAnnotationServices jsCacheAnnotationServices;
	

	/**
	 * Test of processJsCacheRemove method, of class JsCacheRemoveAllInterceptor.
	 */
	@Test
	public void testProcessJsCacheRemove() throws Exception {
		System.out.println("processJsCacheRemove");
		InvocationContext ctx = mock(InvocationContext.class);
		Object result = instance.processJsCacheRemove(ctx);
		verify(jsCacheAnnotationServices).processJsCacheRemoveAll();
		verify(ctx).proceed();
	}

}