/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.security;

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
import org.ocelotds.security.impl.GlassfishSubjectServices;
import org.ocelotds.security.impl.WildflySubjectServices;
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
	 */
	@Test
	public void testGetSubject() {
		System.out.println("getSubject");
		ContainerSubjectServices subjectServices = mock(ContainerSubjectServices.class);
		Subject subject = new Subject();

		doReturn(subjectServices).when(instance).getContainerSubjectServices();
		when(subjectServices.getSubject()).thenReturn(subject);
		
		Subject result = instance.getSubject();
		
		assertThat(result).isEqualTo(subject);
	}

	/**
	 * Test of setSubject method, of class SubjectServices.
	 */
	@Test
	public void testSetSubject() {
		System.out.println("setSubject");
		ContainerSubjectServices subjectServices = mock(ContainerSubjectServices.class);
		Subject subject = new Subject();
		Principal principal = mock(Principal.class);

		doReturn(subjectServices).when(instance).getContainerSubjectServices();
		instance.setSubject(subject, principal);

		ArgumentCaptor<Subject> captureSubject = ArgumentCaptor.forClass(Subject.class);
		ArgumentCaptor<Principal> capturePrincipal = ArgumentCaptor.forClass(Principal.class);
		verify(subjectServices).setSubject(captureSubject.capture(), capturePrincipal.capture());

		assertThat(captureSubject.getValue()).isEqualTo(subject);
		assertThat(capturePrincipal.getValue()).isEqualTo(principal);
	}

}
