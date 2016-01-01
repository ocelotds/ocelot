/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core;

import java.lang.reflect.Method;
import org.junit.Test;
import org.ocelotds.annotations.JsCacheResult;
import org.mockito.runners.MockitoJUnitRunner;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ocelotds.annotations.JsCacheRemove;
import org.ocelotds.annotations.JsCacheRemoveAll;
import org.ocelotds.annotations.JsCacheRemoves;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import org.ocelotds.core.cache.JsCacheAnnotationServices;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class CacheManagerTest {
	
	@Mock
	private Logger logger;
	
	@Mock
	private JsCacheAnnotationServices jsCacheAnnotationServices;

	@Spy
	@InjectMocks
	private CacheManager instance;

	/**
	 * Test of processCacheAnnotations method, of class CacheManager.
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testProcessCacheAnnotations() throws NoSuchMethodException {
		System.out.println("processCacheAnnotations");
		Method method = this.getClass().getMethod("jsCacheResultAnnotatedMethod");
		doNothing().when(instance).processCleanCacheAnnotations(any(Method.class), anyListOf(String.class), anyListOf(String.class));
		doReturn(false).doReturn(true).when(instance).isJsCached(any(Method.class));
		when(jsCacheAnnotationServices.getJsCacheResultDeadline(any(JsCacheResult.class))).thenReturn(6L);
		long result = instance.processCacheAnnotations(method, null, null);
		assertThat(result).isEqualTo(0L);
		result = instance.processCacheAnnotations(method, null, null);
		assertThat(result).isEqualTo(6L);
	}

	/**
	 * Test of isJsCached method, of class CacheManager.
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testIsJsCached() throws NoSuchMethodException {
		System.out.println("isJsCached");
		Method method = this.getClass().getMethod("jsCacheResultAnnotatedMethod");
		boolean result = instance.isJsCached(method);
		assertThat(result).isTrue();
		method = this.getClass().getMethod("nonJsCacheResultAnnotatedMethod");
		result = instance.isJsCached(method);
		assertThat(result).isFalse();
	}

	/**
	 * Test of processCleanCacheAnnotations method, of class CacheManager.
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testProcessCleanCacheAnnotations_JsCacheRemoveAll() throws NoSuchMethodException {
		System.out.println("processCleanCacheAnnotations");
		Method method = this.getClass().getMethod("jsCacheRemoveAllAnnotatedMethod");
		instance.processCleanCacheAnnotations(method, null, null);
		verify(jsCacheAnnotationServices).processJsCacheRemoveAll();
	}
	
	/**
	 * Test of processCleanCacheAnnotations method, of class CacheManager.
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testProcessCleanCacheAnnotations_JsCacheRemove() throws NoSuchMethodException {
		System.out.println("processCleanCacheAnnotations");
		Method method = this.getClass().getMethod("jsCacheRemoveAnnotatedMethodWithAllArgs", Integer.TYPE, String.class);
		instance.processCleanCacheAnnotations(method, null, null);
		verify(jsCacheAnnotationServices).processJsCacheRemove(any(JsCacheRemove.class), anyListOf(String.class), anyListOf(String.class));
	}

	/**
	 * Test of processCleanCacheAnnotations method, of class CacheManager.
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testProcessCleanCacheAnnotations_JsCacheRemove_debug() throws NoSuchMethodException {
		System.out.println("processCleanCacheAnnotations");
		Method method = this.getClass().getMethod("jsCacheRemoveAnnotatedMethodWithAllArgs", Integer.TYPE, String.class);
		when(logger.isDebugEnabled()).thenReturn(Boolean.TRUE);
		instance.processCleanCacheAnnotations(method, null, null);
		verify(logger).debug(anyString(), anyString(), eq(""), eq("y"));
	}

	/**
	 * Test of processCleanCacheAnnotations method, of class CacheManager.
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testProcessCleanCacheAnnotations_JsCacheRemoves() throws NoSuchMethodException {
		System.out.println("processCleanCacheAnnotations");
		Method method = this.getClass().getMethod("jsCacheRemovesAnnotatedMethod", Integer.TYPE, Result.class);
		instance.processCleanCacheAnnotations(method, null, null);
		verify(jsCacheAnnotationServices, times(2)).processJsCacheRemove(any(JsCacheRemove.class), anyListOf(String.class), anyListOf(String.class));
	}

	/**
	 * Test of processCleanCacheAnnotations method, of class CacheManager.
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testProcessCleanCacheAnnotations_JsCacheRemoves_debug() throws NoSuchMethodException {
		System.out.println("processCleanCacheAnnotations");
		Method method = this.getClass().getMethod("jsCacheRemovesAnnotatedMethod", Integer.TYPE, Result.class);
		when(logger.isDebugEnabled()).thenReturn(Boolean.TRUE);
		instance.processCleanCacheAnnotations(method, null, null);
		verify(logger).debug(anyString(), anyString(), eq("s"), eq("ies"));
	}

	/**
	 * Test of processCleanCacheAnnotations method, of class CacheManager.
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testProcessCleanCacheAnnotations_JsCacheRemoveAndJsCacheRemoves_debug() throws NoSuchMethodException {
		System.out.println("processCleanCacheAnnotations");
		Method method = this.getClass().getMethod("jsCacheRemoveAndjsCacheRemovesAnnotatedMethod", Integer.TYPE, Result.class);
		when(logger.isDebugEnabled()).thenReturn(Boolean.TRUE);
		instance.processCleanCacheAnnotations(method, null, null);
		verify(logger).debug(anyString(), anyString(), eq("s"), eq("ies"));
	}

	@JsCacheResult
	public void jsCacheResultAnnotatedMethod() {
		
	}
	
	public void nonJsCacheResultAnnotatedMethod() {
		
	}

	@JsCacheResult
	public void jsCacheResultAnnotatedMethodNoDeadline() {
		
	}

	@JsCacheRemove(cls=CacheManagerTest.class, methodName = "methodName")
	public void jsCacheRemoveAnnotatedMethodWithAllArgs(int a, String b) {
		
	}

	@JsCacheRemoves({
		@JsCacheRemove(cls=CacheManagerTest.class, methodName = "methodName", keys={"b.i"}),
		@JsCacheRemove(cls=CacheManagerTest.class, methodName = "methodName")
	})
	public void jsCacheRemovesAnnotatedMethod(int a, Result b) {
		
	}

	@JsCacheRemoves({
		@JsCacheRemove(cls=CacheManagerTest.class, methodName = "methodName", keys={"b.i"}),
		@JsCacheRemove(cls=CacheManagerTest.class, methodName = "methodName")
	})
	@JsCacheRemove(cls=CacheManagerTest.class, methodName = "methodName")
	public void jsCacheRemoveAndjsCacheRemovesAnnotatedMethod(int a, Result b) {
		
	}

	@JsCacheRemoveAll
	public void jsCacheRemoveAllAnnotatedMethod() {
		
	}

	@JsCacheResult(minute = 10)
	public void jsCacheResultAnnotatedMethodDeadline10Min() {
		
	}

	@JsCacheRemove(cls=CacheManagerTest.class, methodName = "methodName", keys={})
	public void jsCacheRemoveAnnotatedMethodWith0Arg(int a, String b) {
		
	}


	@JsCacheRemove(cls=CacheManagerTest.class, methodName = "methodName", keys={"b.i"})
	public void jsCacheRemoveAnnotatedMethodWithSomeArgs(int a, Result b) {
		
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

//	/**
//	 * Test of getJsCacheResultDeadline method, of class CacheManager.
//	 * @throws java.lang.NoSuchMethodException
//	 */
//	@Test
//	public void testGetJsCacheResultDeadline() throws NoSuchMethodException {
//		System.out.println("getJsCacheResultDeadline");
//		Method method = this.getClass().getMethod("jsCacheResultAnnotatedMethodNoDeadline");
//		JsCacheResult jcr = method.getAnnotation(JsCacheResult.class);
//		long result = instance.getJsCacheResultDeadline(jcr);
//		Calendar deadline = Calendar.getInstance();
//		deadline.add(Calendar.YEAR, 1);
//		assertThat(result).isCloseTo(deadline.getTime().getTime(), Offset.offset(10L));
//
//		method = this.getClass().getMethod("jsCacheResultAnnotatedMethodDeadline10Min");
//		jcr = method.getAnnotation(JsCacheResult.class);
//		result = instance.getJsCacheResultDeadline(jcr);
//		deadline = Calendar.getInstance();
//		deadline.add(Calendar.MINUTE, 10);
//		assertThat(result).isCloseTo(deadline.getTime().getTime(), Offset.offset(10L));
//	}
//
//	/**
//	 * Test of processCacheAnnotations method, of class CacheManager.
//	 * @throws java.lang.NoSuchMethodException
//	 */
//	@Test
//	public void testProcessCacheAnnotations() throws NoSuchMethodException {
//		System.out.println("testProcessCacheAnnotations");
//		Method method = this.getClass().getMethod("testProcessCacheAnnotations");
//		List<String> parameterNames = new ArrayList<>();
//		List<String> parameters = new ArrayList<>();
//		
//		doNothing().when(instance).processCleanCacheAnnotations(any(Method.class), anyListOf(String.class), anyListOf(String.class));
//		doReturn(true).doReturn(false).when(instance).isJsCached(any(Method.class));
//		doReturn(10L).when(instance).getJsCacheResultDeadline(any(JsCacheResult.class));
//		long result = instance.processCacheAnnotations(method, parameterNames, parameters);
//		
//		assertThat(result).isEqualTo(10L);
//
//		result = instance.processCacheAnnotations(method, parameterNames, parameters);				  
//		assertThat(result).isEqualTo(0L);
//	}
//	
//	/**
//	 * Test of processCleanCacheAnnotations method, of class CacheManager.
//	 * @throws java.lang.NoSuchMethodException
//	 */
//	@Test
//	public void testProcessCleanCacheAnnotationsWith0Arg() throws NoSuchMethodException {
//		System.out.println("testProcessCleanCacheAnnotationsWith0Arg");
//		Method method = this.getClass().getMethod("jsCacheRemoveAnnotatedMethodWith0Arg", new Class<?>[] {Integer.TYPE, String.class});
//		List<String> paramNames = Arrays.asList("\"a\"", "\"b\"");
//		List<String> jsonArgs = Arrays.asList("5", "\"stringValue\"");
//		instance.processCleanCacheAnnotations(method, paramNames, jsonArgs);
//
//		ArgumentCaptor<String> captureCacheKey = ArgumentCaptor.forClass(String.class);
//		verify(cacheEvent).fire(captureCacheKey.capture());
//		ArgumentCaptor<MessageToClient> captureMTC = ArgumentCaptor.forClass(MessageToClient.class);
//		verify(wsEvent).fire(captureMTC.capture());
//		String cacheKey = captureCacheKey.getValue();
//		MessageToClient msg = captureMTC.getValue();
//		assertThat(msg.getId()).isEqualTo(Constants.Cache.CLEANCACHE_TOPIC);
//		assertThat(msg.getResponse()).isEqualTo(cacheKey);
//	}
//
//	/**
//	 * Test of processCleanCacheAnnotations method, of class CacheManager.
//	 * @throws java.lang.NoSuchMethodException
//	 */
//	@Test
//	public void testProcessCleanCacheAnnotationsWithAllArg() throws NoSuchMethodException {
//		when(logger.isDebugEnabled()).thenReturn(Boolean.TRUE);
//		System.out.println("testProcessCleanCacheAnnotationsWithAllArg");
//		Method method = this.getClass().getMethod("jsCacheRemoveAnnotatedMethodWithAllArgs", new Class<?>[] {Integer.TYPE, String.class});
//		List<String> paramNames = Arrays.asList("\"a\"", "\"b\"");
//		List<String> jsonArgs = Arrays.asList("5", "\"stringValue\"");
//		instance.processCleanCacheAnnotations(method, paramNames, jsonArgs);
//
//		ArgumentCaptor<String> captureCacheKey = ArgumentCaptor.forClass(String.class);
//		verify(cacheEvent).fire(captureCacheKey.capture());
//		ArgumentCaptor<MessageToClient> captureMTC = ArgumentCaptor.forClass(MessageToClient.class);
//		verify(wsEvent).fire(captureMTC.capture());
//		String cacheKey = captureCacheKey.getValue();
//		MessageToClient msg = captureMTC.getValue();
//		assertThat(msg.getId()).isEqualTo(Constants.Cache.CLEANCACHE_TOPIC);
//		assertThat(msg.getResponse()).isEqualTo(cacheKey);
//	}
//
//	/**
//	 * Test of processCleanCacheAnnotations method, of class CacheManager.
//	 * @throws java.lang.NoSuchMethodException
//	 */
//	@Test
//	public void testProcessCleanCacheAnnotationsWithSomeArg() throws NoSuchMethodException {
//		System.out.println("testProcessCleanCacheAnnotationsWithSomeArg");
//		Method method = this.getClass().getMethod("jsCacheRemoveAnnotatedMethodWithSomeArgs", new Class<?>[] {Integer.TYPE, Result.class});
//		List<String> paramNames = Arrays.asList("\"a\"", "\"b\"");
//		List<String> jsonArgs = Arrays.asList("5", "{\"i\":5}");
//		instance.processCleanCacheAnnotations(method, paramNames, jsonArgs);
//
//		ArgumentCaptor<String> captureCacheKey = ArgumentCaptor.forClass(String.class);
//		verify(cacheEvent).fire(captureCacheKey.capture());
//		ArgumentCaptor<MessageToClient> captureMTC = ArgumentCaptor.forClass(MessageToClient.class);
//		verify(wsEvent).fire(captureMTC.capture());
//		String cacheKey = captureCacheKey.getValue();
//		MessageToClient msg = captureMTC.getValue();
//		assertThat(msg.getId()).isEqualTo(Constants.Cache.CLEANCACHE_TOPIC);
//		assertThat(msg.getResponse()).isEqualTo(cacheKey);
//	}
//
//	/**
//	 * Test of processCleanCacheAnnotations method, of class CacheManager.
//	 * @throws java.lang.NoSuchMethodException
//	 */
//	@Test
//	public void testProcessCleanCacheAnnotationsWithJsCacheRemoves() throws NoSuchMethodException {
//		System.out.println("testProcessCleanCacheAnnotationsWithJsCacheRemoves");
//		Method method = this.getClass().getMethod("jsCacheRemovesAnnotatedMethod", new Class<?>[] {Integer.TYPE, Result.class});
//		List<String> paramNames = Arrays.asList("\"a\"", "\"b\"");
//		List<String> jsonArgs = Arrays.asList("5", "{\"i\":5}");
//		instance.processCleanCacheAnnotations(method, paramNames, jsonArgs);
//
//		ArgumentCaptor<String> captureCacheKey = ArgumentCaptor.forClass(String.class);
//		verify(cacheEvent, times(2)).fire(captureCacheKey.capture());
//		ArgumentCaptor<MessageToClient> captureMTC = ArgumentCaptor.forClass(MessageToClient.class);
//		verify(wsEvent, times(2)).fire(captureMTC.capture());
//		List<String> cacheKeys = captureCacheKey.getAllValues();
//		List<MessageToClient> msgs = captureMTC.getAllValues();
//		for (MessageToClient msg : msgs) {
//			assertThat(msg.getId()).isEqualTo(Constants.Cache.CLEANCACHE_TOPIC);
//			assertThat(msg.getResponse()).isIn(cacheKeys);
//		}
//	}
//
//	/**
//	 * Test of processCleanCacheAnnotations method, of class CacheManager.
//	 * @throws java.lang.NoSuchMethodException
//	 */
//	@Test
//	public void testProcessCleanCacheAnnotationsWithJsCacheRemoveAll() throws NoSuchMethodException {
//		System.out.println("testProcessCleanCacheAnnotationsWithJsCacheRemoveAll");
//		Method method = this.getClass().getMethod("jsCacheRemoveAllAnnotatedMethod");
//		List<String> paramNames = Arrays.asList();
//		List<String> jsonArgs = Arrays.asList();
//		instance.processCleanCacheAnnotations(method, paramNames, jsonArgs);
//
//		ArgumentCaptor<MessageToClient> captureMTC = ArgumentCaptor.forClass(MessageToClient.class);
//		verify(wsEvent).fire(captureMTC.capture());
//		MessageToClient msg = captureMTC.getValue();
//		assertThat(msg.getId()).isEqualTo(Constants.Cache.CLEANCACHE_TOPIC);
//		assertThat(msg.getResponse()).isEqualTo("ALL");
//	}
}
