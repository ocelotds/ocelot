/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.security.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class WildflySubjectServicesTest {

	@Mock
	private Logger logger;

	@InjectMocks
	private WildflySubjectServices instance;

	/**
	 * Test of getSetSubject method, of class WildflySubjectServices.
	 */
	@Test
	public void testGetSetSubject() {
		System.out.println("getSubject");
//		Subject expResult = new Subject();
//		Principal principal = new AnybodyPrincipal();
//		expResult.getPrincipals().add(principal);
//		instance.setSubject(expResult, principal);
//		Subject result = instance.getSubject();
//		assertThat(result).isEqualTo(expResult);
	}

}