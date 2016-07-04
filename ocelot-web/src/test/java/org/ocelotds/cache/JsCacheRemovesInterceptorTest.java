/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import javax.interceptor.InvocationContext;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.annotations.JsCacheRemove;
import org.ocelotds.marshalling.ArgumentServices;
import org.ocelotds.marshalling.exceptions.JsonMarshallerException;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class JsCacheRemovesInterceptorTest {

	@InjectMocks
	@Spy
	JsCacheRemovesInterceptor instance;
	
	@Mock
	private CacheParamNameServices cacheParamNameServices;
	
	@Mock
	private JsCacheAnnotationServices jsCacheAnnotationServices;
	
	@Mock
	ArgumentServices argumentServices;
	
	@Before
	public void init() throws JsonMarshallingException, JsonMarshallerException, JsonProcessingException {
		when(argumentServices.getJsonParameters(any(Object[].class), any(Annotation[][].class))).thenReturn(Arrays.asList("5"));
		when(cacheParamNameServices.getMethodParamNames(any(Class.class), anyString())).thenReturn(Arrays.asList("arg"));
	}
	
	/**
	 * Test of processJsCacheRemoves method, of class JsCacheRemovesInterceptor.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testProcessJsCacheRemoves() throws Exception {
		System.out.println("processJsCacheRemoves");
		InvocationContext ctx = mock(InvocationContext.class);
		Method method = CacheAnnotedClass.class.getDeclaredMethod("jsCacheRemovesAnnotatedMethod", Integer.TYPE, CacheManagerTest.Result.class);
		when(ctx.getMethod()).thenReturn(method);
		instance.processJsCacheRemoves(ctx);
		verify(jsCacheAnnotationServices, times(2)).processJsCacheRemove(any(JsCacheRemove.class), anyList(), anyList());
		verify(ctx).proceed();
	}

}