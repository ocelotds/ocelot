/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdatedCacheManagerTest {
	
	private static final long DELAY = 1000;
	
	@Mock
	private Logger logger;

	@InjectMocks
	private UpdatedCacheManager updatedCacheManager;

	/**
	 * Test of getOutDatedCache method, of class UpdatedCacheManager.
	 */
	@Test
	public void testGetOutDatedCache() {
		System.out.println("getOutDatedCache");
		String cachekey = UUID.randomUUID().toString();
		Map<String, Long> map = new HashMap<>();
		Date now = new Date();
		// test key is outofdate
		map.put(cachekey, now.getTime()-DELAY);
		updatedCacheManager.receiveCacheRemoveEvent(cachekey);
		Collection<String> result = updatedCacheManager.getOutDatedCache(map);
		assertThat(result).isNotNull();
		assertThat(result).contains(cachekey);
	}

	/**
	 * Test of getOutDatedCache method, of class UpdatedCacheManager.
	 */
	@Test
	public void testGetNoYetOutDatedCache() {
		System.out.println("getNoYetOutDatedCache");
		String cachekey = UUID.randomUUID().toString();
		Map<String, Long> map = new HashMap<>();
		Date now = new Date();
		map.put(cachekey, now.getTime()+DELAY);
		updatedCacheManager.receiveCacheRemoveEvent(cachekey);
		Collection<String> result = updatedCacheManager.getOutDatedCache(map);
		assertThat(result).doesNotContain(cachekey);
	}

	/**
	 * Test of getOutDatedCache method, of class UpdatedCacheManager.
	 */
	@Test
	public void testGetNoOutDatedCache() {
		System.out.println("getNoOutDatedCache");
		Map<String, Long> map = new HashMap<>();
		Date now = new Date();
		String cachekey = UUID.randomUUID().toString();
		map.put(cachekey, now.getTime()-DELAY);
		Collection<String> result = updatedCacheManager.getOutDatedCache(map);
		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
	}

	/**
	 * Test of getOutDatedCache method, of class UpdatedCacheManager.
	 */
	@Test
	public void testGetOutDatedCacheNullMap() {
		System.out.println("getNoOutDatedCacheNullMap");
		Collection<String> result = updatedCacheManager.getOutDatedCache(null);
		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
	}
}
