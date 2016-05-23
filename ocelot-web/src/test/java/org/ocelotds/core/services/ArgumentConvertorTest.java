/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.enterprise.util.AnnotationLiteral;
import javax.ws.rs.core.GenericType;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.annotations.TransientDataService;
import org.ocelotds.literals.JsonUnmarshallerLiteral;
import org.ocelotds.marshallers.JsonMarshallerException;
import org.ocelotds.marshallers.JsonMarshallerServices;
import org.ocelotds.marshallers.LocaleMarshaller;
import org.ocelotds.marshallers.TemplateMarshaller;
import org.ocelotds.marshalling.IJsonMarshaller;
import org.ocelotds.marshalling.annotations.JsonMarshallerType;
import org.ocelotds.marshalling.annotations.JsonUnmarshaller;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;
import org.ocelotds.objects.Result;
import org.ocelotds.spi.DataServiceException;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class ArgumentConvertorTest {

	@Mock
	private Logger logger;

	@Mock
	ArgumentServices argumentServices;
	
	@Mock
	JsonMarshallerServices jsonMarshallerServices;

	@InjectMocks
	@Spy
	private ArgumentConvertor instance;

	private final Annotation JSONUNMARSHALLER = new JsonUnmarshallerLiteral(LocaleMarshaller.class);
	private final Annotation TRANSIENTDATASERVICE = new AnnotationLiteral<TransientDataService>() {
	};

	@Before
	public void init() {
		instance.objectMapper = new ObjectMapper();
	}

	/**
	 * Test of convertJsonToJava method, of class ArgumentConvertor.
	 *
	 * @throws org.ocelotds.marshalling.exceptions.JsonUnmarshallingException
	 * @throws org.ocelotds.marshallers.JsonMarshallerException
	 */
	@Test
	public void testConvertNullJsonToJava() throws JsonUnmarshallingException, JsonMarshallerException {
		System.out.println("convertNullJsonToJava");
		doReturn(null).when(instance).getMarshallerAnnotation(any(Annotation[].class));
		Object result = instance.convertJsonToJava("null", String.class, new Annotation[]{});
		assertThat(result).isEqualTo(null);
	}

	/**
	 * Test of convertJsonToJava method, of class ArgumentConvertor.
	 *
	 * @throws org.ocelotds.marshalling.exceptions.JsonUnmarshallingException
	 * @throws org.ocelotds.marshallers.JsonMarshallerException
	 */
	@Test
	public void testConvertJsonToJava() throws JsonUnmarshallingException, JsonMarshallerException {
		System.out.println("convertJsonToJava");
		doReturn(null).when(instance).getMarshallerAnnotation(any(Annotation[].class));
		doReturn("result").when(instance).convertArgument(anyString(), any(Type.class));
		Object result = instance.convertJsonToJava("\"result\"", String.class, new Annotation[]{});
		assertThat(result).isEqualTo("result");
	}

	/**
	 * Test of convertJsonToJava method, of class ArgumentConvertor.
	 *
	 * @throws org.ocelotds.marshalling.exceptions.JsonUnmarshallingException
	 * @throws org.ocelotds.marshallers.JsonMarshallerException
	 */
	@Test
	public void testConvertJsonToJavaWithUnmarshaller() throws JsonUnmarshallingException, JsonMarshallerException {
		System.out.println("convertJsonToJavaWithUnmarshaller");
		JsonUnmarshaller juma = mock(JsonUnmarshaller.class);
		Locale expectResult = Locale.FRANCE;
		when(juma.value()).thenReturn((Class) LocaleMarshaller.class);
		when(juma.type()).thenReturn(JsonMarshallerType.SINGLE);
		doReturn(juma).when(instance).getJsonUnmarshallerAnnotation(any(Annotation[].class));
		doReturn(expectResult).when(instance).getResult(anyString(), any(IJsonMarshaller.class), any(JsonMarshallerType.class));
		doNothing().when(argumentServices).checkType(any(JsonUnmarshaller.class), any(Type.class));
		Object result = instance.convertJsonToJava("{\"language\":\"fr\",\"country\":\"FR\"}", Locale.class, new Annotation[]{});
		assertThat(result).isInstanceOf(Locale.class);
	}

	@Test
	public void testGetResultSingle() throws JsonUnmarshallingException {
		System.out.println("getResult");
		String jsonArg = "{\"language\":\"fr\",\"country\":\"FR\"}";
		LocaleMarshaller lm = new LocaleMarshaller();
		Object result = instance.getResult(jsonArg, lm, JsonMarshallerType.SINGLE);
		assertThat(result).isInstanceOf(Locale.class);
	}

	@Test
	public void testGetResultIterable() throws JsonUnmarshallingException {
		System.out.println("getResult");
		String jsonArg = "[{\"language\":\"fr\",\"country\":\"FR\"}, {\"language\":\"fr\",\"country\":\"FR\"}]";
		LocaleMarshaller lm = new LocaleMarshaller();
		when(argumentServices.getJavaResultFromSpecificUnmarshallerIterable(anyString(), any(IJsonMarshaller.class))).thenReturn(Arrays.asList(Locale.FRANCE, Locale.US));
		Object result = instance.getResult(jsonArg, lm, JsonMarshallerType.LIST);
		assertThat(result).isInstanceOf(List.class);
	}

	@Test
	public void testGetResultMap() throws JsonUnmarshallingException {
		System.out.println("getResult");
		String jsonArg = "{\"KEY1\":{\"language\":\"fr\",\"country\":\"FR\"},\"KEY2\":{\"language\":\"fr\",\"country\":\"FR\"}}";
		LocaleMarshaller lm = new LocaleMarshaller();
		
		when(argumentServices.getJavaResultFromSpecificUnmarshallerMap(anyString(), any(IJsonMarshaller.class))).thenReturn(getMapOfLocale());
		Object result = instance.getResult(jsonArg, lm, JsonMarshallerType.MAP);
		assertThat(result).isInstanceOf(Map.class);
	}

	/**
	 * Test of getJsonUnmarshallerAnnotation method, of class.
	 */
	@Test
	public void test_getJsonUnmarshallerAnnotation() {
		System.out.println("getJsonUnmarshallerAnnotation");
		Object result = instance.getJsonUnmarshallerAnnotation(new Annotation[]{TRANSIENTDATASERVICE, TRANSIENTDATASERVICE});
		assertThat(result).isNull();
		result = instance.getJsonUnmarshallerAnnotation(new Annotation[]{TRANSIENTDATASERVICE, JSONUNMARSHALLER, TRANSIENTDATASERVICE});
		assertThat(result).isNotNull();
	}

	/**
	 * Test of getMarshallerAnnotation method, of class ArgumentConvertor.
	 *
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testGetMarshallerAnnotation() throws NoSuchMethodException {
		System.out.println("getMarshallerAnnotation");
		doReturn(null).doReturn(JSONUNMARSHALLER).when(instance).getJsonUnmarshallerAnnotation(any(Annotation[].class));
		Class result = instance.getMarshallerAnnotation(new Annotation[]{});
		assertThat(result).isNull();
		result = instance.getMarshallerAnnotation(new Annotation[]{});
		assertThat(result).isEqualTo(LocaleMarshaller.class);
	}

	/**
	 * Test of getMarshallerAnnotation method, of class ArgumentConvertor.
	 *
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testNotGetMarshallerAnnotation() throws NoSuchMethodException {
		System.out.println("getMarshallerAnnotation");
		Class result = instance.getMarshallerAnnotation(new Annotation[]{});
		assertThat(result).isEqualTo(null);
	}

	/**
	 * Test of convertArgument method, of class ArgumentConvertor.
	 */
	@Test
	public void testConvertArgumentNull() throws IllegalArgumentException {
		System.out.println("convertArgument");
		Object result = instance.convertArgument(null, Result.class);
		assertThat(result).isNull();
		result = instance.convertArgument("null", Result.class);
		assertThat(result).isNull();

	}

	/**
	 * Test of convertArgument method, of class ArgumentConvertor.
	 */
	@Test
	public void testConvertArgument() throws IllegalArgumentException {
		System.out.println("convertArgument");
		Iterator<String> args = Arrays.asList("\"toto\"", "5", "[\"a\",\"b\"]", "[[\"a\", \"b\"],[\"c\", \"d\"]]",
				  "[\"c\",\"d\"]", "{\"a\":1, \"b\":2, \"c\":3}", "{\"integer\":5}").iterator();
		Type col = new GenericType<Collection<String>>() {
		}.getType();
		Type map = new GenericType<Map<String, Integer>>() {
		}.getType();
		Type colArray = new GenericType<Collection<String[]>>() {
		}.getType();
		Type array = new String[]{}.getClass();
		for (final Type type : new Type[]{String.class, Integer.class, array, colArray, col, map, Result.class}) {
			String arg = args.next();
			Object result = instance.convertArgument(arg, type);
			assertThat(result).is(new Condition<Object>("" + type) {
				@Override
				public boolean matches(Object t) {
					Class cls;
					if (type instanceof ParameterizedType) {
						cls = (Class) ((ParameterizedType) type).getRawType();
					} else {
						cls = (Class) type;
					}
					return cls.isInstance(t);
				}
			});
		}
	}

	/**
	 * Test of convertArgument method, of class ArgumentConvertor.
	 */
	@Test
	public void testConvertArgumentUnsupportedTypes() {
		Type type = mock(WildcardType.class);
		Object result = instance.convertArgument("", type);
		assertThat(result).isNull();

		type = mock(GenericArrayType.class);
		result = instance.convertArgument("", type);
		assertThat(result).isNull();

		type = mock(TypeVariable.class);
		result = instance.convertArgument("", type);
		assertThat(result).isNull();

		ArgumentCaptor<Type> types = ArgumentCaptor.forClass(Type.class);
		verify(logger, times(3)).warn(anyString(), anyString(), types.capture());
		List<Type> results = types.getAllValues();
		assertThat(results).hasSize(3);
		assertThat(results.get(0)).isInstanceOf(WildcardType.class);
		assertThat(results.get(1)).isInstanceOf(GenericArrayType.class);
		assertThat(results.get(2)).isInstanceOf(TypeVariable.class);
	}

	@Test
	public void testCheckArgumentOk() throws IOException {
		System.out.println("checkArgument");
		instance.checkStringArgument(String.class, "\"toto\"");
		instance.checkStringArgument(Integer.class, "5");
	}

	@Test(expected = IOException.class)
	public void testCheckArgumentNok1() throws IOException {
		System.out.println("checkArgument");
		instance.checkStringArgument(String.class, "toto");
	}

	@Test(expected = IOException.class)
	public void testCheckArgumentNok2() throws IOException {
		System.out.println("checkArgument");
		instance.checkStringArgument(Integer.class, "\"5\"");

	}

	/**
	 * Test of convertArgument method, of class ArgumentConvertor.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testConvertArgumentFail() throws IllegalArgumentException {
		System.out.println("convertArgumentFail");
		instance.convertArgument("toto", String.class);
	}

	@Test(expected = JsonUnmarshallingException.class)
	public void testConvertJsonToJavaBadUnmarshaller() throws DataServiceException, JsonUnmarshallingException, JsonMarshallerException, NoSuchMethodException {
		System.out.println("convertJsonToJavaBadUnmarshaller");

		Method method = ClassAsDataService.class.getMethod("methodWithBadUnmarshaller", String.class);
		when(jsonMarshallerServices.getIJsonMarshallerInstance(any(Class.class))).thenThrow(JsonUnmarshallingException.class);
		Annotation[] annotations = method.getParameterAnnotations()[0];
		instance.convertJsonToJava("", null, annotations);
	}

	@Test
	public void testGetObjectMapper() {
		System.out.println("getObjectMapper");
		doCallRealMethod().when(instance).getObjectMapper();
		ObjectMapper result = instance.getObjectMapper();
		assertThat(result).isInstanceOf(ObjectMapper.class);
	}

	private Map<String, Locale> getMapOfLocale() {
		Map<String, Locale> map = new HashMap<>();
		map.put("FR", Locale.FRANCE);
		map.put("US", Locale.US);
		return map;
	}
}
