/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.frameworks;

import org.ocelotds.frameworks.angularjs.AngularFwk;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class AngularFwkTest extends AbstractFwkTest  {

	@InjectMocks
	@Spy
	AngularFwk instance;

	@Override
	FwkWriter getInstance() {
		return instance;
	}

	/**
	 * Test of writeHeaderService method, of class AngularFwk.
	 */
	@Test
	@Override
	public void testWriteHeaderFooterService() throws Exception {
		System.out.println("writeHeaderService");
		super.testWriteHeaderFooterService();
	}

}