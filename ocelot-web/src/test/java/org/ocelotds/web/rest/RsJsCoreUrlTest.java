/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.web.rest;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class RsJsCoreUrlTest {

	@InjectMocks
	@Spy
	RsJsCoreUrl instance;

	/**
	 * Test of getJs method, of class RsJsCoreUrl.
	 */
	@Test
	public void testGetJs() {
		System.out.println("getJs");
		String expResult = "// ok";
		String result = instance.getJs();
		assertThat(result).isEqualTo(expResult);
	}

}