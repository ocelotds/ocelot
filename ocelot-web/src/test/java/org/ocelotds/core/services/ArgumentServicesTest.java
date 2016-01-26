/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.core.services;

import java.util.Locale;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.literals.JsonMarshallerLiteral;
import org.ocelotds.marshallers.LocaleMarshaller;
import org.ocelotds.marshalling.annotations.JsonMarshaller;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class ArgumentServicesTest {
	
	@InjectMocks
	private ArgumentServices instance;

	/**
	 * Test of getJsonResultFromSpecificMarshaller method, of class ArgumentServices.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testGetJsonResultFromSpecificMarshaller() throws Exception {
		System.out.println("getJsonResultFromSpecificMarshaller");
		String result = instance.getJsonResultFromSpecificMarshaller(new JsonMarshallerLiteral(LocaleMarshaller.class), Locale.FRANCE);
		assertThat(result).isEqualTo("{\"country\":\"FR\",\"language\":\"fr\"}");
	}
	
}
