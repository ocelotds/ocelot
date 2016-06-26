/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.dashboard.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.TransientDataService;
import org.ocelotds.marshalling.exceptions.JsonMarshallerException;
import org.ocelotds.marshalling.JsonMarshallerServices;
import org.ocelotds.marshallers.LocaleMarshaller;
import org.ocelotds.marshallers.TemplateMarshaller;
import org.ocelotds.marshalling.IJsonMarshaller;
import org.ocelotds.marshalling.annotations.JsonUnmarshaller;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;
import org.ocelotds.dashboard.objects.Result;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceToolsTest {

	@Mock
	private Logger logger;

	@InjectMocks
	@Spy
	private ServiceTools instance;

	@Mock
	ObjectMapper objectMapper;

	@Mock
	TemplateMarshaller templateMarshaller = new TemplateMarshaller();
	
	@Mock
	JsonMarshallerServices jsonMarshallerServices;

	@Before
	public void init() throws JsonProcessingException {
		ObjectMapper om = new ObjectMapper();
		doReturn(om).when(instance).getObjectMapper();
	}

	/**
	 * Test of getShortName method, of class ServiceTools.
	 *
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testGetShortName() throws NoSuchMethodException {
		System.out.println("getShortName");
		String fullname = "java.lang.Collection<java.lang.Map<java.lang.String, java.lang.Collection<java.lang.Integer>>>";
		String expResult = "Collection<Map<String, Collection<Integer>>>";
		String result = instance.getShortName(fullname);
		assertThat(result).isEqualTo(expResult);

		result = instance.getShortName(null);
		assertThat(result).isEqualTo("");
	}

	/**
	 * Test of getLiteralType method, of class ServiceTools.
	 */
	@Test
	public void testGetLiteralType() {
		System.out.println("getLiteralType");
		String result = instance.getLiteralType(String.class);
		assertThat(result).isEqualTo("java.lang.String");

		result = instance.getLiteralType(null);
		assertThat(result).isEqualTo("");
	}

	/**
	 * Test of getLiteralType method, of class ServiceTools.
	 */
	@Test
	public void testGetLiteralParameterizedType() {
		System.out.println("getLiteralType");
		String expResult = "java.lang.Collection<java.lang.Map<java.lang.String, java.lang.Collection<java.lang.Integer>>>";
		ParameterizedType type = mock(ParameterizedType.class);
		when(type.toString()).thenReturn(expResult);
		String result = instance.getLiteralType(type);
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getJsonMarshallerFromAnnotations method, of class ServiceTools.
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 */
	@Test
	public void testGetJsonMarshallerFromAnnotationsNullArgument() throws JsonMarshallerException {
		System.out.println("getJsonMarshallerFromAnnotationsNullArgument");
		org.ocelotds.marshalling.IJsonMarshaller result = instance.getJsonMarshaller(null);
		assertThat(result).isNull();
	}

	/**
	 * Test of getJsonMarshallerFromAnnotations method, of class ServiceTools.
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 */
	@Test
	public void testGetJsonMarshallerFromAnnotationsNoAnnotation() throws JsonMarshallerException {
		System.out.println("getJsonMarshallerFromAnnotationsNoAnnotation");
		Annotation[] annotations = new Annotation[0];
		org.ocelotds.marshalling.IJsonMarshaller result = instance.getJsonMarshaller(annotations);
		assertThat(result).isNull();
	}

	/**
	 * Test of getJsonMarshallerFromAnnotations method, of class ServiceTools.
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 */
	@Test
	public void testGetJsonMarshallerFromAnnotationsNoUnmarshaller() throws JsonMarshallerException {
		System.out.println("getJsonMarshallerFromAnnotationsNoUnmarshaller");
		doReturn(null).when(instance).getJsonMarshallerFromAnnotation(any(JsonUnmarshaller.class));
		JsonUnmarshaller ju = mock(JsonUnmarshaller.class);
		Class anno = JsonUnmarshaller.class;
		when(ju.annotationType()).thenReturn(anno);
		Annotation[] annotations = new Annotation[] {ju};
		org.ocelotds.marshalling.IJsonMarshaller result = instance.getJsonMarshaller(annotations);
		assertThat(result).isNull();
	}

	/**
	 * Test of getJsonMarshallerFromAnnotations method, of class ServiceTools.
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 */
	@Test
	public void testGetJsonMarshallerFromAnnotations() throws JsonMarshallerException {
		System.out.println("getJsonMarshallerFromAnnotations");
		org.ocelotds.marshalling.IJsonMarshaller ju1 = mock(org.ocelotds.marshalling.IJsonMarshaller.class);
		doReturn(ju1).when(instance).getJsonMarshallerFromAnnotation(any(JsonUnmarshaller.class));
		JsonUnmarshaller ju = mock(JsonUnmarshaller.class);
		Class anno = JsonUnmarshaller.class;
		when(ju.annotationType()).thenReturn(anno);
		Annotation[] annotations = new Annotation[] {ju};
		// getJsonUnmarshallerFromAnnotation return JsonUnmarshaller
		org.ocelotds.marshalling.IJsonMarshaller result = instance.getJsonMarshaller(annotations);
		assertThat(result).isEqualTo(ju1);
	}

	/**
	 * Test of getJsonMarshallerFromAnnotation method, of class ServiceTools.
	 * @throws java.lang.InstantiationException
	 * @throws java.lang.IllegalAccessException
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 */
	@Test
	public void testGetJsonMarshallerAnnotationPresent() throws InstantiationException, IllegalAccessException, JsonMarshallerException {
		System.out.println("getJsonMarshallerPresent");
		JsonUnmarshaller ju = mock(JsonUnmarshaller.class);
		Class<? extends IJsonMarshaller> cls = LocaleMarshaller.class;
		when(ju.value()).thenReturn((Class) cls);
		when(jsonMarshallerServices.getIJsonMarshallerInstance(eq(cls))).thenReturn(cls.newInstance());
		org.ocelotds.marshalling.IJsonMarshaller result = instance.getJsonMarshallerFromAnnotation(ju);
		assertThat(result).isInstanceOf(cls);
		result = instance.getJsonMarshallerFromAnnotation(null);
		assertThat(result).isNull();
	}

	/**
	 * Test of getJsonMarshallerFromAnnotation method, of class ServiceTools.
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 */
//	@Test
	public void testGetBadJsonMarshaller() throws JsonMarshallerException {
		System.out.println("getBadJsonMarshaller");
		JsonUnmarshaller ju = mock(JsonUnmarshaller.class);
		Class cls = BadUnmarshaller.class;
		when(ju.value()).thenReturn(cls);
		org.ocelotds.marshalling.IJsonMarshaller result = instance.getJsonMarshallerFromAnnotation(ju);
		assertThat(result).isNull();
	}

	static class BadUnmarshaller implements org.ocelotds.marshalling.IJsonMarshaller<String> {

		public BadUnmarshaller(String t) {

		}

		@Override
		public String toJava(String json) throws JsonUnmarshallingException {
			return null;
		}

		@Override
		public String toJson(String obj) throws JsonMarshallingException {
			return null;
		}

	}

	/**
	 * Test of getJsonMarshallerFromAnnotation method, of class ServiceTools.
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 */
	@Test
	public void testGetJsonMarshallerAnnotationNotPresent() throws JsonMarshallerException {
		System.out.println("getJsonUnmarshallerNotPresent");
		org.ocelotds.marshalling.IJsonMarshaller result = instance.getJsonMarshallerFromAnnotation(null);
		assertThat(result).isNull();
	}

	/**
	 * Test of getTemplateOfType method, of class ServiceTools.
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallerException
	 */
	@Test
	public void testGetTemplateOfType() throws JsonMarshallerException {
		System.out.println("getTemplateOfType");
		Class type = String.class;
		org.ocelotds.marshalling.IJsonMarshaller ju = mock(org.ocelotds.marshalling.IJsonMarshaller.class);
		String expResult0 = "ok0";
		String expResult1 = "ok1";
		when(jsonMarshallerServices.getIJsonMarshallerInstance(any(Class.class))).thenReturn(mock(TemplateMarshaller.class));
		doReturn(expResult0).when(instance)._getTemplateOfType(eq(type), any(TemplateMarshaller.class));
		doReturn(expResult1).when(instance)._getTemplateOfType(eq(type), eq(ju));
		String result = instance.getTemplateOfType(type, null);
		assertThat(result).isEqualTo(expResult0);

		result = instance.getTemplateOfType(type, ju);
		assertThat(result).isEqualTo(expResult1);
	}

	/**
	 * Test of getInstanceNameFromDataservice method, of class ServiceTools.
	 */
	@Test
	public void testGetInstanceNameFromDataservice() {
		System.out.println("getInstanceNameFromDataservice");
		String result = instance.getInstanceNameFromDataservice(DataService1.class);
		assertThat(result).isEqualTo("dataService1");
	}

	/**
	 * Test of getInstanceNameFromDataservice method, of class ServiceTools.
	 */
	@Test
	public void testGetInstanceNameFromDataserviceWithName() {
		System.out.println("getInstanceNameFromDataservice");
		String result = instance.getInstanceNameFromDataservice(DataService2.class);
		assertThat(result).isEqualTo("dS2");
	}

	@DataService(resolver = "")
	static class DataService1 {

		public void publicMethod() {

		}

		@TransientDataService
		public void transientMethod() {

		}

		public static void staticMethod() {

		}

		protected static void staticProtectedMethod() {

		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}
	}

	@DataService(resolver = "", name = "DS2")
	static class DataService2 {

	}

	/**
	 * Test of isConsiderateMethod method, of class ServiceTools.
	 *
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testIsConsiderateMethod() throws NoSuchMethodException {
		System.out.println("isConsiderateMethod");
		Method method = DataService1.class.getMethod("publicMethod");
		boolean result = instance.isConsiderateMethod(method);
		assertThat(result).isTrue();

		method = DataService1.class.getMethod("hashCode"); // this method is overrided
		result = instance.isConsiderateMethod(method);
		assertThat(result).isTrue();

		method = DataService1.class.getMethod("toString");
		result = instance.isConsiderateMethod(method);
		assertThat(result).isFalse();

		method = DataService1.class.getMethod("transientMethod");
		result = instance.isConsiderateMethod(method);
		assertThat(result).isFalse();

		method = DataService1.class.getMethod("staticMethod");
		result = instance.isConsiderateMethod(method);
		assertThat(result).isFalse();

		method = DataService1.class.getDeclaredMethod("staticProtectedMethod");
		result = instance.isConsiderateMethod(method);
		assertThat(result).isFalse();
	}

	/**
	 * Test of getTemplateOfClass method, of class ServiceTools.
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 */
	@Test
	public void test_getTemplateOfType() throws JsonMarshallingException {
		System.out.println("_getTemplateOfType");
		Class type = String.class;
		ParameterizedType ptype = mock(ParameterizedType.class);
		String expResult1 = "Type";
		String expResult2 = "ParameterizedType";
		doReturn(expResult1).when(instance).getInstanceOfClass(eq(String.class));
		doReturn(expResult2).when(instance).getTemplateOfParameterizedType(any(ParameterizedType.class), any(IJsonMarshaller.class));
		when(templateMarshaller.toJson(any(String.class))).thenReturn("\""+expResult1+"\"").thenThrow(JsonMarshallingException.class);

		String result = instance._getTemplateOfType(type, templateMarshaller);
		assertThat(result).isEqualTo("\""+expResult1+"\"");

		result = instance._getTemplateOfType(type, templateMarshaller);
		assertThat(result).isEqualTo("string");
		
		result = instance._getTemplateOfType(ptype, templateMarshaller);
		assertThat(result).isEqualTo(expResult2);
	}

	/**
	 * Test of getInstanceOfClass method, of class ServiceTools.
	 *
	 * @throws com.fasterxml.jackson.core.JsonProcessingException
	 */
	@Test
	public void testGetInstanceOfClass() throws JsonProcessingException {
		System.out.println("getTemplateOfClass");
		ObjectMapper om = new ObjectMapper();
		Result r = new Result();
		String expResult = om.writeValueAsString(r);
		Class cls = Boolean.class;
		Object result = instance.getInstanceOfClass(cls);
		assertThat(result).isEqualTo(false);

		cls = Boolean.TYPE;
		result = instance.getInstanceOfClass(cls);
		assertThat(result).isEqualTo(false);

		cls = Integer.class;
		result = instance.getInstanceOfClass(cls);
		assertThat(result).isEqualTo(0);

		cls = Integer.TYPE;
		result = instance.getInstanceOfClass(cls);
		assertThat(result).isEqualTo(0);

		cls = Long.class;
		result = instance.getInstanceOfClass(cls);
		assertThat(result).isEqualTo(0L);

		cls = Long.TYPE;
		result = instance.getInstanceOfClass(cls);
		assertThat(result).isEqualTo(0l);

		cls = Float.class;
		result = instance.getInstanceOfClass(cls);
		assertThat(result).isEqualTo(0.1f);

		cls = Float.TYPE;
		result = instance.getInstanceOfClass(cls);
		assertThat(result).isEqualTo(0.1f);

		cls = Double.class;
		result = instance.getInstanceOfClass(cls);
		assertThat(result).isEqualTo(0.1d);

		cls = Double.TYPE;
		result = instance.getInstanceOfClass(cls);
		assertThat(result).isEqualTo(0.1d);

		cls = Float[].class;
		result = instance.getInstanceOfClass(cls);
		assertThat(result).isEqualTo(new Float[]{0.1f, 0.1f});

		cls = Result.class;
		result = instance.getInstanceOfClass(cls);
		assertThat(result).isEqualToComparingFieldByField(r);

		cls = Result[].class;
		result = instance.getInstanceOfClass(cls);
		assertThat(result).isEqualToComparingFieldByField(new Result[] {r, r});

		cls = Locale.class;
		result = instance.getInstanceOfClass(cls);
		assertThat(result).isInstanceOf(Locale.class);

		cls = Serializable.class;
		result = instance.getInstanceOfClass(cls);
		assertThat(result).isNull();
	}

	/**
	 * Test of getObjectFromConstantFields method, of class ServiceTools.
	 */
	@Test
	public void testGetObjectFromConstantFields() {
		System.out.println("getObjectFromConstantFields");
		
		Object result = instance.getObjectFromConstantFields(String.class);
		assertThat(result).isNull();
		
		result = instance.getObjectFromConstantFields(Locale.class);
		assertThat(result).isInstanceOf(Locale.class);
	}

	/**
	 * Test of getObjectFromConstantField method, of class ServiceTools.
	 * @throws java.lang.NoSuchFieldException
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testGetObjectFromConstantField() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		System.out.println("getObjectFromConstantField");
		Field badField = Result.class.getDeclaredField("integer");
		Field goodField = Locale.class.getField("US");
		
		Object result = instance.getObjectFromConstantField(Result.class, badField);
		assertThat(result).isNull();
		
		result = instance.getObjectFromConstantField(Locale.class, goodField);
		assertThat(result).isInstanceOf(Locale.class);

		doThrow(IllegalAccessException.class).doThrow(IllegalArgumentException.class).when(instance).getConstantFromField(any(Field.class));
		result = instance.getObjectFromConstantField(Locale.class, goodField);
		assertThat(result).isNull();

		result = instance.getObjectFromConstantField(Locale.class, goodField);
		assertThat(result).isNull();
	}
	
	/**
	 * Test of getConstantFromField method, of class ServiceTools.
	 * @throws java.lang.NoSuchFieldException
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testGetConstantFromField() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		System.out.println("getConstantFromField");
		Field field = Locale.class.getDeclaredField("US");
		instance.getConstantFromField(field);
	}
	
	/**
	 * Test of getConstantFromField method, of class ServiceTools.
	 * @throws java.lang.NoSuchFieldException
	 * @throws java.lang.IllegalAccessException
	 */
	@Test(expected = IllegalAccessException.class)
	public void testGetConstantFromFieldFailed1() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		System.out.println("getConstantFromField");
		Field field = Result.class.getDeclaredField("fieldOfClass");
		System.out.println("FIELD = "+field);
		instance.getConstantFromField(field);
	}

	/**
	 * Test of getTemplateOfParameterizedType method, of class ServiceTools.
	 */
	@Test
	public void testGetTemplateOfParameterizedType() {
		System.out.println("getTemplateOfParameterizedType");
		ParameterizedType parameterizedType = mock(ParameterizedType.class);
		when(parameterizedType.getRawType()).thenReturn(Collection.class).thenReturn(Map.class).thenReturn(String.class);
		doReturn("Iterable").when(instance).getTemplateOfIterable(any(Type[].class), any(IJsonMarshaller.class));
		doReturn("Map").when(instance).getTemplateOfMap(any(Type[].class), any(IJsonMarshaller.class));

		String result = instance.getTemplateOfParameterizedType(parameterizedType, templateMarshaller);
		assertThat(result).isEqualTo("Iterable");

		result = instance.getTemplateOfParameterizedType(parameterizedType, templateMarshaller);
		assertThat(result).isEqualTo("Map");

		result = instance.getTemplateOfParameterizedType(parameterizedType, templateMarshaller);
		assertThat(result).isEqualTo("string");
	}

	/**
	 * Test of getTemplateOfIterable method, of class ServiceTools.
	 *
	 * @throws com.fasterxml.jackson.core.JsonProcessingException
	 */
	@Test
	public void testGetTemplateOfIterable() throws JsonProcessingException {
		System.out.println("getTemplateOfIterable");
		ObjectMapper om = new ObjectMapper();
		Result r = new Result();
		String res = om.writeValueAsString(r);

		String expResult = "[" + res + "," + res + "]";
		doReturn(res).when(instance)._getTemplateOfType(any(Type.class), any(IJsonMarshaller.class));
		String result = instance.getTemplateOfIterable(new Type[]{Result.class}, templateMarshaller);
		assertThat(result).isEqualTo(expResult);

		result = instance.getTemplateOfIterable(new Type[]{Result.class, String.class}, templateMarshaller);
		assertThat(result).isEqualTo(expResult);

		result = instance.getTemplateOfIterable(new Type[]{}, templateMarshaller);
		assertThat(result).isEqualTo("[]");
	}

	/**
	 * Test of getTemplateOfMap method, of class ServiceTools.
	 *
	 * @throws com.fasterxml.jackson.core.JsonProcessingException
	 */
	@Test
	public void testGetTemplateOfMap() throws JsonProcessingException {
		System.out.println("getTemplateOfMap");
		Type[] actualTypeArguments = new Type[]{String.class, Result.class};
		ObjectMapper om = new ObjectMapper();
		Result r = new Result();
		String res = om.writeValueAsString(r);

		String expResult = "{\"key\":" + res + "}";
		doReturn("\"key\"").doReturn(res).when(instance)._getTemplateOfType(any(Type.class), any(IJsonMarshaller.class));
		String result = instance.getTemplateOfMap(actualTypeArguments, templateMarshaller);
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getInstanceName method, of class ServiceTools.
	 */
	@Test
	public void testGetInstanceName() {
		System.out.println("getInstanceName");
		String clsName = "String";
		String expResult = "string";
		String result = instance.getInstanceName(clsName);
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getObjectMapper method, of class ServiceTools.
	 */
	@Test
	public void getObjectMapper() {
		System.out.println("getObjectMapper");
		doCallRealMethod().when(instance).getObjectMapper();
		ObjectMapper o = instance.getObjectMapper();
		assertThat(o).isNotNull();
	}
}
