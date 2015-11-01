/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.security;

import org.ocelotds.security.UnkownServerSecutityServices;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.spi.security.SecurityContext;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class UnkownServerSecurityServicesTest {

	@Mock
	private Logger logger;

	@InjectMocks
	private UnkownServerSecutityServices instance;

	/**
	 * Test of getSecurityContext method, of class UnkownServerSecutityServices.
	 */
	@Test
	public void testGetSecurityContext() {
		System.out.println("getSecurityContext");
		SecurityContext result = instance.getSecurityContext();
		assertThat(result).isNull();
	}

	/**
	 * Test of setSecurityContext method, of class UnkownServerSecutityServices.
	 */
	@Test
	public void testSetSecurityContext() {
		System.out.println("setSecurityContext");
		SecurityContext securityContext = mock(SecurityContext.class);
		instance.setSecurityContext(securityContext);
	}

}