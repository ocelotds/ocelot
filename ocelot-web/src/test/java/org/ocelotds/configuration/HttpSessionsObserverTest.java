/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ocelotds.configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.HttpHeaders;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.ocelotds.context.ThreadLocalContextHolder;
import org.ocelotds.exceptions.LocaleNotFoundException;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpSessionsObserverTest {

	@InjectMocks
	@Spy
	HttpSessionsObserver instance;
	
	@Mock
	private HttpServletRequest request;
	
	@Mock
	private LocaleExtractor localeExtractor;

	@Mock
	private Logger logger;


	/**
	 * Test of processSessionScopedInit method, of class HttpSessionsObserver.
	 */
	@Test
	public void testProcessSessionScopedInit() {
		System.out.println("processSessionScopedInit "+instance);
		HttpSession session = mock(HttpSession.class);
		instance.processSessionScopedInit(session);
		doNothing().when(instance).setContext(eq(session));
	}

	/**
	 * Test of setContext method, of class HttpSessionsObserver.
	 */
	@Test
	public void testSetContext() {
		System.out.println("setContext");
		HttpSession session = mock(HttpSession.class);
		doReturn(true).when(instance).isMonitored();
		doReturn(Locale.US).when(instance).getLocale();
		instance.setContext(session);
		verify(session).setAttribute(eq(Constants.Options.MONITOR), eq(true));
		verify(session).setAttribute(eq(Constants.LOCALE), eq(Locale.US));
		assertThat(ThreadLocalContextHolder.get(Constants.HTTPSESSION)).isEqualTo(session);
	}

	/**
	 * Test of isMonitored method, of class HttpSessionsObserver.
	 */
	@Test
	public void testIsMonitored() {
		when(request.getParameter(eq(Constants.Options.MONITOR)))
				  .thenReturn("").thenReturn("foo").thenReturn("false").thenReturn("FALSE")
				  .thenReturn("true").thenReturn("TRUE");
		boolean result = instance.isMonitored();
		assertThat(result).isFalse();
		result = instance.isMonitored();
		assertThat(result).isFalse();
		result = instance.isMonitored();
		assertThat(result).isFalse();
		result = instance.isMonitored();
		assertThat(result).isFalse();
		result = instance.isMonitored();
		assertThat(result).isTrue();
		result = instance.isMonitored();
		assertThat(result).isTrue();
	}
	/**
	 * Test of getLocale method, of class RSEndpoint.
	 */
	@Test
	public void testGetLocaleNull() {
		System.out.println("getLocale");
		when(request.getHeaders(eq(HttpHeaders.ACCEPT_LANGUAGE))).thenReturn(null);
		Locale result = instance.getLocale();
		assertThat(result).isEqualTo(Locale.US);
	}
	
	/**
	 * Test of getLocale method, of class RSEndpoint.
	 * @throws org.ocelotds.exceptions.LocaleNotFoundException
	 */
	@Test
	public void testGetLocaleNotFound() throws LocaleNotFoundException {
		System.out.println("getLocale");
		Enumeration<String> enumeration = Collections.enumeration(Arrays.asList("1", "2", "3"));
		when(request.getHeaders(eq(HttpHeaders.ACCEPT_LANGUAGE))).thenReturn(enumeration);
		when(localeExtractor.extractFromAccept(any(String.class))).thenThrow(LocaleNotFoundException.class);
		Locale result = instance.getLocale();
		assertThat(result).isEqualTo(Locale.US);
	}

	/**
	 * Test of getLocale method, of class RSEndpoint.
	 * @throws org.ocelotds.exceptions.LocaleNotFoundException
	 */
	@Test
	public void testGetLocale() throws LocaleNotFoundException {
		System.out.println("getLocale");
		Enumeration<String> enumeration = Collections.enumeration(Arrays.asList("1", "2"));
		when(request.getHeaders(eq(HttpHeaders.ACCEPT_LANGUAGE))).thenReturn(enumeration);
		when(localeExtractor.extractFromAccept(any(String.class))).thenThrow(LocaleNotFoundException.class).thenReturn(Locale.FRANCE);
		Locale result = instance.getLocale();
		assertThat(result).isEqualTo(Locale.FRANCE);
	}
}