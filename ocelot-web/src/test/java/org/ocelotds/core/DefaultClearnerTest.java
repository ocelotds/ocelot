/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hhfrancois
 */
public class DefaultClearnerTest {
	
	/**
	 * Test of cleanArg method, of class DefaultClearner.
	 */
	@Test
	public void testCleanArg() {
		System.out.println("cleanArg");
		String arg = "{\"v\":\"toto\",\"$$hashKey\":\"5\"}";
		DefaultClearner instance = new DefaultClearner();
		String expResult = "{\"v\":\"toto\"}";
		String result = instance.cleanArg(arg);
		assertThat(result).isEqualTo(expResult);
	}
	
}
