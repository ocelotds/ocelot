/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
