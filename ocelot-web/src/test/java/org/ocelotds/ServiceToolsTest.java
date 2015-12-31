/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.annotation.Annotation;
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
import org.ocelotds.marshallers.LocaleUnmarshaller;
import org.ocelotds.marshalling.annotations.JsonUnmarshaller;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;
import org.ocelotds.objects.Result;
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

	@Before
	public void init () throws JsonProcessingException {
		ObjectMapper om = new ObjectMapper();
		doReturn(om).when(instance).getObjectMapper();
	}

	/**
	 * Test of getShortName method, of class ServiceTools.
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
	 * Test of getJsonUnmarshaller method, of class ServiceTools.
	 */
	@Test
	public void testGetJsonUnmarshallerAnnotationPresent() {
		System.out.println("getJsonUnmarshallerPresent");
		JsonUnmarshaller ju = mock(JsonUnmarshaller.class);
		Class cls = LocaleUnmarshaller.class;
		when(ju.value()).thenReturn(cls);
		Class anno = JsonUnmarshaller.class;
		when(ju.annotationType()).thenReturn(anno);
		Annotation[] annotations = new Annotation[]{ju};
		org.ocelotds.marshalling.JsonUnmarshaller result = instance.getJsonUnmarshaller(annotations);
		assertThat(result).isInstanceOf(cls);
	}

	/**
	 * Test of getJsonUnmarshaller method, of class ServiceTools.
	 */
	@Test
	public void testGetBadJsonUnmarshaller() {
		System.out.println("getBadJsonUnmarshaller");
		JsonUnmarshaller ju = mock(JsonUnmarshaller.class);
		Class cls = BadUnmarshaller.class;
		when(ju.value()).thenReturn(cls);
		Class anno = JsonUnmarshaller.class;
		when(ju.annotationType()).thenReturn(anno);
		Annotation[] annotations = new Annotation[]{ju};
		org.ocelotds.marshalling.JsonUnmarshaller result = instance.getJsonUnmarshaller(annotations);
		assertThat(result).isNull();
	}

	static class BadUnmarshaller implements org.ocelotds.marshalling.JsonUnmarshaller<String> {
		
		public BadUnmarshaller(String t) {
			
		}

		@Override
		public String toJava(String json) throws JsonUnmarshallingException {
			return null;
		}
		
	}

	/**
	 * Test of getJsonUnmarshaller method, of class ServiceTools.
	 */
	@Test
	public void testGetJsonUnmarshallerAnnotationNotPresent() {
		System.out.println("getJsonUnmarshallerNotPresent");
		Annotation[] annotations = new Annotation[]{};
		org.ocelotds.marshalling.JsonUnmarshaller result = instance.getJsonUnmarshaller(annotations);
		assertThat(result).isNull();
	}

	/**
	 * Test of getTemplateOfType method, of class ServiceTools.
	 */
	@Test
	public void testGetTemplateOfType() {
		System.out.println("getTemplateOfType");
		Type type = mock(Type.class);
		String expResult = "ok";
		doReturn(expResult).when(instance)._getTemplateOfType(type);
		String result = instance.getTemplateOfType(type, null);
		assertThat(result).isEqualTo(expResult);
		
		org.ocelotds.marshalling.JsonUnmarshaller ju = mock(org.ocelotds.marshalling.JsonUnmarshaller.class);
		result = instance.getTemplateOfType(type, ju);
		assertThat(result).isEqualTo(expResult);
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
	 * Test of getRealClass method, of class ServiceTools.
	 * @throws java.lang.ClassNotFoundException
	 */
	@Test
	public void testGetRealClass() throws ClassNotFoundException {
		System.out.println("getRealClass");
		doReturn("java.lang.String").doThrow(ClassNotFoundException.class).when(instance).getRealClassname(anyString());
		Class result = instance.getRealClass(Integer.class);
		assertThat(result).isEqualTo(String.class);

		result = instance.getRealClass(Integer.class);
		assertThat(result).isEqualTo(Integer.class);
	}

	/**
	 * Test of isConsiderateMethod method, of class ServiceTools.
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
	 */
	@Test
	public void test_getTemplateOfType() {
		System.out.println("_getTemplateOfType");
		Class type = String.class;
		ParameterizedType ptype = mock(ParameterizedType.class);
		String expResult1 = "Type";
		String expResult2 = "ParameterizedType";
		doReturn(expResult1).when(instance).getTemplateOfClass(any(Class.class));
		doReturn(expResult2).when(instance).getTemplateOfParameterizedType(any(ParameterizedType.class));

		String result = instance._getTemplateOfType(type);
		assertThat(result).isEqualTo(expResult1);
		
		result = instance._getTemplateOfType(ptype);
		assertThat(result).isEqualTo(expResult2);
	}

	/**
	 * Test of getTemplateOfClass method, of class ServiceTools.
	 * @throws com.fasterxml.jackson.core.JsonProcessingException
	 */
	@Test
	public void testGetTemplateOfClass() throws JsonProcessingException {
		System.out.println("getTemplateOfClass");
		ObjectMapper om = new ObjectMapper();
		Result r = new Result();
		String expResult = om.writeValueAsString(r);
		Class cls = Boolean.class;
		String result = instance.getTemplateOfClass(cls);
		assertThat(result).isEqualTo("false");

		cls = Boolean.TYPE;
		result = instance.getTemplateOfClass(cls);
		assertThat(result).isEqualTo("false");

		cls = Integer.class;
		result = instance.getTemplateOfClass(cls);
		assertThat(result).isEqualTo("0");

		cls = Integer.TYPE;
		result = instance.getTemplateOfClass(cls);
		assertThat(result).isEqualTo("0");

		cls = Long.class;
		result = instance.getTemplateOfClass(cls);
		assertThat(result).isEqualTo("0");

		cls = Long.TYPE;
		result = instance.getTemplateOfClass(cls);
		assertThat(result).isEqualTo("0");

		cls = Float.class;
		result = instance.getTemplateOfClass(cls);
		assertThat(result).isEqualTo("0.0");

		cls = Float.TYPE;
		result = instance.getTemplateOfClass(cls);
		assertThat(result).isEqualTo("0.0");

		cls = Double.class;
		result = instance.getTemplateOfClass(cls);
		assertThat(result).isEqualTo("0.0");

		cls = Double.TYPE;
		result = instance.getTemplateOfClass(cls);
		assertThat(result).isEqualTo("0.0");

		cls = Float[].class;
		result = instance.getTemplateOfClass(cls);
		assertThat(result).isEqualTo("[0.0,0.0]");

		cls = Result.class;
		result = instance.getTemplateOfClass(cls);
		assertThat(result).isEqualTo(expResult);

		cls = Result[].class;
		result = instance.getTemplateOfClass(cls);
		assertThat(result).isEqualTo("["+expResult+","+expResult+"]");

		cls = Locale.class;
		result = instance.getTemplateOfClass(cls);
		assertThat(result).isEqualTo("locale");
	}

	/**
	 * Test of getTemplateOfParameterizedType method, of class ServiceTools.
	 */
	@Test
	public void testGetTemplateOfParameterizedType() {
		System.out.println("getTemplateOfParameterizedType");
		ParameterizedType parameterizedType = mock(ParameterizedType.class);
		when(parameterizedType.getRawType()).thenReturn(Collection.class).thenReturn(Map.class).thenReturn(String.class);
		doReturn("Iterable").when(instance).getTemplateOfIterable(any(Type[].class));
		doReturn("Map").when(instance).getTemplateOfMap(any(Type[].class));

		String result = instance.getTemplateOfParameterizedType(parameterizedType);
		assertThat(result).isEqualTo("Iterable");
		
		result = instance.getTemplateOfParameterizedType(parameterizedType);
		assertThat(result).isEqualTo("Map");

		result = instance.getTemplateOfParameterizedType(parameterizedType);
		assertThat(result).isEqualTo("string");
	}

	/**
	 * Test of getTemplateOfIterable method, of class ServiceTools.
	 * @throws com.fasterxml.jackson.core.JsonProcessingException
	 */
	@Test
	public void testGetTemplateOfIterable() throws JsonProcessingException {
		System.out.println("getTemplateOfIterable");
		ObjectMapper om = new ObjectMapper();
		Result r = new Result();
		String res = om.writeValueAsString(r);

		String expResult = "["+res+","+res+"]";
		String result = instance.getTemplateOfIterable(new Type[] {Result.class});
		assertThat(result).isEqualTo(expResult);
		
		result = instance.getTemplateOfIterable(new Type[] {Result.class, String.class});
		assertThat(result).isEqualTo(expResult);
		
		result = instance.getTemplateOfIterable(new Type[] {});
		assertThat(result).isEqualTo("[]");
	}

	/**
	 * Test of getTemplateOfMap method, of class ServiceTools.
	 * @throws com.fasterxml.jackson.core.JsonProcessingException
	 */
	@Test
	public void testGetTemplateOfMap() throws JsonProcessingException {
		System.out.println("getTemplateOfMap");
		Type[] actualTypeArguments = new Type[] {String.class, Result.class};
		ObjectMapper om = new ObjectMapper();
		Result r = new Result();
		String res = om.writeValueAsString(r);

		String expResult = "{\"\":"+res+"}";
		String result = instance.getTemplateOfMap(actualTypeArguments);
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

	/**
	 * Test of getRealClassname method, of class ServiceTools.
	 * @throws java.lang.ClassNotFoundException
	 */
	@Test
	public void testGetRealClassname() throws ClassNotFoundException {
		System.out.println("getRealClassname");
		String proxyname = "java.lang.String$Proxy";
		
		String result = instance.getRealClassname(proxyname);
		assertThat(result).isEqualTo("java.lang.String");
		
	}

	/**
	 * Test of getRealClassname method, of class ServiceTools.
	 * @throws java.lang.ClassNotFoundException
	 */
	@Test(expected = ClassNotFoundException.class)
	public void testGetRealClassnameNotProxy() throws ClassNotFoundException {
		System.out.println("getRealClassname");
		String proxyname = "java.lang.String";
		
		instance.getRealClassname(proxyname);
	}
}