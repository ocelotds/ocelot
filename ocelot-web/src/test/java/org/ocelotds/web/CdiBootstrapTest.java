/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.objects.Result;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class CdiBootstrapTest {
	
	private final static String BEANMANAGER = "java:comp/env/BeanManager";

	/**
	 * Test of getBeanManager method, of class CdiBootstrap.
	 * @throws javax.naming.NamingException
	 */
	@Test
	public void testGetBeanManager() throws NamingException {
		System.out.println("getBeanManager");
		CdiBootstrap cdiBootstrap = spy(CdiBootstrapImpl.class);
		// First time NamingException so null
		when(cdiBootstrap.getInitialContext()).thenThrow(NamingException.class);
		BeanManager result = cdiBootstrap.getBeanManager();
		assertThat(result).isNull();

		cdiBootstrap = spy(CdiBootstrapImpl.class);
		// no exception so result = bm 
		BeanManager bm = mock(BeanManager.class);
		InitialContext ic = mock(InitialContext.class);
		when(ic.lookup(eq(BEANMANAGER))).thenReturn(bm);
		when(cdiBootstrap.getInitialContext()).thenReturn(ic);
		result = cdiBootstrap.getBeanManager();
		assertThat(result).isEqualTo(bm);

		// exception but not reached, so result = bm
		when(cdiBootstrap.getInitialContext()).thenThrow(NamingException.class);
		result = cdiBootstrap.getBeanManager();
		assertThat(result).isEqualTo(bm);
	}

	/**
	 * Test of getBean method, of class CdiBootstrap.
	 * @throws javax.naming.NamingException
	 */
	@Test
	public void testGetBean() throws NamingException {
		System.out.println("getBean");
		CdiBootstrap cdiBootstrap = spy(CdiBootstrapImpl.class);
		Bean b = mock(Bean.class);
		when(b.getBeanClass()).thenReturn(Result.class);
		
		Set<Bean<?>> beans = new HashSet<>();
		beans.add(b);
		
		CreationalContext context = mock(CreationalContext.class);
		
		BeanManager bm = mock(BeanManager.class);
		when(bm.getBeans(eq(Result.class), any(Annotation.class))).thenReturn(beans);
		when(bm.createCreationalContext(eq(b))).thenReturn(context);
		when(bm.getReference(eq(b), eq(Result.class), eq(context))).thenReturn(new Result());
				  
		when(cdiBootstrap.getBeanManager()).thenReturn(bm);

		Object result = cdiBootstrap.getBean(Result.class);
		assertThat(result).isInstanceOf(Result.class);
		
	}

	public static class CdiBootstrapImpl extends CdiBootstrap {
	}
	
}
