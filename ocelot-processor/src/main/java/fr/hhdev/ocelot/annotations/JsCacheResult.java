/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author hhfrancois
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsCacheResult {
	int year() default 0;
	int month() default 0;
	int day() default 0;
	int hour() default 0;
	int minute() default 0;
	int second() default 0;
	int millisecond() default 0;
	/**
	 * Rules for parameters about key for cache, separate by coma<br>
	 * '-' : parameter is not used<br>
	 * '*' : parameter is used fully<br>
	 * '**' : parameters until the last is used fully<br>
	 * 'id' : parameter.id is used<br>
	 * <b>Example</b> : the method 'package1.class1.method1(Object o1, Object o2, Object o3, Object o4, Object o5)'<br>
	 * is annotated with  @JsCacheResult(keys="-,*,name,**")<br>
	 * so the cache key is compute from package1.class1.method1(o2,o3.name,o4,o5)<br>
	 * @return 
	 */
	String keys() default "**";
}
