/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.dashboard.objects;

import java.util.List;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hhfrancois
 */
public class OcelotServiceTest {

	private OcelotService instance = new OcelotService("instanceName");

	/**
	 * Test of getName method, of class OcelotService.
	 */
	@Test
	public void testGetName() {
		System.out.println("getName");
		String expResult = "instanceName";
		String result = instance.getName();
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getMethods method, of class OcelotService.
	 */
	@Test
	public void testGetMethods() {
		System.out.println("getMethods");
		List<OcelotMethod> result = instance.getMethods();
		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
	}

}