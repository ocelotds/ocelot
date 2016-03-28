/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.integration.dataservices.cache;

import org.ocelotds.Constants;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.JsCacheRemove;
import org.ocelotds.annotations.JsCacheRemoveAll;
import org.ocelotds.annotations.JsCacheResult;
import org.ocelotds.integration.objects.Result;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.CDI)
public class CacheDataService {
	
	@JsCacheResult(minute = 5)
	public String getMessageCached(String a, int b) {
		return "FOO";
	}
	
	@JsCacheRemove(cls = CacheDataService.class , methodName = "getMessageCached", keys = {"a","r.integer"})
	public void generateCleanCacheMessage(String a, Result r) {
		
	} 

	@JsCacheRemoveAll
	public void generateCleanAllCacheMessage() {
		
	}
}
