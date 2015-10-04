/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.security.containers;

import java.security.Principal;
import java.security.acl.Group;
import javax.security.auth.Subject;
import org.glassfish.grizzly.http.server.GrizzlyPrincipal;
import org.jboss.security.SimpleGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.security.SecurityContext;
import org.slf4j.Logger;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class WildflySecurityServicesTest {

	@Mock
	private Logger logger;

	@InjectMocks
	private WildflySecurityServices instance;

	/**
	 * Test of getSetSubject method, of class WildflySecurityServices.
	 */
	@Test
	public void testGetSetSecurityContext() {
		System.out.println("getSecurityContext");
		Principal p = new GrizzlyPrincipal("demouser");
		Group g = new SimpleGroup("GROUPNAME");
		Subject subject = new Subject();
		subject.getPrincipals().add(p);
		subject.getPrincipals().add(g);
		SecurityContext expResult = new WildflySecurityServices.WildflySecurityContext(p, subject, null);
		instance.setSecurityContext(expResult);
		SecurityContext result = instance.getSecurityContext();
//		assertThat(result.getPrincipal()).isEqualTo(expResult.getPrincipal());
//		assertThat(result.getSubject()).isEqualTo(expResult.getSubject());
	}

}