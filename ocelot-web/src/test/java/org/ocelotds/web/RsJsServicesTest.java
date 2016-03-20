/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ocelotds.web;

import java.io.InputStream;
import java.util.List;
import javax.enterprise.inject.Instance;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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
	Instance<Object> dataservices = new FakeCDI<Object>();

	/**
	 * Test of getStreams method, of class RsJsServices.
	 */
	@Test
	public void testGetStreams() {
		System.out.println("getStreams");
		((FakeCDI) dataservices).add("service1");
		((FakeCDI) dataservices).add("service2");
		((FakeCDI) dataservices).add(new OcelotServices());
		((FakeCDI) dataservices).add("service4");
		doReturn("").when(instance).getClassnameFromProxy(anyObject());
		doReturn("").when(instance).getJsFilename(anyString());
		List<InputStream> result = instance.getStreams();
		verify(instance, times(3)).addStream(any(List.class), anyString());
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