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

/**
 *
 * @author hhfrancois
 */
public class UpdatedCacheManagerTest {
	
	/**
	 * Test of receiveCacheRemoveEvent method, of class UpdatedCacheManager.
	 */
	@Test
	public void testReceiveCacheRemoveEvent() {
		System.out.println("receiveCacheRemoveEvent");
		String cachekey = UUID.randomUUID().toString();
		Map<String, Long> map = new HashMap<>();
		Date now = new Date();
		map.put(cachekey, now.getTime());
		UpdatedCacheManager instance = new UpdatedCacheManager();
		instance.receiveCacheRemoveEvent(cachekey);
		Collection<String> result = instance.getOutDatedCache(map);
		assertThat(result).contains(cachekey);
	}
}