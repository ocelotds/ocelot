/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.glassfish;

import java.security.Principal;
import java.security.acl.Group;
import javax.security.auth.Subject;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.glassfish.grizzly.http.server.GrizzlyPrincipal;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import sun.security.acl.GroupImpl;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class GlassfishSecurityServicesTest {

	@Mock
	private Logger logger;

	@InjectMocks
	private GlassfishSecurityServices instance;

	/**
	 * Test of getSetSubject method, of class GlassfishSecurityServices.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testGetSetSecurityContext() throws Exception {
		System.out.println("getSecurityContext");
		Principal p = new GrizzlyPrincipal("demouser");
		Group g = new GroupImpl("GROUPNAME");
		Subject subject = new Subject();
		subject.getPrincipals().add(p);
		subject.getPrincipals().add(g);
		GlassfishSecurityServices.GlassfishSecurityContext expResult = new GlassfishSecurityServices.GlassfishSecurityContext(p, subject);
		instance.setSecurityContext(expResult);
		GlassfishSecurityServices.GlassfishSecurityContext result = (GlassfishSecurityServices.GlassfishSecurityContext) instance.getSecurityContext();
		assertThat(result.getPrincipal()).isEqualTo(expResult.getPrincipal());
		assertThat(result.getSubject()).isEqualTo(expResult.getSubject());
		assertThat(result.toString()).isEqualTo(expResult.toString());
	}
}
