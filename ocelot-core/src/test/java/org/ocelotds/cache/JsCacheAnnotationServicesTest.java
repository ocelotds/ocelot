/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.cache;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import javax.enterprise.event.Event;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.ocelotds.KeyMaker;
import org.ocelotds.annotations.JsCacheRemove;
import org.ocelotds.annotations.JsCacheResult;
import org.ocelotds.messaging.MessageToClient;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class JsCacheAnnotationServicesTest {

	@Mock
	private Logger logger;

	@Mock
	Event<MessageToClient> wsEvent;

	@Mock
	Event<String> cacheEvent;

	@Mock
	KeyMaker keyMaker;

	@Mock
	CacheArgumentServices cacheArgumentServices;

	@InjectMocks
	@Spy
	private JsCacheAnnotationServices instance;

	/**
	 * Test of processJsCacheRemoveAll method, of class JsCacheAnnotationServices.
	 */
	@Test
	public void testProcessJsCacheRemoveAll() {
		System.out.println("processJsCacheRemoveAll");
		instance.processJsCacheRemoveAll();
		ArgumentCaptor<MessageToClient> captureMTC = ArgumentCaptor.forClass(MessageToClient.class);
		verify(wsEvent).fire(captureMTC.capture());
		assertThat(captureMTC.getValue().getId()).isEqualTo(Constants.Cache.CLEANCACHE_TOPIC);
		assertThat(captureMTC.getValue().getResponse()).isEqualTo(Constants.Cache.ALL);
	}

	int MILLISECOND = 1;
	int SECOND = 1000 * MILLISECOND;
	int MINUTE = 60 * SECOND;
	int HOUR = 60 * MINUTE;
	int DAY = 24 * HOUR;
	int MONTH = 28 * DAY;
	int YEAR = 364 * DAY;
	// MONTH AND YEAR depend of context

	/**
	 * Test of getJsCacheResultDeadline method, of class JsCacheAnnotationServices.
	 */
	@Test
	public void testGetJsCacheResultDeadline() {
		System.out.println("getJsCacheResultDeadline");
		Calendar c = Calendar.getInstance();
		doReturn(c).when(instance).getNowCalendar();
		long expResult = c.getTime().getTime() + YEAR;
		JsCacheResult jcr = new LiteralJsCacheResult();
		long result = instance.getJsCacheResultDeadline(jcr);
		assertThat(result).isGreaterThanOrEqualTo(expResult);
	}

	/**
	 * Test of getJsCacheResultDeadline method, of class JsCacheAnnotationServices.
	 */
	@Test
	public void testGetJsCacheResultDeadline2() {
		System.out.println("getJsCacheResultDeadline");
		Calendar c = Calendar.getInstance();
		doReturn(c).when(instance).getNowCalendar();
		long expResult = c.getTime().getTime()+MILLISECOND;
		JsCacheResult jcr = new LiteralJsCacheResult(0, 0, 0, 0, 0, 0, 1);
		long result = instance.getJsCacheResultDeadline(jcr);
		assertThat(result).isEqualTo(expResult);

		expResult = c.getTime().getTime()+SECOND;
		jcr = new LiteralJsCacheResult(0, 0, 0, 0, 0, 1, 0);
		result = instance.getJsCacheResultDeadline(jcr);
		assertThat(result).isEqualTo(expResult);

		expResult = c.getTime().getTime()+MINUTE;
		jcr = new LiteralJsCacheResult(0, 0, 0, 0, 1, 0, 0);
		result = instance.getJsCacheResultDeadline(jcr);
		assertThat(result).isEqualTo(expResult);

		expResult = c.getTime().getTime()+HOUR;
		jcr = new LiteralJsCacheResult(0, 0, 0, 1, 0, 0, 0);
		result = instance.getJsCacheResultDeadline(jcr);
		assertThat(result).isEqualTo(expResult);

		expResult = c.getTime().getTime()+DAY;
		jcr = new LiteralJsCacheResult(0, 0, 1, 0, 0, 0, 0);
		result = instance.getJsCacheResultDeadline(jcr);
		assertThat(result).isEqualTo(expResult);

		expResult = c.getTime().getTime()+MONTH;
		jcr = new LiteralJsCacheResult(0, 1, 0, 0, 0, 0, 0);
		result = instance.getJsCacheResultDeadline(jcr);
		assertThat(result).isGreaterThanOrEqualTo(expResult);

		expResult = c.getTime().getTime()+YEAR;
		jcr = new LiteralJsCacheResult(0, 1, 0, 0, 0, 0, 0);
		result = instance.getJsCacheResultDeadline(jcr);
		assertThat(result).isGreaterThanOrEqualTo(expResult);
	}
	
	/**
	 * Test of getNowCalendar method, of class JsCacheAnnotationServices.
	 */
	@Test
	public void testGetNowCalendar() {
		System.out.println("getNowCalendar");
		Calendar result = instance.getNowCalendar();
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Calendar.class);
	}

	/**
	 * Test of processJsCacheRemove method, of class JsCacheAnnotationServices.
	 */
	@Test
	public void testProcessJsCacheRemove() {
		System.out.println("processJsCacheRemove");
		JsCacheRemove jcr = new LiteralJsCacheRemove(this.getClass(), "testProcessJsCacheRemove", new String[] {});
		List<String> paramNames = Arrays.asList("a", "b", "c");
		List<String> jsonArgs = Arrays.asList("1", "2", "3");
		doReturn("MD5").when(instance).computeCacheKey(any(Class.class), anyString(), anyString());
		
		instance.processJsCacheRemove(jcr, paramNames, jsonArgs);
		ArgumentCaptor<MessageToClient> captureMTC = ArgumentCaptor.forClass(MessageToClient.class);
		verify(wsEvent).fire(captureMTC.capture());
		assertThat(captureMTC.getValue().getId()).isEqualTo(Constants.Cache.CLEANCACHE_TOPIC);
		ArgumentCaptor<String> captureCacheKey = ArgumentCaptor.forClass(String.class);
		verify(cacheEvent).fire(captureCacheKey.capture());
		assertThat(captureCacheKey.getValue()).isEqualTo("MD5");
	}

	/**
	 * Test of processJsCacheRemove method, of class JsCacheAnnotationServices.
	 */
	@Test
	public void testProcessJsCacheRemoveLog() {
		System.out.println("processJsCacheRemove");
		JsCacheRemove jcr = new LiteralJsCacheRemove(this.getClass(), "testProcessJsCacheRemove", new String[] {});
		List<String> paramNames = Arrays.asList("a", "b", "c");
		List<String> jsonArgs = Arrays.asList("1", "2", "3");
		when(logger.isDebugEnabled()).thenReturn(true);
		doReturn("MD5").when(instance).computeCacheKey(any(Class.class), anyString(), anyString());
		instance.processJsCacheRemove(jcr, paramNames, jsonArgs);

		verify(logger, times(3)).debug(anyString(), anyString());
	}
	
	/**
	 * Test of computeCacheKey method, of class JsCacheAnnotationServices.
	 */
	@Test
	public void testComputeCacheKey() {
		System.out.println("computeCacheKey");
		when(keyMaker.getMd5(anyString())).thenReturn("MD5");
		String result = instance.computeCacheKey(this.getClass(), "computeCacheKey", "");
		assertThat(result).isEqualTo("MD5");
		result = instance.computeCacheKey(this.getClass(), "computeCacheKey", "7,\"foo\"");
		assertThat(result).isEqualTo("MD5_MD5");
	}

}
