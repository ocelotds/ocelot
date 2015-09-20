/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.marshallers;

import java.util.Locale;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;

/**
 *
 * @author hhfrancois
 */
public class LocaleMarshallerTest {

	private LocaleMarshaller instance = new LocaleMarshaller();

	/**
	 * Test of toJson method, of class LocaleMarshaller.
	 *
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 */
	@Test
	public void testToJson() throws JsonMarshallingException {
		System.out.println("toJson");
		Locale obj = new Locale("fr", "FR");
		String expResult = "{\"country\":\"FR\",\"language\":\"fr\"}";
		String result = instance.toJson(obj);
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

}
