/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.resolvers;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hhfrancois
 */
public class DataserviceLiteralTest {

	private DataserviceLiteral instance = new DataserviceLiteral();

	/**
	 * Test of resolver method, of class DataserviceLiteral.
	 */
	@Test
	public void testResolver() {
		System.out.println("resolver");
		String result = instance.resolver();
		assertThat(result).isEqualTo("");
	}

	/**
	 * Test of name method, of class DataserviceLiteral.
	 */
	@Test
	public void testName() {
		System.out.println("name");
		String result = instance.name();
		assertThat(result).isEqualTo("");
	}

}