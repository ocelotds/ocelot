/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security.containers;

import java.security.Principal;
import javax.security.auth.Subject;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
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
public class GlassfishSubjectServicesTest {

	@Mock
	private Logger logger;

	@InjectMocks
	private GlassfishSubjectServices instance;

	/**
	 * Test of getSetSubject method, of class GlassfishSubjectServices.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testGetSetSubject() throws Exception {
		System.out.println("getSubject");
		Subject expResult = new Subject();
		Principal principal = mock(Principal.class);
		expResult.getPrincipals().add(principal);
		instance.setSubject(expResult, principal);
		Subject result = instance.getSubject();
		assertThat(result).isEqualTo(expResult);
	}
}
