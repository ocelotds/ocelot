/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.cache;

import org.ocelotds.annotations.JsCacheRemove;
import org.ocelotds.annotations.JsCacheRemoveAll;
import org.ocelotds.annotations.JsCacheRemoves;
import org.ocelotds.annotations.JsCacheResult;

/**
 *
 * @author hhfrancois
 */
public class CacheAnnotedClass {
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
	public void jsCacheRemovesAnnotatedMethod(int a, CacheManagerTest.Result b) {
		
	}

	@JsCacheRemoves({
		@JsCacheRemove(cls=CacheManagerTest.class, methodName = "methodName", keys={"b.i"}),
		@JsCacheRemove(cls=CacheManagerTest.class, methodName = "methodName")
	})
	@JsCacheRemove(cls=CacheManagerTest.class, methodName = "methodName")
	public void jsCacheRemoveAndjsCacheRemovesAnnotatedMethod(int a, CacheManagerTest.Result b) {
		
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
	public void jsCacheRemoveAnnotatedMethodWithSomeArgs(int a, CacheManagerTest.Result b) {
		
	}
	
}
