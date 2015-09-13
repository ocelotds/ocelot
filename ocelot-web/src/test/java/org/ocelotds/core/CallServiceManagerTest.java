/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.enterprise.inject.Instance;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.ws.rs.core.GenericType;
import org.ocelotds.messaging.MessageFromClient;
import org.assertj.core.api.Condition;
import org.assertj.core.data.Index;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.JsCacheResult;
import org.ocelotds.configuration.OcelotConfiguration;
import org.ocelotds.messaging.Fault;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.objects.Result;
import org.ocelotds.resolvers.DataServiceResolverIdLitteral;
import org.ocelotds.spi.DataServiceException;
import org.ocelotds.spi.IDataServiceResolver;
import org.ocelotds.spi.Scope;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.ocelotds.OcelotServices;
import org.ocelotds.marshallers.LocaleMarshaller;
import org.ocelotds.marshallers.LocaleUnmarshaller;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.marshalling.annotations.JsonUnmarshaller;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.ocelotds.resolvers.PojoResolver;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class CallServiceManagerTest {

	@Mock
	private Cleaner cleaner;

	@Mock
	private Instance<IDataServiceResolver> resolvers;

	@Mock
	private OcelotConfiguration configuration;

	@Spy
	@InjectMocks
	private CacheManager cacheManager = new CacheManager();

	@Mock
	private Logger logger;

	@Spy
	@InjectMocks
	private CallServiceManager callServiceManager;

	@Before
	public void init() {
		when(cleaner.cleanArg(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (String) args[0];
			}
		});
		when(configuration.getStacktracelength()).thenReturn(0);
	}

	/**
	 * Test of getUnMarshallerAnnotation method, of class CallServiceManager.
	 *
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testGetUnMarshallerAnnotation() throws NoSuchMethodException {
		Method method = OcelotServices.class.getMethod("setLocale", Locale.class);
		Annotation[][] parametersAnnotations = method.getParameterAnnotations();
		Annotation[] parameterAnnotations = parametersAnnotations[0];
		Class result = callServiceManager.getUnMarshallerAnnotation(parameterAnnotations);
		assertThat(result).isEqualTo(LocaleUnmarshaller.class);
	}

	/**
	 * Test of getResolver method, of class CallServiceManager.
	 */
	@Test
	public void testGetResolver() {
		Instance<IDataServiceResolver> instance = mock(Instance.class);
		when(resolvers.select(eq(new DataServiceResolverIdLitteral("pojo")))).thenReturn(instance);
		when(instance.get()).thenReturn(new PojoResolver());
		IDataServiceResolver result = callServiceManager.getResolver("pojo");
		assertThat(result).isInstanceOf(PojoResolver.class);
	}

	/**
	 * Test of getMethodFromDataService method, of class CallServiceManager.
	 *
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testGetMethodFromDataService() throws NoSuchMethodException {
		System.out.println("getMethodFromDataService");
		Class dsClass = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setOperation("methodWith2Arguments");
		message.setParameters(Arrays.asList("5", "\"toto\""));
		Object[] arguments = new Object[2];
		Method expResult = dsClass.getMethod("methodWith2Arguments", new Class<?>[]{Integer.class, String.class});
		Method result = callServiceManager.getMethodFromDataService(dsClass, message, arguments);
		assertThat(result).isEqualTo(expResult);
		assertThat(arguments).contains(5, Index.atIndex(0));
		assertThat(arguments).contains("toto", Index.atIndex(1));
	}

	/**
	 * Test of getMethodFromDataService method, of class CallServiceManager.
	 *
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test(expected = NoSuchMethodException.class)
	public void testGetMethodFromDataServiceNotFound() throws NoSuchMethodException {
		System.out.println("getMethodFromDataService");
		Class dsClass = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setOperation("methodWith2Arguments");
		message.setParameters(Arrays.asList("\"toto\"", "5"));
		Object[] arguments = new Object[2];
		callServiceManager.getMethodFromDataService(dsClass, message, arguments);
	}

	/**
	 * Test of getMethodFromDataServiceWithSessionInjection method, of class CallServiceManager.
	 *
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testGetMethodFromDataServiceWithSessionInjection() throws NoSuchMethodException {
		System.out.println("getMethodFromDataServiceWithSessionInjection");
		Session session = mock(Session.class);
		Class dsClass = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setOperation("methodWith2ArgumentsAndSession");
		message.setParameters(Arrays.asList("5", "\"toto\""));
		Object[] arguments = new Object[3];
		Method expResult = dsClass.getMethod("methodWith2ArgumentsAndSession", new Class<?>[]{Session.class, Integer.TYPE, String.class});
		Method result = callServiceManager.getMethodFromDataServiceWithSessionInjection(session, dsClass, message, arguments);
		assertThat(result).isEqualTo(expResult);
		assertThat(arguments).contains(session, Index.atIndex(0));
		assertThat(arguments).contains(5, Index.atIndex(1));
		assertThat(arguments).contains("toto", Index.atIndex(2));
	}

	/**
	 * Test of getMethodFromDataService method, of class CallServiceManager.
	 *
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test(expected = NoSuchMethodException.class)
	public void testGetMethodFromDataServiceNotFound2() throws NoSuchMethodException {
		System.out.println("getMethodFromDataService");
		Session session = mock(Session.class);
		Class dsClass = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setOperation("methodWith2ArgumentsAndSession");
		message.setParameters(Arrays.asList("\"toto\"", "5"));
		Object[] arguments = new Object[2];
		callServiceManager.getMethodFromDataServiceWithSessionInjection(session, dsClass, message, arguments);
	}

	/**
	 * Test of getMethodFromDataService method, of class CallServiceManager.
	 *
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testGetMethodFromDataServiceWithWithUnmarshaller() throws NoSuchMethodException {
		System.out.println("getMethodFromDataService");
		Class dsClass = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setOperation("methodWithUnmarshaller");
		message.setParameters(Arrays.asList("{\"language\":\"fr\",\"country\":\"FR\"}"));
		Object[] arguments = new Object[1];
		Method expResult = dsClass.getMethod("methodWithUnmarshaller", new Class<?>[]{Locale.class});
		Method result = callServiceManager.getMethodFromDataService(dsClass, message, arguments);
		assertThat(result).isEqualTo(expResult);
		assertThat(arguments).hasSize(1);
		Locale l = (Locale) arguments[0];
		assertThat(l.getCountry()).isEqualTo("FR");
		assertThat(l.getLanguage()).isEqualTo("fr");
	}

	/**
	 * Test of convertArgument method, of class CallServiceManager.
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
			Object result = callServiceManager.convertArgument(arg, type);
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
	 * Test of convertArgument method, of class CallServiceManager.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testConvertArgumentFail() throws IllegalArgumentException {
		System.out.println("convertArgumentFail");
		callServiceManager.convertArgument("toto", String.class);
	}

	/**
	 * Test of getDataService method, of class CallServiceManager.
	 *
	 * @throws org.ocelotds.spi.DataServiceException
	 */
	@Test
	public void testGetDataService() throws DataServiceException {
		System.out.println("getDataService");
		Class cls = ClassAsDataService.class;
		IDataServiceResolver resolver = mock(IDataServiceResolver.class);
		when(resolver.getScope(any(Class.class))).thenReturn(Scope.MANAGED);
		when(resolver.resolveDataService(cls)).thenReturn(new ClassAsDataService());
		doReturn(resolver).when(callServiceManager).getResolver("TEST");
		Session client = mock(Session.class);
		Map<String, Object> userProperties = new HashMap<>();
		when(client.getUserProperties()).thenReturn(userProperties);
		Object result = callServiceManager.getDataService(client, cls);
		assertThat(result).isInstanceOf(cls);
		assertThat(userProperties).doesNotContainKey(cls.getName());
		when(resolver.getScope(any(Class.class))).thenReturn(Scope.SESSION);
		result = callServiceManager.getDataService(client, cls);
		assertThat(result).isInstanceOf(cls);
		assertThat(userProperties).containsKey(cls.getName());
	}

	/**
	 * Test of getDataService method, of class CallServiceManager.
	 *
	 * @throws org.ocelotds.spi.DataServiceException
	 */
	@Test(expected = DataServiceException.class)
	public void testGetDataServiceFail() throws DataServiceException {
		System.out.println("getDataServiceFail");
		Class cls = ClassAsNotDataService.class;
		IDataServiceResolver resolver = mock(IDataServiceResolver.class);
		when(resolver.getScope(any(Class.class))).thenReturn(Scope.MANAGED);
		when(resolver.resolveDataService(cls)).thenReturn(new ClassAsNotDataService());
		doReturn(resolver).when(callServiceManager).getResolver("TEST");
		Session client = mock(Session.class);
		Map<String, Object> userProperties = new HashMap<>();
		when(client.getUserProperties()).thenReturn(userProperties);
		callServiceManager.getDataService(client, cls);
	}

	private class ClassAsNotDataService {
	}

	/**
	 * Test of sendMessageToClient method, of class CallServiceManager.
	 *
	 * @throws org.ocelotds.spi.DataServiceException
	 * @throws java.lang.NoSuchMethodException
	 */
	@Test
	public void testSendMessageToClient() throws DataServiceException, NoSuchMethodException, JsonMarshallingException {
		System.out.println("sendMessageToClient");
		when(logger.isDebugEnabled()).thenReturn(Boolean.TRUE);
		Class cls = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setDataService(cls.getName());
		message.setOperation("methodReturnString");
		message.setParameterNames(Arrays.asList("a"));
		message.setParameters(Arrays.asList("\"v\""));
		message.setId(UUID.randomUUID().toString());
		Session client = mock(Session.class);
		RemoteEndpoint.Async async = mock(RemoteEndpoint.Async.class);
		when(client.getAsyncRemote()).thenReturn(async);

		IDataServiceResolver resolver = mock(IDataServiceResolver.class);
		when(resolver.getScope(any(Class.class))).thenReturn(Scope.MANAGED);
		when(resolver.resolveDataService(cls)).thenReturn(new ClassAsDataService());
		doReturn(resolver).when(callServiceManager).getResolver("TEST");

		callServiceManager.sendMessageToClient(message, client);
		// Method with Session injection
		message.setOperation("methodReturnString2");
		callServiceManager.sendMessageToClient(message, client);

		message.setOperation("methodReturnCachedString");
		callServiceManager.sendMessageToClient(message, client);

		message.setOperation("methodUnknown");
		callServiceManager.sendMessageToClient(message, client);

		message.setOperation("methodThrowException");
		callServiceManager.sendMessageToClient(message, client);

		Mockito.doThrow(NoSuchMethodException.class).when(callServiceManager).getNonProxiedMethod(cls, "methodReturnString", String.class);
		message.setOperation("methodReturnString");
		callServiceManager.sendMessageToClient(message, client);

		message.setOperation("methodWithMarshaller");
		callServiceManager.sendMessageToClient(message, client);

		ArgumentCaptor<MessageToClient> captureMsg = ArgumentCaptor.forClass(MessageToClient.class);
		verify(async, times(7)).sendObject(captureMsg.capture());
		List<MessageToClient> result = captureMsg.getAllValues();
		assertThat(result.get(0).getResponse()).isEqualTo(new ClassAsDataService().methodReturnString("e"));
		assertThat(result.get(1).getResponse()).isEqualTo(new ClassAsDataService().methodReturnString2(client, "e"));
		assertThat(result.get(2).getResponse()).isEqualTo(new ClassAsDataService().methodReturnCachedString("e"));
		Calendar deadline = Calendar.getInstance();
		deadline.add(Calendar.YEAR, 1);
		assertThat(result.get(2).getDeadline()).isCloseTo(deadline.getTime().getTime(), Offset.offset(1000L));
		Fault fault;
		try {
			throw new NoSuchMethodException("class org.ocelotds.core.CallServiceManagerTest$ClassAsDataService.methodUnknown");
		} catch (NoSuchMethodException e) {
			fault = new Fault(e, 0);
		}
		assertThat(result.get(3).getResponse()).isEqualTo(fault);
		try {
			throw new AbstractMethodError("MyMessage");
		} catch (AbstractMethodError e) {
			fault = new Fault(e, 0);
		}
		assertThat(result.get(4).getResponse()).isEqualTo(fault);
		assertThat(result.get(5).getResponse()).isEqualTo(new ClassAsDataService().methodReturnString("e"));
		assertThat(result.get(6).getJson()).isEqualTo(new LocaleMarshaller().toJson(new Locale("fr", "FR")));
	}

	@DataService(resolver = "TEST")
	private class ClassAsDataService {

		public void methodWith2ArgumentsAndSession(Session session, int i, String s) {

		}

		public void methodWithSomeArguments(String s, Integer i, String[] a, Collection<String> c, Map<String, Integer> m) {

		}

		public void methodWith2Arguments(Integer i, String s) {

		}

		public String methodReturnString(String a) {
			return "r1";
		}

		@MethodWithSessionInjection
		public String methodReturnString2(String a) {
			return "r2";
		}

		public String methodReturnString2(Session s, String a) {
			return "r3";
		}

		public String methodThrowException(String a) throws AbstractMethodError {
			throw new AbstractMethodError("MyMessage");
		}

		@JsCacheResult
		public String methodReturnCachedString(String a) {
			return "r5";
		}

		@JsonMarshaller(LocaleMarshaller.class)
		public Locale methodWithMarshaller(String a) {
			return new Locale("fr", "FR");
		}

		public void methodWithUnmarshaller(@JsonUnmarshaller(LocaleUnmarshaller.class) Locale l) {
		}
	}
}
