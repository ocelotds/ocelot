/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.core.services;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.marshallers.LocaleMarshaller;
import org.ocelotds.marshalling.IJsonMarshaller;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;
import org.ocelotds.objects.BadMarshaller1;
import org.ocelotds.objects.BadMarshaller2;
import org.ocelotds.objects.BadMarshaller3;
import org.ocelotds.objects.Result;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class ArgumentServicesTest {

	@InjectMocks
	@Spy
	private ArgumentServices instance;

	/**
	 * Test of getJsonResultFromSpecificMarshaller method, of class ArgumentServices.
	 *
	 * @throws java.lang.Exception
	 */
	@Test
	public void testGetJsonResultFromSpecificMarshaller() throws Exception {
		System.out.println("getJsonResultFromSpecificMarshaller");
		JsonMarshaller jm = mock(JsonMarshaller.class);
		when(jm.value()).thenReturn((Class) LocaleMarshaller.class);
		when(jm.iterable()).thenReturn(false).thenReturn(true);
		doReturn("[LG1,LG2]").when(instance).getJsonResultFromSpecificMarshallerIterable(any(Iterable.class), any(IJsonMarshaller.class));

		String result = instance.getJsonResultFromSpecificMarshaller(jm, Locale.FRANCE);
		assertThat(result).isEqualTo("{\"country\":\"FR\",\"language\":\"fr\"}");
		result = instance.getJsonResultFromSpecificMarshaller(jm, Arrays.asList(Locale.FRANCE, Locale.US));
		assertThat(result).isEqualTo("[LG1,LG2]");
	}

	/**
	 * Test of getJsonResultFromSpecificMarshaller method, of class ArgumentServices.
	 *
	 * @throws java.lang.Exception
	 */
	@Test(expected = InstantiationException.class)
	public void testGetJsonResultFromSpecificMarshallerFail1() throws Exception {
		System.out.println("getJsonResultFromSpecificMarshaller");
		JsonMarshaller jm = mock(JsonMarshaller.class);
		when(jm.value()).thenReturn((Class) BadMarshaller1.class);
		instance.getJsonResultFromSpecificMarshaller(jm, Locale.FRANCE);
	}

	/**
	 * Test of getJsonResultFromSpecificMarshaller method, of class ArgumentServices.
	 *
	 * @throws java.lang.Exception
	 */
	@Test(expected = IllegalAccessException.class)
	public void testGetJsonResultFromSpecificMarshallerFail2() throws Exception {
		System.out.println("getJsonResultFromSpecificMarshaller");
		JsonMarshaller jm = mock(JsonMarshaller.class);
		when(jm.value()).thenReturn((Class) BadMarshaller2.class);
		instance.getJsonResultFromSpecificMarshaller(jm, Locale.FRANCE);
	}

	/**
	 * Test of getJsonResultFromSpecificMarshaller method, of class ArgumentServices.
	 *
	 * @throws java.lang.Exception
	 */
	@Test(expected = JsonMarshallingException.class)
	public void testGetJsonResultFromSpecificMarshallerFail3() throws Exception {
		System.out.println("getJsonResultFromSpecificMarshaller");
		JsonMarshaller jm = mock(JsonMarshaller.class);
		when(jm.value()).thenReturn((Class) BadMarshaller3.class);
		instance.getJsonResultFromSpecificMarshaller(jm, Locale.FRANCE);
	}

	/**
	 * Test of getJsonResultFromSpecificMarshaller method, of class ArgumentServices.
	 *
	 * @throws java.lang.Exception
	 */
	@Test
	public void testGetJsonResultFromSpecificMarshallerIterable() throws Exception {
		System.out.println("getJsonResultFromSpecificMarshallerIterable");
		IJsonMarshaller ijm = mock(IJsonMarshaller.class);
		when(ijm.toJson(anyObject())).thenReturn("\"JSON\"");
		List<Locale> locales = Arrays.asList(Locale.FRANCE, Locale.US);
		String result = instance.getJsonResultFromSpecificMarshallerIterable(locales, ijm);
		assertThat(result).isEqualTo("[\"JSON\",\"JSON\"]");
	}

	@Test
	public void testGetJavaResultFromSpecificUnmarshallerIterable() throws JsonUnmarshallingException {
		System.out.println("getJavaResultFromSpecificUnmarshallerIterable");
		LocaleMarshaller lm = new LocaleMarshaller();
		String json = "[{\"country\":\"FR\",\"language\":\"fr\"},{\"country\":\"FR\",\"language\":\"fr\"},{\"country\":\"FR\",\"language\":\"fr\"}]";
		List result = instance.getJavaResultFromSpecificUnmarshallerIterable(json, lm);
		assertThat(result).hasSize(3);
		for (Object object : result) {
			assertThat(object).isEqualTo(Locale.FRANCE);
		}
	}

	@Test(expected = JsonUnmarshallingException.class)
	public void testGetJavaResultFromSpecificUnmarshallerIterableFail() throws JsonUnmarshallingException {
		System.out.println("getJavaResultFromSpecificUnmarshallerIterable");
		LocaleMarshaller lm = new LocaleMarshaller();
		String json = "BADJSON";
		instance.getJavaResultFromSpecificUnmarshallerIterable(json, lm);
	}

	@Test
	public void testCheckTypeClass() throws JsonUnmarshallingException {
		System.out.println("checkType");
		doNothing().when(instance).checkClass(anyObject(), any(Class.class));
		instance.checkType(mock(Result.class), (Type) Result.class);
		verify(instance).checkClass(anyObject(), any(Class.class));
	}

	@Test
	public void testCheckTypeParamType() throws JsonUnmarshallingException {
		System.out.println("checkType");
		Method[] methods = this.getClass().getDeclaredMethods();
		Type type = null;
		for (Method method : methods) {
			if (method.getName().equals("method")) {
				type = method.getGenericReturnType();
			}
		}
		doNothing().when(instance).checkClass(anyObject(), any(Class.class));
		instance.checkType(mock(List.class), type);
		verify(instance).checkClass(anyObject(), any(Class.class));
	}

	@Test(expected = JsonUnmarshallingException.class)
	public void testCheckTypeFail() throws JsonUnmarshallingException {
		System.out.println("checkType");
		WildcardType obj = mock(WildcardType.class);
		instance.checkType(obj, (Type) String.class);
	}

	@Test
	public void testCheckClass() throws JsonUnmarshallingException {
		System.out.println("checkClass");
		instance.checkClass(mock(Result.class), Result.class);
	}
	
	@Test(expected = JsonUnmarshallingException.class)
	public void testCheckClassFail() throws JsonUnmarshallingException {
		System.out.println("checkClass");
		instance.checkClass(mock(Result.class), String.class);
	}

	private List<Result> method() {
		return null;
	}
}
