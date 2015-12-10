/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.core.services;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.core.Cleaner;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class ArgCleanerDecoratorTest {

	@Mock
	private Logger logger;
	
	@Mock
	IArgumentConvertor argumentConvertor;
	
	@Mock
	Cleaner cleaner;

	
	@InjectMocks
	private ArgCleanerDecorator instance = new ArgCleanerDecoratorImpl();

	/**
	 * Test of convertJsonToJava method, of class ArgCleanerDecorator.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testConvertJsonToJava() throws Exception {
		System.out.println("convertJsonToJava");
		when(argumentConvertor.convertJsonToJava(anyString(), any(Type.class), any(Annotation[].class))).thenReturn("OK");
		when(cleaner.cleanArg(anyString())).thenReturn(null);
		Object result = instance.convertJsonToJava("", null, null);
		assertThat(result).isEqualTo("OK");
	}

	public class ArgCleanerDecoratorImpl extends ArgCleanerDecorator {
	}

}