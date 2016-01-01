/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.marshallers;

import java.util.Locale;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class LocaleUnmarshallerTest {

	@InjectMocks
	private LocaleUnmarshaller instance;

	/**
	 * Test of toJava method, of class LocaleUnmarshaller.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testToJava() throws Exception {
		System.out.println("toJava");
		String json = "{\"language\":\"fr\",\"country\":\"FR\"}";
		Locale expResult = new Locale("FR", "fr");
		Locale result = instance.toJava(json);
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of toJava method, of class LocaleUnmarshaller.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testToNullJava() throws Exception {
		System.out.println("toJava");
		Locale result = instance.toJava(null);
		assertThat(result).isNull();
	}

	/**
	 * Test of toJava method, of class LocaleUnmarshaller.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testToJavaFail() throws Exception {
		System.out.println("toJava");
		String json = "{\"language\":\"fr\"}";
		Locale result = instance.toJava(json);
		assertThat(result).isNull();
	}
}