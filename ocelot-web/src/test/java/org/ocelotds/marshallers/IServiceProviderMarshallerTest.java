/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.marshallers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.enterprise.inject.Instance;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.IServicesProvider;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.ocelotds.objects.FakeCDI;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class IServiceProviderMarshallerTest {

	
	@InjectMocks
	@Spy
	private IServiceProviderMarshaller instance;

	@Spy
	private Instance<IServicesProvider> jsonServicesProviders = new FakeCDI<>();

	/**
	 * Test of toJson method, of class LocaleMarshaller.
	 *
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 */
	@Test
	public void testToJson() throws JsonMarshallingException {
		System.out.println("toJson");
		IServicesProvider provider0 = mock(IServicesProvider.class);
		((FakeCDI) jsonServicesProviders).add(provider0);
		doReturn(true).doReturn(true).doReturn(false).when(provider0).streamJavascriptServices(any(OutputStream.class));
		String result = instance.toJson(jsonServicesProviders);
		assertThat(result).isEqualTo("[]");

		IServicesProvider provider1 = mock(IServicesProvider.class);
		((FakeCDI) jsonServicesProviders).add(provider1);
		doReturn(true).when(provider1).streamJavascriptServices(any(OutputStream.class));
		result = instance.toJson(jsonServicesProviders);
		assertThat(result).isEqualTo("[,\n]");

		result = instance.toJson(jsonServicesProviders);
		assertThat(result).isEqualTo("[]");
	}

	/**
	 * Test of toJson method, of class LocaleMarshaller.
	 *
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 */
	@Test
	public void testToJsonFail() throws JsonMarshallingException {
		System.out.println("toJson");
		IServicesProvider provider0 = mock(IServicesProvider.class);
		((FakeCDI) jsonServicesProviders).add(provider0);
		ByteArrayOutputStream out = mock(ByteArrayOutputStream.class);
		doThrow(IOException.class).when(out).write(anyByte());
		
		String result = instance.toJson(jsonServicesProviders);
		assertThat(result).isEqualTo("[]");
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
		assertThat(result).isEqualTo("[]");
	}

}
