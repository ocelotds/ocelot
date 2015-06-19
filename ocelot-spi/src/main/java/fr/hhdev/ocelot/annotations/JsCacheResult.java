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
	/**
	 * Define where should be store the result
	 * @return 
	 */
	Store store() default Store.BROWSER;
	int year() default 0;
	int month() default 0;
	int day() default 0;
	int hour() default 0;
	int minute() default 0;
	int second() default 0;
	int millisecond() default 0;
	/**
	 * Rules for parameters about key for cache, separate by coma<br>
	 * The order is important mostly if you want use JsCacheRemove<br>
	 * use json notation.<br>
	 * '*' : use all arguments in calculating cache key<br>
	 * 'obj.id' : mean that for compute the key the value of id from argument named obj will be used.<br>
	 * Exemple {"i", "u.id", "f.name"}
	 * @return set of used keys ordered
	 */
	String[] keys() default {"*"};
	
	public enum Store {
		/**
		 * The result will not be store in cache
		 */
		NONE, 
		/**
		 * The result should be store in browser storage (localStorage)
		 */
		BROWSER, 
		/**
		 * The result should be store in session storage (sessionStorage)
		 */
		SESSION;
	}
}
