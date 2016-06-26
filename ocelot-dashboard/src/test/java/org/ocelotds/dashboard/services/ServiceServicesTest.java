/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.dashboard.services;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import javax.enterprise.inject.Instance;
import javax.servlet.http.HttpSession;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.ocelotds.core.UnProxyClassServices;
import org.ocelotds.dashboard.objects.FakeCDI;
import org.ocelotds.marshalling.exceptions.JsonMarshallerException;
import org.ocelotds.marshalling.IJsonMarshaller;
import org.ocelotds.dashboard.objects.OcelotMethod;
import org.ocelotds.dashboard.objects.OcelotService;
import org.ocelotds.objects.Options;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceServicesTest {

	@Mock
	private Logger logger;

	@Mock
	private ServiceTools serviceTools;

	@Mock
	private UnProxyClassServices unProxyClassServices;

	@Spy
	private Instance<Object> dataservices = new FakeCDI();

	@InjectMocks
	@Spy
	ServiceServices instance;

	@Test
	public void testGetServices() {
		System.out.println("getServices");
		((FakeCDI)dataservices).add(new ClassAsDataService());
		HttpSession httpSession = mock(HttpSession.class);
		when(unProxyClassServices.getRealClass(any(Class.class))).thenReturn((Class) ClassAsDataService.class);
		when(serviceTools.getInstanceNameFromDataservice(any(Class.class))).thenReturn("ClassAsDataService");
		doNothing().when(instance).addMethodsToMethodsService(any(Method[].class), any(List.class));
		when(httpSession.getAttribute(Constants.Options.OPTIONS)).thenReturn(new Options());
		List<OcelotService> services = instance.getServices(httpSession);
		assertThat(services).hasSize(1);
	}
	
	/**
	 * Test of addMethodsToMethodsService method, of class OcelotServices.
	 */
	@Test
	public void testAddMethodsToMethodsService() {
		System.out.println("addMethodsToMethodsService");
		Method[] methods = ClassAsDataService.class.getDeclaredMethods();
		List<OcelotMethod> methodsService = mock(List.class);
		when(serviceTools.isConsiderateMethod(any(Method.class))).thenReturn(Boolean.FALSE).thenReturn(Boolean.TRUE).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE);
		doReturn(mock(OcelotMethod.class)).when(instance).getOcelotMethod(any(Method.class));
		instance.addMethodsToMethodsService(methods, methodsService);
		verify(methodsService, times(2)).add(any(OcelotMethod.class));
	}

	@Test
	public void testGetOcelotMethod0Arg() throws NoSuchMethodException {
		System.out.println("getOcelotMethod");
		Method method = this.getClass().getDeclaredMethod("methodWith0Arg");
		when(serviceTools.getShortName(anyString())).thenReturn("returntype");
		OcelotMethod result = instance.getOcelotMethod(method);
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("methodWith0Arg");
		assertThat(result.getReturntype()).isEqualTo("returntype");
		assertThat(result.getArgtypes()).isEmpty();
		assertThat(result.getArgnames()).isEmpty();
		assertThat(result.getArgtemplates()).isEmpty();
	}
	
	@Test
	public void testGetOcelotMethod2Args() throws NoSuchMethodException, JsonMarshallerException {
		System.out.println("getOcelotMethod");
		Method method = this.getClass().getDeclaredMethod("methodWith2Args", String.class, String.class);
		when(serviceTools.getShortName(anyString())).thenReturn("returntype").thenReturn("argtype");
		when(serviceTools.getTemplateOfType(any(Type.class), any(IJsonMarshaller.class))).thenReturn("template");
		OcelotMethod result = instance.getOcelotMethod(method);
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("methodWith2Args");
		assertThat(result.getReturntype()).isEqualTo("returntype");
		assertThat(result.getArgtypes()).hasSize(2);
		assertThat(result.getArgnames()).hasSize(2);
		assertThat(result.getArgtemplates()).hasSize(2);
	}

	@Test
	public void testGetOcelotMethod2ArgsWithOneUnmarshalled() throws NoSuchMethodException, JsonMarshallerException {
		System.out.println("getOcelotMethod");
		Method method = this.getClass().getDeclaredMethod("methodWith2Args", String.class, String.class);
		when(serviceTools.getShortName(anyString())).thenReturn("returntype").thenReturn("argtype");
		when(serviceTools.getTemplateOfType(any(Type.class), any(IJsonMarshaller.class))).thenReturn("template").thenThrow(JsonMarshallerException.class);
		OcelotMethod result = instance.getOcelotMethod(method);
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("methodWith2Args");
		assertThat(result.getReturntype()).isEqualTo("returntype");
		assertThat(result.getArgtypes()).hasSize(2);
		assertThat(result.getArgnames()).hasSize(2);
		assertThat(result.getArgtemplates()).hasSize(2);
		assertThat(result.getArgtemplates().get(1)).isEqualTo("java.lang.String");
	}

	private void methodWith0Arg() {
		
	}
	private void methodWith2Args(String a, String b) {
		
	}

}