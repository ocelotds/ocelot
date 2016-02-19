/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.marshallers;

import java.util.Locale;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class LocaleMarshallerTest {

	@InjectMocks
	@Spy
	private LocaleMarshaller instance;

	/**
	 * Test of toJson method, of class LocaleMarshaller.
	 *
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 */
	@Test
	public void testToJson() throws JsonMarshallingException {
		System.out.println("toJson");
		String expResult = "{\"country\":\"FR\",\"language\":\"fr\"}";
		String result = instance.toJson(Locale.FRANCE);
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of toJson method, of class LocaleMarshaller.
	 *
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 */
	@Test
	public void testToJsonNull() throws JsonMarshallingException {
		System.out.println("toJson");
		String result = instance.toJson(null);
		assertThat(result).isEqualTo("null");
	}

	/**
	 * Test of toJava method, of class LocaleUnmarshaller.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testToJava() throws Exception {
		System.out.println("toJava");
		String json = "{\"language\":\"fr\",\"country\":\"FR\"}";
		Locale result = instance.toJava(json);
		assertThat(result).isEqualTo(Locale.FRANCE);
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
