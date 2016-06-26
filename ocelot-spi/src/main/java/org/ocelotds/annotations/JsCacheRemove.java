/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

/**
 * Annotation for specify that the method annotated remove entry cache
 * @author hhfrancois
 */
@Inherited
@InterceptorBinding
@Retention(RUNTIME)
@Target({METHOD, TYPE})
public @interface JsCacheRemove {
	@Nonbinding Class cls();
	@Nonbinding String methodName();
	/**
	 * Rules for parameters about key for cache, separate by coma<br>
	 * The order is important<br>
	 * use json notation.<br>
	 * {} no values : use no arguments in calculating cache key<br>
	 * {'*'} : use all arguments in calculating cache key (default value)<br>
	 * {'obj.id'} : mean that for compute the key the value of id from argument named obj will be used.<br>
	 * Exemple {"i", "u.id", "f.name"}
	 * If the cacheResultKey includes arguments and cacheRemoveKey not, that means that you want to remove all results of this method without considering arguments.
	 * @return set of used keys ordered
	 */
	@Nonbinding String[] keys() default {"*"};
	/**
	 * The remove event is for all user (false) or current user (true)
	 * @return 
	 */
	@Nonbinding boolean userScope() default false;
}
