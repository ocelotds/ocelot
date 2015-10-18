/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

import javax.servlet.ServletContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.slf4j.Logger;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class OcelotConfigurationTest {

	@Mock
	private Logger logger;

	@InjectMocks
	@Spy
	private OcelotConfiguration ocelotConfiguration;

	/**
	 * Test of readConfigFromContext method, of class OcelotConfiguration.
	 */
	@Test
	public void readConfigFromContext() {
		System.out.println("testReadConfigFromContext");
		// given
		ServletContext sc = mock(ServletContext.class);
		// when
		when(sc.getInitParameter(eq(Constants.Options.STACKTRACE_LENGTH))).thenReturn(null).thenReturn("20");
		// then
		ocelotConfiguration.readConfigFromContext(sc);
		int result = ocelotConfiguration.getStacktracelength();
		assertThat(result).isEqualTo(50);

		ocelotConfiguration.readConfigFromContext(sc);
		result = ocelotConfiguration.getStacktracelength();
		assertThat(result).isEqualTo(20);
	}

	/**
	 * Test of getStacktracelength method, of class OcelotConfiguration.
	 */
	@Test
	public void testGetSetStacktracelength() {
		System.out.println("testGetSetStacktracelength");
		int expResult = 50;
		int result = ocelotConfiguration.getStacktracelength();
		assertThat(result).isEqualTo(expResult);
		expResult = 10;
		ocelotConfiguration.setStacktracelength(expResult);
		result = ocelotConfiguration.getStacktracelength();
		assertThat(result).isEqualTo(expResult);
	}
}
