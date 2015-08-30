/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import javax.enterprise.event.Event;
import org.junit.Test;
import org.ocelotds.annotations.JsCacheResult;
import org.mockito.runners.MockitoJUnitRunner;
import org.assertj.core.data.Offset;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ocelotds.Constants;
import org.ocelotds.annotations.JsCacheRemove;
import org.ocelotds.annotations.JsCacheRemoveAll;
import org.ocelotds.annotations.JsCacheRemoves;
import org.ocelotds.messaging.MessageToClient;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class CacheManagerTest {
	
	@Mock
	private Event<MessageToClient> wsEvent;
	@Mock
	private Event<String> cacheEvent;
	
	@InjectMocks
	private final CacheManager cacheManager = new CacheManager();

	/**
	 * Test of isJsCached method, of class CacheManager.
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testIsJsCached() throws NoSuchMethodException {
		System.out.println("isJsCached");
		Method method = this.getClass().getMethod("jsCacheResultAnnotatedMethod");
		boolean result = cacheManager.isJsCached(method);
		assertThat(result).isTrue();
		method = this.getClass().getMethod("nonJsCacheResultAnnotatedMethod");
		result = cacheManager.isJsCached(method);
		assertThat(result).isFalse();
	}

	/**
	 * Test of getJsCacheResultDeadline method, of class CacheManager.
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testGetJsCacheResultDeadline() throws NoSuchMethodException {
		System.out.println("getJsCacheResultDeadline");
		Method method = this.getClass().getMethod("jsCacheResultAnnotatedMethodNoDeadline");
		JsCacheResult jcr = method.getAnnotation(JsCacheResult.class);
		long result = cacheManager.getJsCacheResultDeadline(jcr);
		Calendar deadline = Calendar.getInstance();
		deadline.add(Calendar.YEAR, 1);
		assertThat(result).isCloseTo(deadline.getTime().getTime(), Offset.offset(10L));

		method = this.getClass().getMethod("jsCacheResultAnnotatedMethodDeadline10Min");
		jcr = method.getAnnotation(JsCacheResult.class);
		result = cacheManager.getJsCacheResultDeadline(jcr);
		deadline = Calendar.getInstance();
		deadline.add(Calendar.MINUTE, 10);
		assertThat(result).isCloseTo(deadline.getTime().getTime(), Offset.offset(10L));
	}

	/**
	 * Test of processCleanCacheAnnotations method, of class CacheManager.
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testProcessCleanCacheAnnotationsWith0Arg() throws NoSuchMethodException {
		System.out.println("testProcessCleanCacheAnnotationsWith0Arg");
		Method method = this.getClass().getMethod("jsCacheRemoveAnnotatedMethodWith0Arg", new Class<?>[] {Integer.TYPE, String.class});
		List<String> paramNames = Arrays.asList("\"a\"", "\"b\"");
		List<String> jsonArgs = Arrays.asList("5", "\"stringValue\"");
		cacheManager.processCleanCacheAnnotations(method, paramNames, jsonArgs);

		ArgumentCaptor<String> captureCacheKey = ArgumentCaptor.forClass(String.class);
		verify(cacheEvent).fire(captureCacheKey.capture());
		ArgumentCaptor<MessageToClient> captureMTC = ArgumentCaptor.forClass(MessageToClient.class);
		verify(wsEvent).fire(captureMTC.capture());
		String cacheKey = captureCacheKey.getValue();
		MessageToClient msg = captureMTC.getValue();
		assertThat(msg.getId()).isEqualTo(Constants.Cache.CLEANCACHE_TOPIC);
		assertThat(msg.getResponse()).isEqualTo(cacheKey);
	}

	/**
	 * Test of processCleanCacheAnnotations method, of class CacheManager.
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testProcessCleanCacheAnnotationsWithAllArg() throws NoSuchMethodException {
		System.out.println("testProcessCleanCacheAnnotationsWithAllArg");
		Method method = this.getClass().getMethod("jsCacheRemoveAnnotatedMethodWithAllArgs", new Class<?>[] {Integer.TYPE, String.class});
		List<String> paramNames = Arrays.asList("\"a\"", "\"b\"");
		List<String> jsonArgs = Arrays.asList("5", "\"stringValue\"");
		cacheManager.processCleanCacheAnnotations(method, paramNames, jsonArgs);

		ArgumentCaptor<String> captureCacheKey = ArgumentCaptor.forClass(String.class);
		verify(cacheEvent).fire(captureCacheKey.capture());
		ArgumentCaptor<MessageToClient> captureMTC = ArgumentCaptor.forClass(MessageToClient.class);
		verify(wsEvent).fire(captureMTC.capture());
		String cacheKey = captureCacheKey.getValue();
		MessageToClient msg = captureMTC.getValue();
		assertThat(msg.getId()).isEqualTo(Constants.Cache.CLEANCACHE_TOPIC);
		assertThat(msg.getResponse()).isEqualTo(cacheKey);
	}

	/**
	 * Test of processCleanCacheAnnotations method, of class CacheManager.
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testProcessCleanCacheAnnotationsWithSomeArg() throws NoSuchMethodException {
		System.out.println("testProcessCleanCacheAnnotationsWithSomeArg");
		Method method = this.getClass().getMethod("jsCacheRemoveAnnotatedMethodWithSomeArgs", new Class<?>[] {Integer.TYPE, Result.class});
		List<String> paramNames = Arrays.asList("\"a\"", "\"b\"");
		List<String> jsonArgs = Arrays.asList("5", "{\"i\":5}");
		cacheManager.processCleanCacheAnnotations(method, paramNames, jsonArgs);

		ArgumentCaptor<String> captureCacheKey = ArgumentCaptor.forClass(String.class);
		verify(cacheEvent).fire(captureCacheKey.capture());
		ArgumentCaptor<MessageToClient> captureMTC = ArgumentCaptor.forClass(MessageToClient.class);
		verify(wsEvent).fire(captureMTC.capture());
		String cacheKey = captureCacheKey.getValue();
		MessageToClient msg = captureMTC.getValue();
		assertThat(msg.getId()).isEqualTo(Constants.Cache.CLEANCACHE_TOPIC);
		assertThat(msg.getResponse()).isEqualTo(cacheKey);
	}

	/**
	 * Test of processCleanCacheAnnotations method, of class CacheManager.
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testProcessCleanCacheAnnotationsWithJsCacheRemoves() throws NoSuchMethodException {
		System.out.println("testProcessCleanCacheAnnotationsWithJsCacheRemoves");
		Method method = this.getClass().getMethod("jsCacheRemovesAnnotatedMethod", new Class<?>[] {Integer.TYPE, Result.class});
		List<String> paramNames = Arrays.asList("\"a\"", "\"b\"");
		List<String> jsonArgs = Arrays.asList("5", "{\"i\":5}");
		cacheManager.processCleanCacheAnnotations(method, paramNames, jsonArgs);

		ArgumentCaptor<String> captureCacheKey = ArgumentCaptor.forClass(String.class);
		verify(cacheEvent, times(2)).fire(captureCacheKey.capture());
		ArgumentCaptor<MessageToClient> captureMTC = ArgumentCaptor.forClass(MessageToClient.class);
		verify(wsEvent, times(2)).fire(captureMTC.capture());
		List<String> cacheKeys = captureCacheKey.getAllValues();
		List<MessageToClient> msgs = captureMTC.getAllValues();
		for (MessageToClient msg : msgs) {
			assertThat(msg.getId()).isEqualTo(Constants.Cache.CLEANCACHE_TOPIC);
			assertThat(msg.getResponse()).isIn(cacheKeys);
		}
	}

	/**
	 * Test of processCleanCacheAnnotations method, of class CacheManager.
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testProcessCleanCacheAnnotationsWithJsCacheRemoveAll() throws NoSuchMethodException {
		System.out.println("testProcessCleanCacheAnnotationsWithJsCacheRemoveAll");
		Method method = this.getClass().getMethod("jsCacheRemoveAllAnnotatedMethod");
		List<String> paramNames = Arrays.asList();
		List<String> jsonArgs = Arrays.asList();
		cacheManager.processCleanCacheAnnotations(method, paramNames, jsonArgs);

		ArgumentCaptor<MessageToClient> captureMTC = ArgumentCaptor.forClass(MessageToClient.class);
		verify(wsEvent).fire(captureMTC.capture());
		MessageToClient msg = captureMTC.getValue();
		assertThat(msg.getId()).isEqualTo(Constants.Cache.CLEANCACHE_TOPIC);
		assertThat(msg.getResponse()).isEqualTo("ALL");
	}

	@JsCacheResult
	public void jsCacheResultAnnotatedMethod() {
		
	}
	
	public void nonJsCacheResultAnnotatedMethod() {
		
	}

	@JsCacheResult
	public void jsCacheResultAnnotatedMethodNoDeadline() {
		
	}

	@JsCacheResult(minute = 10)
	public void jsCacheResultAnnotatedMethodDeadline10Min() {
		
	}

	@JsCacheRemove(cls=CacheManagerTest.class, methodName = "methodName", keys={})
	public void jsCacheRemoveAnnotatedMethodWith0Arg(int a, String b) {
		
	}

	@JsCacheRemove(cls=CacheManagerTest.class, methodName = "methodName")
	public void jsCacheRemoveAnnotatedMethodWithAllArgs(int a, String b) {
		
	}

	@JsCacheRemove(cls=CacheManagerTest.class, methodName = "methodName", keys={"b.i"})
	public void jsCacheRemoveAnnotatedMethodWithSomeArgs(int a, Result b) {
		
	}

	@JsCacheRemoves({
		@JsCacheRemove(cls=CacheManagerTest.class, methodName = "methodName", keys={"b.i"}),
		@JsCacheRemove(cls=CacheManagerTest.class, methodName = "methodName")
	})
	public void jsCacheRemovesAnnotatedMethod(int a, Result b) {
		
	}

	@JsCacheRemoveAll
	public void jsCacheRemoveAllAnnotatedMethod() {
		
	}

	public class Result {

		public Result() {
		}
		public Result(int i) {
			this.i = i;
		}
		
		private int i = 0;

		public int getI() {
			return i;
		}
		public void setI(int i) {
			this.i = i;
		}

	}
}
