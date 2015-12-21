/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.core.services;

import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.configuration.OcelotConfiguration;
import org.ocelotds.messaging.Fault;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class FaultServicesTest {

	@Mock
	private Logger logger;

	@InjectMocks
	private FaultServices instance;

	@Mock
	private OcelotConfiguration configuration;

	/**
	 * Test of buildFault method, of class FaultServices.
	 */
	@Test
	public void testBuildFault() {
		System.out.println("buildFault");
		when(configuration.getStacktracelength()).thenReturn(1).thenReturn(3);
		try {
			throw new Exception("ERROR_MESSAGE");
		} catch (Exception e) {
			Fault fault = instance.buildFault(e);
			assertThat(fault.getClassname()).isEqualTo("java.lang.Exception");
			assertThat(fault.getMessage()).isEqualTo("ERROR_MESSAGE");
			String[] stacktraces = fault.getStacktrace();
			assertThat(stacktraces).hasSize(1);
			assertThat(stacktraces[0]).startsWith(this.getClass().getName()+".testBuildFault("+this.getClass().getSimpleName()+".java:");

			fault = instance.buildFault(e);
			stacktraces = fault.getStacktrace();
			assertThat(stacktraces).hasSize(3);
		}
	}


	/**
	 * Test of buildFault method, of class FaultServices.
	 */
	@Test
	public void testBuildFaultDebugMode() {
		System.out.println("buildFault");
		when(configuration.getStacktracelength()).thenReturn(3);
		when(logger.isDebugEnabled()).thenReturn(true);
		try {
			throw new Exception("ERROR_MESSAGE");
		} catch (Exception e) {
			Fault fault = instance.buildFault(e);

			ArgumentCaptor<String> captureLog = ArgumentCaptor.forClass(String.class);
			verify(logger).error(captureLog.capture(), any(Throwable.class));
			assertThat(captureLog.getValue()).isEqualTo("Invocation failed");

			assertThat(fault.getClassname()).isEqualTo("java.lang.Exception");
			assertThat(fault.getMessage()).isEqualTo("ERROR_MESSAGE");
			String[] stacktraces = fault.getStacktrace();
			assertThat(stacktraces).hasSize(3);
		}
	}

	/**
	 * Test of buildFault method, of class FaultServices.
	 */
	@Test
	public void testBuildFaultNoStackTrace() {
		System.out.println("buildFault");
		when(configuration.getStacktracelength()).thenReturn(0);
		try {
			throw new Exception("ERROR_MESSAGE");
		} catch (Exception e) {
			Fault fault = instance.buildFault(e);
			assertThat(fault.getClassname()).isEqualTo("java.lang.Exception");
			assertThat(fault.getMessage()).isEqualTo("ERROR_MESSAGE");
			String[] stacktraces = fault.getStacktrace();
			assertThat(stacktraces).isEmpty();
		}
	}
}