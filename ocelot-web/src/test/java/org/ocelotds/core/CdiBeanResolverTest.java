/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.ocelotds.Constants;

/**
 *
 * @author hhfrancois
 */
public class CdiBeanResolverTest {

	
	@Test
	public void testGetInitialContext() throws NamingException {
		CdiBeanResolver cdiBeanResolver = new CdiBeanResolver();
		InitialContext ic = cdiBeanResolver.getInitialContext();
		InitialContext ic2 = cdiBeanResolver.getInitialContext();
		assertThat(ic).isEqualTo(ic2);
	}

	/**
	 * Test of getBeanManager method, of class CdiBeanResolver.
	 * @throws javax.naming.NamingException
	 */
	@Test
	public void testGetBeanManagerFromJee() throws NamingException {
		System.out.println("getBeanManager");
		CdiBeanResolver instance = spy(CdiBeanResolver.class);
		CdiBeanResolver.raz();
		InitialContext ic = mock(InitialContext.class);
		BeanManager bm = mock(BeanManager.class);

		doReturn(ic).when(instance).getInitialContext();
		when(ic.lookup(eq(Constants.BeanManager.BEANMANAGER_JEE))).thenReturn(bm);

		BeanManager result = instance.getBeanManager();
		assertThat(result).isEqualTo(bm);
	}

	/**
	 * Test of getBeanManager method, of class CdiBeanResolver.
	 * @throws javax.naming.NamingException
	 */
	@Test
	public void testGetBeanManagerFromAlt() throws NamingException {
		System.out.println("getBeanManager");
		CdiBeanResolver instance = spy(CdiBeanResolver.class);
		CdiBeanResolver.raz();
		InitialContext ic = mock(InitialContext.class);
		BeanManager bm = mock(BeanManager.class);

		doReturn(ic).when(instance).getInitialContext();
		when(ic.lookup(eq(Constants.BeanManager.BEANMANAGER_JEE))).thenThrow(NamingException.class);
		when(ic.lookup(eq(Constants.BeanManager.BEANMANAGER_ALT))).thenReturn(bm);

		BeanManager result = instance.getBeanManager();
		assertThat(result).isEqualTo(bm);
	}

	/**
	 * Test of getBeanManager method, of class CdiBeanResolver.
	 * @throws javax.naming.NamingException
	 */
	@Test
	public void testGetBeanManagerChainOfNamingException() throws NamingException {
		System.out.println("getBeanManager");
		CdiBeanResolver instance = spy(CdiBeanResolver.class);
		CdiBeanResolver.raz();
		InitialContext ic = mock(InitialContext.class);
		doThrow(NamingException.class).doReturn(ic).when(instance).getInitialContext();
		when(ic.lookup(anyString())).thenThrow(NamingException.class).thenThrow(NamingException.class);

		BeanManager result = instance.getBeanManager();
		assertThat(result).isNull();

		result = instance.getBeanManager();
		assertThat(result).isNull();
	}
	
	@Test
	public void testGetBean() throws NamingException {
		System.out.println("getBean");
		CdiBeanResolver instance = spy(CdiBeanResolver.class);
		CdiBeanResolver.raz();
		InitialContext ic = mock(InitialContext.class);
		BeanManager bm = mock(BeanManager.class);
		Set<Bean<?>> beans = new HashSet<>();
		Bean<?> b = mock(Bean.class);
		beans.add(b);
		CreationalContext context = mock(CreationalContext.class);

		doReturn(ic).when(instance).getInitialContext();
		when(ic.lookup(eq(Constants.BeanManager.BEANMANAGER_JEE))).thenReturn(bm);
		when(bm.getBeans(any(Class.class), any(Annotation.class))).thenReturn(beans);
		when(bm.createCreationalContext(eq(b))).thenReturn(context);
		when(bm.getReference(eq(b), any(Class.class), eq(context))).thenReturn(this);
		Object result = instance.getBean(this.getClass());
		assertThat(result).isInstanceOf(this.getClass());
	}
}
