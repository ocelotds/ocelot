/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.marshalling;

import org.ocelotds.marshalling.exceptions.JsonMarshallerException;
import javax.enterprise.inject.Instance;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.marshallers.LocaleMarshaller;
import org.ocelotds.objects.FakeCDI;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonMarshallerServicesTest {

	@InjectMocks
	@Spy
	JsonMarshallerServices instance;

	@Spy
	Instance<IJsonMarshaller> iJsonMarshallers = new FakeCDI();

	/**
	 * Test of getIJsonMarshallerInstance method, of class JsonMarshallerServices.
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 */
	@Test
	public void testGetIJsonMarshallerInstance() throws JsonMarshallerException {
		System.out.println("getIJsonMarshallerInstance");
		FakeCDI.class.cast(iJsonMarshallers).add(new LocaleMarshaller());
		IJsonMarshaller result = instance.getIJsonMarshallerInstance(LocaleMarshaller.class);
		assertThat(result).isInstanceOf(LocaleMarshaller.class);
	}

	/**
	 * Test of getIJsonMarshallerInstance method, of class JsonMarshallerServices.
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 */
	@Test
	public void testGetIJsonMarshallerInstanceFromNewInstance() throws JsonMarshallerException {
		System.out.println("getIJsonMarshallerInstance");
		FakeCDI.class.cast(iJsonMarshallers).clear();
		IJsonMarshaller result = instance.getIJsonMarshallerInstance(LocaleMarshaller.class);
		assertThat(result).isInstanceOf(LocaleMarshaller.class);
	}

//	/**
//	 * Test of getIJsonMarshallerInstance method, of class JsonMarshallerServices.
//	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
//	 */
//	@Test(expected = JsonMarshallerException.class)
//	public void testGetIJsonMarshallerInstanceFail() throws JsonMarshallerException {
//		System.out.println("getIJsonMarshallerInstance");
//		FakeCDI.class.cast(iJsonMarshallers).clear();
//		instance.getIJsonMarshallerInstance(LocaleMarshaller.class);
//	}
}