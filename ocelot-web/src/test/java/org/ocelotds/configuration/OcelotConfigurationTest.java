/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

import javax.enterprise.inject.Instance;
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
	
	@Mock
	private Instance<String> ocelotConfigurationsStack;

	/**
	 * Test of readConfigFromContext method, of class OcelotConfiguration.
	 */
	@Test
	public void readConfigFromContext() {
		System.out.println("testReadConfigFromContext");
		// given
		ServletContext sc = mock(ServletContext.class);
		// when
		when(sc.getInitParameter(eq(Constants.Options.STACKTRACE_LENGTH))).thenReturn(null).thenReturn("10");
		when(ocelotConfigurationsStack.isUnsatisfied()).thenReturn(true);
		
		// then
		ocelotConfiguration.readStacktraceConfig(sc);
		int result = ocelotConfiguration.getStacktracelength();
		assertThat(result).isEqualTo(20);

		ocelotConfiguration.readStacktraceConfig(sc);
		result = ocelotConfiguration.getStacktracelength();
		assertThat(result).isEqualTo(10);
	}

	/**
	 * Test of readConfigFromContext method, of class OcelotConfiguration.
	 */
	@Test
	public void readConfigFromProducer() {
		System.out.println("testReadConfigFromContext");
		// given
		ServletContext sc = mock(ServletContext.class);
		// when
		when(ocelotConfigurationsStack.isUnsatisfied()).thenReturn(false);
		when(ocelotConfigurationsStack.get()).thenReturn("80");
		
		// then
		ocelotConfiguration.readStacktraceConfig(sc);
		int result = ocelotConfiguration.getStacktracelength();
		assertThat(result).isEqualTo(80);
	}

	/**
	 * Test of getStacktracelength method, of class OcelotConfiguration.
	 */
	@Test
	public void testGetSetStacktracelength() {
		System.out.println("testGetSetStacktracelength");
		int expResult = 20;
		int result = ocelotConfiguration.getStacktracelength();
		assertThat(result).isEqualTo(expResult);
		expResult = 10;
		ocelotConfiguration.setStacktracelength(expResult);
		result = ocelotConfiguration.getStacktracelength();
		assertThat(result).isEqualTo(expResult);
	}
}
