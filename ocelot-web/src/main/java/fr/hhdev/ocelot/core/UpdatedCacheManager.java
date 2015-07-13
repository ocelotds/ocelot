/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.core;

import fr.hhdev.ocelot.messaging.CacheEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
@Singleton
public class UpdatedCacheManager {
	private final static Logger logger = LoggerFactory.getLogger(UpdatedCacheManager.class);

	private final Map<String, Long> lastupdateTime = new HashMap<>();
	
	public void receiveCacheRemoveEvent(@Observes @CacheEvent String cachekey) {
		Date now = new Date();
		logger.debug("RemoveCache {} at instant {}", cachekey, now);
		lastupdateTime.put(cachekey, now.getTime());
	}

	/**
	 * 
	 * @param map
	 * @return 
	 */
	public Collection<String> getOutDatedCache(Map<String, Long> map) {
		Collection<String> result = new ArrayList<>();
		
		for (String key : map.keySet()) {
			if(lastupdateTime.containsKey(key)) {
				if(lastupdateTime.get(key).compareTo(map.get(key)) == 1) { // map element is smaller of lastupdateTime element
					logger.debug("Cache {} is outdated", key);
					result.add(key);
				}
			}
		}
		return result;
	}
}
