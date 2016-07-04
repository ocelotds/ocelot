/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.marshalling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import org.ocelotds.marshalling.exceptions.JsonMarshallerException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.marshallers.LocaleMarshaller;
import org.ocelotds.marshalling.IJsonMarshaller;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.marshalling.annotations.JsonMarshallerType;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;
import org.ocelotds.objects.BadMarshaller1;
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
	
	@Mock
	ObjectMapper objectMapper;

	
	/**
	 * Test of getJsonParameters method, of class.
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 * @throws com.fasterxml.jackson.core.JsonProcessingException
	 */
	@Test
	public void getJsonParametersWithMarshallerTest() throws JsonMarshallingException, JsonMarshallerException, JsonProcessingException {
		System.out.println("getJsonParameters");
		Serializable s1 = mock(Serializable.class);
		Annotation[] annotations = new Annotation[] {};
		Annotation[][] annotationss = new Annotation[][] {annotations, annotations};
		JsonMarshaller marshaller = mock(JsonMarshaller.class);
		doReturn(marshaller).when(instance).getJsonMarshaller(any(Annotation[].class));
		doReturn("FOO").when(instance).getJsonResultFromSpecificMarshaller(any(JsonMarshaller.class), anyObject());
		when(objectMapper.writeValueAsString(anyObject())).thenReturn("FOO");

		List<String> result = instance.getJsonParameters(new Object[] {s1, s1}, annotationss);

		assertThat(result).hasSize(2);
		verify(instance, times(2)).getJsonResultFromSpecificMarshaller(any(JsonMarshaller.class), anyObject());
		verify(objectMapper, never()).writeValueAsString(anyObject());
	}
	
	/**
	 * Test of getJsonParameters method, of class.
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 * @throws com.fasterxml.jackson.core.JsonProcessingException
	 */
	@Test
	public void getJsonParametersWithoutMarshallerTest() throws JsonMarshallingException, JsonMarshallerException, JsonProcessingException {
		System.out.println("getJsonParameters");
		Serializable s1 = mock(Serializable.class);
		Annotation[] annotations = new Annotation[] {};
		Annotation[][] annotationss = new Annotation[][] {annotations, annotations};
		doReturn(null).when(instance).getJsonMarshaller(any(Annotation[].class));
		doReturn("FOO").when(instance).getJsonResultFromSpecificMarshaller(any(JsonMarshaller.class), anyObject());
		when(objectMapper.writeValueAsString(anyObject())).thenReturn("FOO");

		List<String> result = instance.getJsonParameters(new Object[] {s1, s1}, annotationss);

		assertThat(result).hasSize(2);
		verify(instance, never()).getJsonResultFromSpecificMarshaller(any(JsonMarshaller.class), anyObject());
		verify(objectMapper, times(2)).writeValueAsString(anyObject());
	}
	
	/**
	 * Test of getJsonMarshaller method, of class.
	 */
	@Test
	public void getJsonMarshallerTest() {
		System.out.println("getJsonMarshaller");
		JsonMarshaller a1 = mock(JsonMarshaller.class);
		when(a1.annotationType()).thenReturn((Class) JsonMarshaller.class);
		Annotation a2 = mock(Annotation.class);
		when(a2.annotationType()).thenReturn((Class) String.class);
		Annotation[] annotations = new Annotation[] {a2, a1};
		JsonMarshaller result = instance.getJsonMarshaller(new Annotation[] {});
		assertThat(result).isNull();
		result = instance.getJsonMarshaller(annotations);
		assertThat(result).isNotNull();
	}

	/**
	 * Test of getJsonResultFromSpecificMarshaller method, of class ArgumentServices.
	 *
	 * @throws java.lang.Exception
	 */
	@Test
	public void testGetJsonResultFromSpecificMarshaller() throws Exception {
		System.out.println("getJsonResultFromSpecificMarshaller");
		IJsonMarshaller ijm = mock(IJsonMarshaller.class);
		JsonMarshaller jm = mock(JsonMarshaller.class);

		when(jm.value()).thenReturn((Class) LocaleMarshaller.class);
		when(jm.type()).thenReturn(JsonMarshallerType.SINGLE).thenReturn(JsonMarshallerType.LIST).thenReturn(JsonMarshallerType.MAP);
		when(ijm.toJson(any(Object.class))).thenReturn("LG1");
		doReturn("[LG1,LG2]").when(instance).getJsonResultFromSpecificMarshallerIterable(any(Iterable.class), any(IJsonMarshaller.class));
		doReturn("{\"FR\":LG1,\"US\":LG2}").when(instance).getJsonResultFromSpecificMarshallerMap(any(Map.class), any(IJsonMarshaller.class));
		
		doReturn(ijm).when(instance).getIJsonMarshallerInstance(any(Class.class));
		String result = instance.getJsonResultFromSpecificMarshaller(jm, Locale.FRANCE);
		assertThat(result).isEqualTo("LG1");
		result = instance.getJsonResultFromSpecificMarshaller(jm, Arrays.asList(Locale.FRANCE, Locale.US));
		assertThat(result).isEqualTo("[LG1,LG2]");
		result = instance.getJsonResultFromSpecificMarshaller(jm, getMapOfLocale());
		assertThat(result).isEqualTo("{\"FR\":LG1,\"US\":LG2}");
	}

	/**
	 * Test of getJsonResultFromSpecificMarshaller method, of class ArgumentServices.
	 *
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 */
	@Test(expected = JsonMarshallerException.class)
	public void testGetJsonResultFromSpecificMarshallerFail() throws JsonMarshallerException, JsonMarshallingException {
		System.out.println("getJsonResultFromSpecificMarshaller");
		doThrow(JsonMarshallerException.class).when(instance).getIJsonMarshallerInstance(any(Class.class));
		JsonMarshaller jm = mock(JsonMarshaller.class);
		when(jm.value()).thenReturn((Class) BadMarshaller1.class);
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

	/**
	 * Test of getJsonResultFromSpecificMarshaller method, of class ArgumentServices.
	 *
	 * @throws java.lang.Exception
	 */
	@Test
	public void testGetJsonResultFromSpecificMarshallerMap() throws Exception {
		System.out.println("getJsonResultFromSpecificMarshallerMap");
		IJsonMarshaller ijm = mock(IJsonMarshaller.class);
		when(ijm.toJson(anyObject())).thenReturn("\"JSON\"");
		String result = instance.getJsonResultFromSpecificMarshallerMap(getMapOfLocale(), ijm);
		assertThat(result).isEqualTo("{\"FR\":\"JSON\",\"US\":\"JSON\"}");
	}

	@Test
	public void testGetJavaResultFromSpecificUnmarshallerMap() throws JsonUnmarshallingException {
		System.out.println("getJavaResultFromSpecificUnmarshallerMap");
		LocaleMarshaller lm = new LocaleMarshaller();
		String json = "{\"KEY1\":{\"country\":\"FR\",\"language\":\"fr\"},\"KEY2\":{\"country\":\"US\",\"language\":\"en\"}}";
		Map result = instance.getJavaResultFromSpecificUnmarshallerMap(json, lm);
		assertThat(result).hasSize(2);
		assertThat(result.get("KEY1")).isEqualTo(Locale.FRANCE);
		assertThat(result.get("KEY2")).isEqualTo(Locale.US);
	}

	@Test(expected = JsonUnmarshallingException.class)
	public void testGetJavaResultFromSpecificUnmarshallerMapFail() throws JsonUnmarshallingException {
		System.out.println("getJavaResultFromSpecificUnmarshallerMap");
		LocaleMarshaller lm = new LocaleMarshaller();
		String json = "{{\"KEY1\"}:{\"country\":\"FR\",\"language\":\"fr\"},{\"KEY2\"}:{\"country\":\"US\",\"language\":\"en\"}}";
		instance.getJavaResultFromSpecificUnmarshallerMap(json, lm);
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
	
	/**
	 * Test of checkType method, of class.
	 * @throws org.ocelotds.marshalling.exceptions.JsonUnmarshallingException
	 */
	@Test(expected = JsonUnmarshallingException.class)
	public void checkTypeOtherTest() throws JsonUnmarshallingException {
		System.out.println("checkType");
		instance.checkType(mock(Result.class), mock(Type.class));
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
	
	private Map<String, Locale> getMapOfLocale() {
		Map<String, Locale> map = new HashMap<>();
		map.put("FR", Locale.FRANCE);
		map.put("US", Locale.US);
		return map;
	}
}
