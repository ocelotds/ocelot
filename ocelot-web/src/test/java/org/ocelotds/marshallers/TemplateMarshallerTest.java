/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.marshallers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;

/**
 *
 * @author hhfrancois
 */

@RunWith(MockitoJUnitRunner.class)
public class TemplateMarshallerTest {
	
	@InjectMocks
	private TemplateMarshaller instance;
	
	@Mock
	ObjectMapper objectMapper;


	/**
	 * Test of toJson method, of class TemplateMarshaller.
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 * @throws com.fasterxml.jackson.core.JsonProcessingException
	 */
	@Test
	public void testToJson() throws JsonMarshallingException, JsonProcessingException {
		System.out.println("toJson");
		String obj = "in";
		String expResult = "out";
		when(objectMapper.writeValueAsString(eq(obj))).thenReturn(expResult);
		String result = instance.toJson(obj);
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of toJson method, of class TemplateMarshaller.
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 * @throws com.fasterxml.jackson.core.JsonProcessingException
	 */
	@Test(expected = JsonMarshallingException.class)
	public void testToJsonFail() throws JsonMarshallingException, JsonProcessingException {
		System.out.println("toJson");
		String obj = "in";
		when(objectMapper.writeValueAsString(eq(obj))).thenThrow(JsonProcessingException.class);
		instance.toJson(obj);
	}

	/**
	 * Test of toJava method, of class TemplateMarshaller.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testToJava() throws Exception {
		System.out.println("toJava");
		String json = "in";
		Object expResult = null;
		Object result = instance.toJava(json);
		assertThat(result).isEqualTo(expResult);
	}
	
}
