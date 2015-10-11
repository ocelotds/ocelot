/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.logger;

import java.lang.reflect.Member;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.annotations.OcelotLogger;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class LoggerProducerTest {

	@Mock
	private Logger logger;

	@Mock
	InjectionPoint injectionPoint;

	@InjectMocks
	private LoggerProducer instance;

	/**
	 * Test of getLogger method, of class LoggerProducer.
	 */
	@Test
	public void testGetLogger() {
		System.out.println("getLogger");
		String expResult = LoggerProducerTest.class.getName();
		Member member = mock(Member.class);
		Annotated annotated = mock(Annotated.class);
		Class cls = LoggerProducerTest.class;
		OcelotLogger ol = mock(OcelotLogger.class);

		when(member.getDeclaringClass()).thenReturn(cls);
		when(injectionPoint.getMember()).thenReturn(member);
		when(injectionPoint.getAnnotated()).thenReturn(annotated);
		when(annotated.getAnnotation(eq(OcelotLogger.class))).thenReturn(ol);
		when(ol.name()).thenReturn("").thenReturn("SECURITY");

		Logger result = instance.getLogger(injectionPoint);
		assertThat(result.getName()).isEqualTo(expResult);
		
		result = instance.getLogger(injectionPoint);
		assertThat(result.getName()).isEqualTo("SECURITY");
}

}
