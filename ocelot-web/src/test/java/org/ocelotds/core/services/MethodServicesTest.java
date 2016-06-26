/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.services;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.marshalling.exceptions.JsonMarshallerException;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.objects.Result;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class MethodServicesTest {

	@Mock
	private Logger logger;

	@InjectMocks
	private MethodServices instance;

	@Mock
	private ArgumentConvertor argumentsServices;

	/**
	 * Test of getMethodFromDataService method, of class MethodServices.
	 *
	 * @throws java.lang.NoSuchMethodException
	 * @throws org.ocelotds.marshalling.exceptions.JsonUnmarshallingException
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 */
	@Test
	public void testGetMethodFromDataService() throws NoSuchMethodException, JsonUnmarshallingException, JsonMarshallerException {
		System.out.println("getMethodFromDataService");
		Class dsClass = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setOperation("methodWith2Arguments");
		message.setParameters(Arrays.asList("5", "\"toto\""));
		List<Object> arguments = new ArrayList<>();
		Method expResult = dsClass.getMethod("methodWith2Arguments", new Class<?>[]{Integer.class, String.class});

		when(argumentsServices.convertJsonToJava(eq("5"), any(Type.class), any(Annotation[].class))).thenReturn(5);
		when(argumentsServices.convertJsonToJava(eq("\"toto\""), any(Type.class), any(Annotation[].class))).thenReturn("toto");

		Method result = instance.getMethodFromDataService(dsClass, message, arguments);
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getMethodFromDataService method, of class MethodServices.
	 *
	 * @throws java.lang.NoSuchMethodException
	 * @throws org.ocelotds.marshalling.exceptions.JsonUnmarshallingException
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 */
	@Test(expected = NoSuchMethodException.class)
	public void testGetMethodFromDataServiceNotFound() throws NoSuchMethodException, JsonUnmarshallingException, JsonMarshallerException {
		System.out.println("getMethodFromDataService");
		Class dsClass = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setOperation("methodWith2Arguments");
		message.setParameters(Arrays.asList("\"toto\"", "5"));
		List<Object> arguments = new ArrayList<>();

		when(argumentsServices.convertJsonToJava(anyString(), any(Type.class), any(Annotation[].class))).thenThrow(JsonUnmarshallingException.class);

		instance.getMethodFromDataService(dsClass, message, arguments);
	}

	/**
	 * Test of getMethodFromDataService method, of class MethodServices.
	 *
	 * @throws java.lang.NoSuchMethodException
	 * @throws org.ocelotds.marshalling.exceptions.JsonUnmarshallingException
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 */
	@Test
	public void testGetMethodFromDataServiceWithWithUnmarshaller() throws NoSuchMethodException, JsonUnmarshallingException, JsonMarshallerException {
		System.out.println("getMethodFromDataService");
		Class dsClass = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setOperation("methodWithUnmarshaller");
		String json = "{\"language\":\"fr\",\"country\":\"FR\"}";
		message.setParameters(Arrays.asList(json));
		List<Object> arguments = new ArrayList<>();
		Method expResult = dsClass.getMethod("methodWithUnmarshaller", new Class<?>[]{Locale.class});

		when(argumentsServices.convertJsonToJava(eq(json), any(Type.class), any(Annotation[].class))).thenReturn(Locale.FRANCE);

		Method result = instance.getMethodFromDataService(dsClass, message, arguments);
		assertThat(result).isEqualTo(expResult);
		assertThat(arguments).hasSize(1);
		Locale l = (Locale) arguments.get(0);
		assertThat(l.getCountry()).isEqualTo("FR");
		assertThat(l.getLanguage()).isEqualTo("fr");
	}

	/**
	 * Test of getNonProxiedMethod method, of class MethodServices.
	 *
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testGetNonProxiedMethod() throws NoSuchMethodException {
		System.out.println("getNonProxiedMethod");
		Method m = instance.getNonProxiedMethod(Result.class, "getInteger");
		assertThat(m).isNotNull();

		m = instance.getNonProxiedMethod(Result.class, "setInteger", Integer.TYPE);
		assertThat(m).isNotNull();
	}

	/**
	 * Test of getNonProxiedMethod method, of class MethodServices.
	 *
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test(expected = NoSuchMethodException.class)
	public void testGetNonProxiedMethodFailed() throws NoSuchMethodException {
		System.out.println("getNonProxiedMethod");
		Method m = instance.getNonProxiedMethod(Result.class, "unknownMethod");
	}

	/**
	 * Test of getNumberOfNullEnderParameter method, of class MethodServices.
	 *
	 */
	@Test
	public void testGetNumberOfNullEnderParameter() {
		System.out.println("getNumberOfNullEnderParameter");
		List<String> params = Arrays.asList("", "null", "", "null", "null");
		int result = instance.getNumberOfNullEnderParameter(params);
		assertThat(result).isEqualTo(2);

		params = Arrays.asList("", "null", "", "null");
		result = instance.getNumberOfNullEnderParameter(params);
		assertThat(result).isEqualTo(1);

		params = Arrays.asList("", "null", "");
		result = instance.getNumberOfNullEnderParameter(params);
		assertThat(result).isEqualTo(0);
	}

	/**
	 * Test of checkMethod method, of class MethodServices.
	 *
	 * @throws java.lang.NoSuchMethodException
	 * @throws org.ocelotds.marshalling.exceptions.JsonUnmarshallingException
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 */
	@Test
	public void testCheckMethod() throws NoSuchMethodException, JsonUnmarshallingException, JsonMarshallerException {
		String methodname = "methodWith2Arguments";
		Class dsClass = ClassAsDataService.class;
		Method method = dsClass.getMethod(methodname, Integer.class, String.class);
		List<Object> arguments = new ArrayList<>();
		List<String> parameters = Arrays.asList("5", "\"foo\"");
		int nbparam = 2;

		when(argumentsServices.convertJsonToJava(anyString(), any(Class.class), any(Annotation[].class))).thenReturn("ok");

		instance.checkMethod(method, arguments, parameters, nbparam);
		assertThat(arguments).hasSize(2);
	}

	/**
	 * Test of checkMethod method, of class MethodServices.
	 *
	 * @throws java.lang.NoSuchMethodException
	 * @throws org.ocelotds.marshalling.exceptions.JsonUnmarshallingException
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testCheckMethodFail() throws NoSuchMethodException, JsonUnmarshallingException, JsonMarshallerException {
		String methodname = "methodWith2Arguments";
		Class dsClass = ClassAsDataService.class;
		Method method = dsClass.getMethod(methodname, Integer.class, String.class);
		List<Object> arguments = new ArrayList<>();
		List<String> parameters = Arrays.asList("5", "\"foo\"");
		int nbparam = 1;

		when(argumentsServices.convertJsonToJava(anyString(), any(Class.class), any(Annotation[].class))).thenReturn("ok");

		instance.checkMethod(method, arguments, parameters, nbparam);
	}
}
