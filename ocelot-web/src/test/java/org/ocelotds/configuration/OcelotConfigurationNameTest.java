/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.configuration;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class OcelotConfigurationNameTest {


	private OcelotConfigurationName instance;

	/**
	 * Test of values method, of class OcelotConfigurationName.
	 */
	@Test
	public void testValues() {
		System.out.println("values");
		OcelotConfigurationName[] result = OcelotConfigurationName.values();
		assertThat(result).isNotEmpty();
	}

	/**
	 * Test of valueOf method, of class OcelotConfigurationName.
	 */
	@Test
	public void testValueOf() {
		System.out.println("valueOf");
		OcelotConfigurationName result = OcelotConfigurationName.valueOf("STACKTRACELENGTH");
		assertThat(result).isEqualTo(OcelotConfigurationName.STACKTRACELENGTH);
	}

}