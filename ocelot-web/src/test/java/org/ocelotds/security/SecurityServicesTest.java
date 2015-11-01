/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.security;

import org.ocelotds.spi.security.SecurityContext;
import org.ocelotds.spi.security.ContainerSecurityServices;
import javax.enterprise.inject.Instance;
import javax.servlet.ServletContext;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.ocelotds.annotations.ContainerQualifier;
import org.ocelotds.objects.FakeCDI;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityServicesTest {

	@Mock
	private Logger logger;

	@Mock
	private ContainerSecurityServices current;

	@Spy
	private Instance<ContainerSecurityServices> instances = new FakeCDI<>();

	@Spy
	@InjectMocks
	private SecurityServices instance;
	
	/**
	 * Test of setSecurityContainerService method, of class SecurityServices.
	 */
	@Test
	public void testSetSecurityContainerService() {
		System.out.println("setSecurityContainerService");
		TestContainerSecurity testSubjectServices = new TestContainerSecurity();
		((FakeCDI<ContainerSecurityServices>) instances).add(testSubjectServices);
		ServletContext sc	= mock(ServletContext.class);
		
		when(sc.getServerInfo()).thenReturn("... UNKNOWNSERVER ...").thenReturn("test ...");

		ContainerSecurityServices result;
		instance.setSecurityContainerService(sc);
		result = instance.getContainerSubjectServices();
		assertThat(result).isEqualTo(current);

		instance.setSecurityContainerService(sc);
		result = instance.getContainerSubjectServices();
		assertThat(result).isEqualTo(testSubjectServices);
	}

	/**
	 * Test of setSecurityContainerService method, of class SecurityServices.
	 */
	@Test
	public void testSetSecurityContainerService2() {
		System.out.println("setSecurityContainerService");
		TestContainerSecurity testSubjectServices = new TestContainerSecurity();
		((FakeCDI<ContainerSecurityServices>) instances).add(testSubjectServices);
		ServletContext sc	= mock(ServletContext.class);
		
		when(sc.getInitParameter(Constants.Options.SECUREKEY)).thenReturn("").thenReturn("test ...");
		when(sc.getServerInfo()).thenReturn("... UNKNOWNSERVER ...");

		ContainerSecurityServices result;
		instance.setSecurityContainerService(sc);
		result = instance.getContainerSubjectServices();
		assertThat(result).isEqualTo(current);

		instance.setSecurityContainerService(sc);
		result = instance.getContainerSubjectServices();
		assertThat(result).isEqualTo(testSubjectServices);
	}

	/**
	 * Test of getSubject method, of class SecurityServices.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testGetSecurityContext() throws Exception {
		System.out.println("getSecurityContext");
		ContainerSecurityServices subjectServices = mock(ContainerSecurityServices.class);
		SecurityContext securityContext = mock(SecurityContext.class);

		doReturn(subjectServices).when(instance).getContainerSubjectServices();
		when(subjectServices.getSecurityContext()).thenReturn(securityContext).thenThrow(Exception.class);
		
		SecurityContext result = instance.getSecurityContext();
		assertThat(result).isEqualTo(securityContext);
		
		result = instance.getSecurityContext();
		assertThat(result).isNull();
	}

	/**
	 * Test of getSecurityContext method, of class SecurityServices.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testGetSecurityContextNull() throws Exception {
		System.out.println("getSecurityContext");
		ContainerSecurityServices subjectServices = mock(ContainerSecurityServices.class);

		doReturn(subjectServices).when(instance).getContainerSubjectServices();
		when(subjectServices.getSecurityContext()).thenThrow(Exception.class);
		
		SecurityContext result = instance.getSecurityContext();
		
		assertThat(result).isNull();
	}

	/**
	 * Test of setSecurityContext method, of class SecurityServices.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testSetSecurityContext() throws Exception {
		System.out.println("setSecurityContext");
		ContainerSecurityServices subjectServices = mock(ContainerSecurityServices.class);
		SecurityContext securityContext = mock(SecurityContext.class);

		doReturn(subjectServices).when(instance).getContainerSubjectServices();
		instance.setSecurityContext(securityContext);

		ArgumentCaptor<SecurityContext> captureSecurityContext = ArgumentCaptor.forClass(SecurityContext.class);
		verify(subjectServices).setSecurityContext(captureSecurityContext.capture());

		assertThat(captureSecurityContext.getValue()).isEqualTo(securityContext);
	}

	/**
	 * Test of setSecurityContext method, of class SecurityServices.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testNoSetSecurityContext() throws Exception {
		System.out.println("setSecurityContext");
		ContainerSecurityServices subjectServices = mock(ContainerSecurityServices.class);
		SecurityContext securityContext = mock(SecurityContext.class);

		doReturn(subjectServices).when(instance).getContainerSubjectServices();
		doThrow(Exception.class).when(subjectServices).setSecurityContext(securityContext);

		instance.setSecurityContext(securityContext);
	}
	
	@ContainerQualifier("TEST")
	static class TestContainerSecurity implements ContainerSecurityServices {

		@Override
		public SecurityContext getSecurityContext() {
			return null;
		}

		@Override
		public void setSecurityContext(SecurityContext securityContext) {
		}
		
	}
}
