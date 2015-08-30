/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hhfrancois
 */
public class JsTopicACAnnotationLiteralTest {

	/**
	 * Test of value method, of class JsTopicACAnnotationLiteral.
	 */
	@Test
	public void testValue() {
		System.out.println("value");
		String expResult = "VALUE";
		JsTopicACAnnotationLiteral instance = new JsTopicACAnnotationLiteral(expResult);
		String result = instance.value();
		assertThat(result).isEqualTo(expResult);
	}

}
