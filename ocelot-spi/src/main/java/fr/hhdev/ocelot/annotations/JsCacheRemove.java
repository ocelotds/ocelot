/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for spscify that the method annotated remove entry cache
 * @author hhfrancois
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface JsCacheRemove {
	Class cls();
	String methodName();
	/**
	 * Rules for parameters about key for cache, separate by coma<br>
	 * The order is important<br>
	 * use json notation.<br>
	 * '*' : use all arguments in calculating cache key<br>
	 * 'obj.id' : mean that for compute the key the value of id from argument named obj will be used.<br>
	 * Exemple {"i", "u.id", "f.name"}
	 * @return set of used keys ordered
	 */
	String[] keys() default {"*"};
}
