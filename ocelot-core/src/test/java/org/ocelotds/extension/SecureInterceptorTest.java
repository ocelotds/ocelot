/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.extension;

import java.lang.reflect.Method;
import javax.enterprise.inject.Instance;
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
import org.ocelotds.objects.FakeCDI;
import org.ocelotds.security.OcelotSecured;
import org.ocelotds.security.SecureProvider;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class SecureInterceptorTest {

	@InjectMocks
	@Spy
	SecureInterceptor instance;

	@Spy
	Instance<SecureProvider> providers = new FakeCDI<>();

	InvocationContext getCtx() throws Exception {
		InvocationContext ctx = mock(InvocationContext.class);
		Method m = this.getClass().getMethod("testProcessSecure");
		when(ctx.getMethod()).thenReturn(m);
		when(ctx.getParameters()).thenReturn(new Object[] {"FOO1", "FOO2"});
		OcelotSecured ocelotSecured = mock(OcelotSecured.class);
		doReturn(ocelotSecured).when(instance).getOcelotSecuredAnnotation(eq(m));
		doReturn(new MySecureProvider()).when(instance).getSecureProviderImpl(eq(MySecureProvider.class));
		when(ocelotSecured.provider()).thenReturn((Class)MySecureProvider.class);
		when(ocelotSecured.roles()).thenReturn(new String[] {});
		return ctx;
	}

	/**
	 * Test of processSecure method, of class SecureInterceptor.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testProcessSecure() throws Exception {
		InvocationContext ctx = getCtx();
		doReturn(null).when(instance).checkAccess(eq(ctx), any(MySecureProvider.class), any(String[].class));
		when(ctx.proceed()).thenReturn("RESULT");
		Object result = instance.processSecure(ctx);
		assertThat(result).isEqualTo("RESULT");
	}
	
	/**
	 * Test of processSecure method, of class SecureInterceptor.
	 * @throws java.lang.Exception
	 */
	@Test(expected = Exception.class)
	public void testProcessSecureFailed() throws Exception {
		InvocationContext ctx = getCtx();
		doReturn(null).when(instance).checkAccess(eq(ctx), any(MySecureProvider.class), any(String[].class));
		when(ctx.proceed()).thenThrow(Exception.class);
		Object result = instance.processSecure(ctx);
		assertThat(result).isEqualTo("RESULT");
	}

	/**
	 * Test of getOcelotSecuredAnnotation method, of class SecureInterceptor.
	 */
	@Test
	public void testGetOcelotSecuredAnnotation() throws Exception {
		System.out.println("getOcelotSecuredAnnotation");
		Method method = AnnotedClass.class.getDeclaredMethod("methodNoAnnoted");
		OcelotSecured result = instance.getOcelotSecuredAnnotation(method);
		assertThat(result.roles()).containsExactly("ROLE_ON_CLASS");
		
		method = AnnotedClass.class.getDeclaredMethod("methodAnnoted");
		result = instance.getOcelotSecuredAnnotation(method);
		assertThat(result.roles()).containsExactly("ROLE_ON_METHOD");
	}
	
	/**
	 * Test of checkAccess method, of class.
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void checkAccessTest() throws IllegalAccessException, Exception {
		System.out.println("checkAccess");
		InvocationContext ctx = getCtx();
		MySecureProvider secureProvider = mock(MySecureProvider.class);
		doNothing().when(secureProvider).checkAccess(
				  any(org.ocelotds.security.InvocationContext.class), 
				  any(String[].class));
		org.ocelotds.security.InvocationContext ic = instance.checkAccess(ctx, null, new String[] {"ROLE1", "ROLE2"});
		assertThat(ic).isNull();
		ic = instance.checkAccess(getCtx(), secureProvider, new String[] {"ROLE1", "ROLE2"});
		assertThat(ic.getMethod()).isEqualTo(ctx.getMethod());
		verify(secureProvider).checkAccess(eq(ic), any(String[].class));
	}
	
	/**
	 * Test of getSecureProviderImpl method, of class.
	 */
	@Test
	public void getSecureProviderImplTest() {
		System.out.println("getSecureProviderImpl");
		((FakeCDI)providers).add(new MySecureProvider());
		Object result = instance.getSecureProviderImpl(MySecureProvider.class);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(MySecureProvider.class);
		((FakeCDI)providers).clear();
		result = instance.getSecureProviderImpl(MySecureProvider.class);
		assertThat(result).isNull();
	}

	class MySecureProvider implements SecureProvider {

		@Override
		public void checkAccess(org.ocelotds.security.InvocationContext ic, String[] roles) throws IllegalAccessException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
		
	}
	
	@OcelotSecured(roles = {"ROLE_ON_CLASS"})
	class AnnotedClass {
		public void methodNoAnnoted() {
		}
		@OcelotSecured(roles = {"ROLE_ON_METHOD"})
		public void methodAnnoted() {
		}
		
	}

}