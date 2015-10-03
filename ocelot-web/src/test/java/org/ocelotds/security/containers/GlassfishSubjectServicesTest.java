/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security.containers;

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
import org.ocelotds.security.SecurityContext;
import org.slf4j.Logger;
import sun.security.acl.GroupImpl;

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
	public void testGetSetSecurityContext() throws Exception {
		System.out.println("getSecurityContext");
		Principal p = new GrizzlyPrincipal("demouser");
		Group g = new GroupImpl("GROUPNAME");
		Subject subject = new Subject();
		subject.getPrincipals().add(p);
		subject.getPrincipals().add(g);
		SecurityContext expResult = new GlassfishSubjectServices.GlassfishSecurityContext(p, subject);
		instance.setSecurityContext(expResult);
		SecurityContext result = instance.getSecurityContext();
		assertThat(result.getPrincipal()).isEqualTo(expResult.getPrincipal());
		assertThat(result.getSubject()).isEqualTo(expResult.getSubject());
		assertThat(result.toString()).isEqualTo(expResult.toString());
	}
}
