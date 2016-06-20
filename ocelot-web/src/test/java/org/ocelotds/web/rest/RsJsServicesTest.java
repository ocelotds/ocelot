/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web.rest;

import java.io.InputStream;
import java.util.List;
import javax.enterprise.inject.Instance;
import javax.ws.rs.core.UriInfo;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.OcelotServices;
import org.ocelotds.objects.FakeCDI;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class RsJsServicesTest {

	@InjectMocks
	@Spy
	RsJsServices instance;
	
	@Spy
	Instance<Object> dataservices = new FakeCDI();

	@Mock
	UriInfo context;

	/**
	 * Test of getStreams method, of class RsJsServices.
	 */
	@Test
	public void testGetStreamsNoFwk() {
		System.out.println("getStreams");
		((FakeCDI) dataservices).add("service1");
		((FakeCDI) dataservices).add("service2");
		((FakeCDI) dataservices).add(new OcelotServices());
		((FakeCDI) dataservices).add("service4");
		when(context.getPath()).thenReturn("");
		doReturn("srv1").when(instance).getClassnameFromProxy(eq("service1"));
		doReturn("srv2").when(instance).getClassnameFromProxy(eq("service2"));
		doReturn("srv4").when(instance).getClassnameFromProxy(eq("service4"));
		doReturn("").when(instance).getJsFilename(anyString(), anyString());
		List<InputStream> result = instance.getStreams();
		verify(instance, times(3)).addStream(any(List.class), anyString());
		verify(instance).getJsFilename(eq("srv1"), eq((String) null));
		verify(instance).getJsFilename(eq("srv2"), eq((String) null));
		verify(instance).getJsFilename(eq("srv4"), eq((String) null));

		assertThat(result).isNotNull();
	}

	/**
	 * Test of getStreams method, of class RsJsServices.
	 */
	@Test
	public void testGetStreamsNgFwk() {
		System.out.println("getStreams");
		((FakeCDI) dataservices).add("service");
		when(context.getPath()).thenReturn("services.ng.js");
		doReturn("srv").when(instance).getClassnameFromProxy(eq("service"));
		doReturn("").when(instance).getJsFilename(anyString(), anyString());
		List<InputStream> result = instance.getStreams();
		verify(instance).addStream(any(List.class), anyString());
		verify(instance).getJsFilename(eq("srv"), eq("ng"));
		assertThat(result).isNotNull();
	}

	/**
	 * Test of getClassnameFromProxy method, of class RsJsServices.
	 */
	@Test
	public void testGetClassnameFromProxy() {
		System.out.println("getClassnameFromProxy");
		Object dataservice = "string";
		String expectResult = dataservice.toString();
		doReturn(null).when(instance).getClassnameFromProxyname(anyString());
		instance.getClassnameFromProxy(dataservice);
		verify(instance).getClassnameFromProxyname(eq(expectResult));
	}

	/**
	 * Test of getClassnameFromProxyname method, of class RsJsServices.
	 */
	@Test
	public void testGetClassnameFromProxyname() {
		System.out.println("getClassnameFromProxyname");
		String result = instance.getClassnameFromProxyname("a.b.c.Class@abc");
		assertThat(result).isEqualTo("a.b.c.Class");
	}
	
}