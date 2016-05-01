/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.annotations;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.inject.Qualifier;
import javax.enterprise.util.Nonbinding;

/**
 *
 * @author hhfrancois
 */
@Qualifier
@Retention(RUNTIME)
@Target({TYPE })
public @interface JsTopicControls {
	@Nonbinding JsTopicControl[] value() default {};
}
