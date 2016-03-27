/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.context;

import java.security.Principal;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class OcelotContextTest {

	@Mock
	private Logger logger;
	
	@InjectMocks
	@Spy
	private OcelotContext instance;
	
	@Mock
	private Principal principal;

	@Mock
	private HttpServletRequest request;

	/**
	 * Test of getHttpSession method, of class OcelotContext.
	 */
	@Test
	public void testGetHttpSession() {
		System.out.println("getHttpSession");
		HttpSession session = mock(HttpSession.class);
		ThreadLocalContextHolder.cleanupThread();
		HttpSession result = instance.getHttpSession();
		assertThat(result).isNull();
		
		ThreadLocalContextHolder.put(Constants.HTTPSESSION, session);
		result = instance.getHttpSession();
		assertThat(result).isEqualTo(session);
	}

	/**
	 * Test of getLocale method, of class OcelotContext.
	 */
	@Test
	public void testGetLocale() {
		System.out.println("getLocale");
		doReturn(null).doReturn(Locale.FRANCE).when(instance).getLocaleFromHttpSession();
		Locale result = instance.getLocale();
		assertThat(result).isEqualTo(Locale.US);

		result = instance.getLocale();
		assertThat(result).isEqualTo(Locale.FRANCE);
	}
	
	/**
	 * Test of getLocaleFromHttpSession method, of class OcelotContext.
	 */
	@Test
	public void testGetLocaleFromNullHttpSession() {
		System.out.println("getLocaleFromHttpSession");
		doReturn(null).when(instance).getHttpSession();
		Locale result = instance.getLocaleFromHttpSession();
		assertThat(result).isNull();
	}

	/**
	 * Test of getLocaleToHttpSession method, of class OcelotContext.
	 */
	@Test
	public void testGetLocaleFromHttpSession() {
		System.out.println("getLocaleFromHttpSession");
		HttpSession httpSession = mock(HttpSession.class);
		doReturn(httpSession).when(instance).getHttpSession();
		when(httpSession.getAttribute(eq(Constants.LOCALE))).thenReturn(Locale.CHINA);
		Locale result = instance.getLocaleFromHttpSession();
		assertThat(result).isEqualTo(Locale.CHINA);
	}
	
	/**
	 * Test of setLocale method, of class OcelotContext.
	 */
	@Test
	public void testSetLocale() {
		System.out.println("setLocale");
		instance.setLocale(Locale.CHINA);
		verify(instance).setLocaleToHttpSession(eq(Locale.CHINA));
	}
	
	
	/**
	 * Test of setLocaleToHttpSession method, of class OcelotContext.
	 */
	@Test
	public void testSetLocaleFromNullHttpSession() {
		System.out.println("setLocaleToHttpSession");
		doReturn(null).when(instance).getHttpSession();
		instance.setLocaleToHttpSession(null);
		verify(logger).warn(anyString());
	}

	/**
	 * Test of setLocaleToHttpSession method, of class OcelotContext.
	 */
	@Test
	public void testRemoveLocaleFromHttpSession() {
		System.out.println("setLocaleToHttpSession");
		HttpSession httpSession = mock(HttpSession.class);
		doReturn(httpSession).when(instance).getHttpSession();
		instance.setLocaleToHttpSession(null);
		verify(httpSession).removeAttribute(eq(Constants.LOCALE));
		verify(httpSession, never()).setAttribute(eq(Constants.LOCALE), any(Locale.class));
	}

	/**
	 * Test of setLocaleToHttpSession method, of class OcelotContext.
	 */
	@Test
	public void testSetLocaleFromHttpSession() {
		HttpSession httpSession = mock(HttpSession.class);
		doReturn(httpSession).when(instance).getHttpSession();
		instance.setLocaleToHttpSession(Locale.CHINA);
		verify(httpSession).removeAttribute(eq(Constants.LOCALE));
		verify(httpSession).setAttribute(eq(Constants.LOCALE), eq(Locale.CHINA));
	}

	/**
	 * Test of getPrincipal method, of class OcelotContext.
	 */
	@Test
	public void testGetPrincipal() {
		System.out.println("getPrincipal");
		Principal result = instance.getPrincipal();
		assertThat(result).isEqualTo(principal);
	}

	@Test
	public void testIsUserInRole() {
		when(instance.getRequest()).thenReturn(null).thenReturn(request);
		when(request.isUserInRole("OK")).thenReturn(Boolean.TRUE);
		when(request.isUserInRole("NOK")).thenReturn(Boolean.FALSE);
		boolean result = instance.isUserInRole("OK");
		assertThat(result).isEqualTo(Boolean.FALSE);
		result = instance.isUserInRole("OK");
		assertThat(result).isEqualTo(Boolean.TRUE);
		result = instance.isUserInRole("NOK");
		assertThat(result).isEqualTo(Boolean.FALSE);
	}
}