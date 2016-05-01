/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.annotations.JsTopicControl;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class JsTopicCtrlsAnnotationLiteralTest {

	/**
	 * Test of value method, of class JsTopicCtrlsAnnotationLiteral.
	 */
	@Test
	public void testValue() {
		System.out.println("value");
		JsTopicCtrlsAnnotationLiteral instance = new JsTopicCtrlsAnnotationLiteral();
		JsTopicControl[] result = instance.value();
		assertThat(result).isEmpty();
	}

}