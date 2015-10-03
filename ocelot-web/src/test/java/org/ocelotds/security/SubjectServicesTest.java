/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.security;

import org.ocelotds.security.containers.ContainerSubjectServices;
import java.security.Principal;
import javax.enterprise.inject.Instance;
import javax.security.auth.Subject;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.objects.FakeCDI;
import org.ocelotds.security.containers.GlassfishSubjectServices;
import org.ocelotds.security.containers.WildflySubjectServices;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class SubjectServicesTest {

	@Mock
	private Logger logger;

	@Spy
	private Instance<ContainerSubjectServices> instances = new FakeCDI<>();

	@Spy
	@InjectMocks
	private SubjectServices instance;
	
	/**
	 * Test of setServerInfo method, of class SubjectServices.
	 */
	@Test
	public void testSetServerInfo() {
		System.out.println("setServerInfo");
		GlassfishSubjectServices glassfishSubjectServices = new GlassfishSubjectServices();
		WildflySubjectServices wildflySubjectServices = new WildflySubjectServices();
		((FakeCDI<ContainerSubjectServices>) instances).add(glassfishSubjectServices);
		((FakeCDI<ContainerSubjectServices>) instances).add(wildflySubjectServices);
		
		instance.setServerInfo("Glassfish ...");
		ContainerSubjectServices result = instance.getContainerSubjectServices();
		assertThat(result).isEqualTo(glassfishSubjectServices);
		instance.setServerInfo("... WILDFLY ...");
		result = instance.getContainerSubjectServices();
		assertThat(result).isEqualTo(wildflySubjectServices);
	}

	/**
	 * Test of getSubject method, of class SubjectServices.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testGetSecurityContext() throws Exception {
		System.out.println("getSecurityContext");
		ContainerSubjectServices subjectServices = mock(ContainerSubjectServices.class);
		SecurityContext securityContext = mock(SecurityContext.class);

		doReturn(subjectServices).when(instance).getContainerSubjectServices();
		when(subjectServices.getSecurityContext()).thenReturn(securityContext);
		
		SecurityContext result = instance.getSecurityContext();
		
		assertThat(result).isEqualTo(securityContext);
	}

	/**
	 * Test of getSecurityContext method, of class SubjectServices.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testGetSecurityContextNull() throws Exception {
		System.out.println("getSecurityContext");
		ContainerSubjectServices subjectServices = mock(ContainerSubjectServices.class);

		doReturn(subjectServices).when(instance).getContainerSubjectServices();
		when(subjectServices.getSecurityContext()).thenThrow(Exception.class);
		
		SecurityContext result = instance.getSecurityContext();
		
		assertThat(result).isNull();
	}

	/**
	 * Test of setSecurityContext method, of class SubjectServices.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testSetSecurityContext() throws Exception {
		System.out.println("setSecurityContext");
		ContainerSubjectServices subjectServices = mock(ContainerSubjectServices.class);
		SecurityContext securityContext = mock(SecurityContext.class);

		doReturn(subjectServices).when(instance).getContainerSubjectServices();
		instance.setSecurityContext(securityContext);

		ArgumentCaptor<SecurityContext> captureSecurityContext = ArgumentCaptor.forClass(SecurityContext.class);
		verify(subjectServices).setSecurityContext(captureSecurityContext.capture());

		assertThat(captureSecurityContext.getValue()).isEqualTo(securityContext);
	}

	/**
	 * Test of setSecurityContext method, of class SubjectServices.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testNoSetSecurityContext() throws Exception {
		System.out.println("setSecurityContext");
		ContainerSubjectServices subjectServices = mock(ContainerSubjectServices.class);
		SecurityContext securityContext = mock(SecurityContext.class);

		doReturn(subjectServices).when(instance).getContainerSubjectServices();
		doThrow(Exception.class).when(subjectServices).setSecurityContext(securityContext);

		instance.setSecurityContext(securityContext);
	}
}
