/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.services;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.enterprise.inject.Instance;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import org.ocelotds.messaging.MessageFromClient;
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
import org.ocelotds.configuration.OcelotConfiguration;
import org.ocelotds.messaging.Fault;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.resolvers.DataServiceResolverIdLitteral;
import org.ocelotds.spi.DataServiceException;
import org.ocelotds.spi.IDataServiceResolver;
import org.ocelotds.spi.Scope;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.ocelotds.Constants;
import org.ocelotds.core.CacheManager;
import org.ocelotds.marshallers.LocaleMarshaller;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;
import org.ocelotds.resolvers.PojoResolver;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class CallServiceManagerTest {

	@Mock
	private Instance<IDataServiceResolver> resolvers;

	@Mock
	private OcelotConfiguration configuration;

	@Mock
	private ArgumentConvertor argumentsServices;

	@Spy
	@InjectMocks
	private CacheManager cacheManager = new CacheManager();

	@Mock
	private Logger logger;

	@Spy
	@InjectMocks
	private CallServiceManager instance;

	@Before
	public void init() {
		when(configuration.getStacktracelength()).thenReturn(0);
	}

	/**
	 * Test of getResolver method, of class CallServiceManager.
	 */
	@Test
	public void testGetResolver() {
		Instance<IDataServiceResolver> inst = mock(Instance.class);
		when(resolvers.select(eq(new DataServiceResolverIdLitteral("pojo")))).thenReturn(inst);
		when(inst.get()).thenReturn(new PojoResolver());
		IDataServiceResolver result = instance.getResolver("pojo");
		assertThat(result).isInstanceOf(PojoResolver.class);
	}

	/**
	 * Test of getMethodFromDataService method, of class CallServiceManager.
	 *
	 * @throws java.lang.NoSuchMethodException
	 * @throws org.ocelotds.marshalling.exceptions.JsonUnmarshallingException
	 */
	@Test
	public void testGetMethodFromDataService() throws NoSuchMethodException, JsonUnmarshallingException {
		System.out.println("getMethodFromDataService");
		Class dsClass = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setOperation("methodWith2Arguments");
		message.setParameters(Arrays.asList("5", "\"toto\""));
		Object[] arguments = new Object[2];
		Method expResult = dsClass.getMethod("methodWith2Arguments", new Class<?>[]{Integer.class, String.class});

		when(argumentsServices.convertJsonToJava(eq("5"), any(Type.class), any(Annotation[].class))).thenReturn(5);
		when(argumentsServices.convertJsonToJava(eq("\"toto\""), any(Type.class), any(Annotation[].class))).thenReturn("toto");

		Method result = instance.getMethodFromDataService(dsClass, message, arguments);
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getMethodFromDataService method, of class CallServiceManager.
	 *
	 * @throws java.lang.NoSuchMethodException
	 * @throws org.ocelotds.marshalling.exceptions.JsonUnmarshallingException
	 */
	@Test(expected = NoSuchMethodException.class)
	public void testGetMethodFromDataServiceNotFound() throws NoSuchMethodException, JsonUnmarshallingException {
		System.out.println("getMethodFromDataService");
		Class dsClass = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setOperation("methodWith2Arguments");
		message.setParameters(Arrays.asList("\"toto\"", "5"));
		Object[] arguments = new Object[2];
		
		when(argumentsServices.convertJsonToJava(anyString(), any(Type.class), any(Annotation[].class))).thenThrow(JsonUnmarshallingException.class);

		instance.getMethodFromDataService(dsClass, message, arguments);
	}

	/**
	 * Test of getMethodFromDataService method, of class CallServiceManager.
	 *
	 * @throws java.lang.NoSuchMethodException
	 * @throws org.ocelotds.marshalling.exceptions.JsonUnmarshallingException
	 */
	@Test
	public void testGetMethodFromDataServiceWithWithUnmarshaller() throws NoSuchMethodException, JsonUnmarshallingException {
		System.out.println("getMethodFromDataService");
		Class dsClass = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setOperation("methodWithUnmarshaller");
		String json = "{\"language\":\"fr\",\"country\":\"FR\"}";
		message.setParameters(Arrays.asList(json));
		Object[] arguments = new Object[1];
		Method expResult = dsClass.getMethod("methodWithUnmarshaller", new Class<?>[]{Locale.class});
		
		when(argumentsServices.convertJsonToJava(eq(json), any(Type.class), any(Annotation[].class))).thenReturn(new Locale("fr", "FR"));
		
		Method result = instance.getMethodFromDataService(dsClass, message, arguments);
		assertThat(result).isEqualTo(expResult);
		assertThat(arguments).hasSize(1);
		Locale l = (Locale) arguments[0];
		assertThat(l.getCountry()).isEqualTo("FR");
		assertThat(l.getLanguage()).isEqualTo("fr");
	}

	/**
	 * Test of getDataService method, of class CallServiceManager.
	 *
	 * @throws org.ocelotds.spi.DataServiceException
	 */
	@Test
	public void test_GetDataService() throws Exception {
		System.out.println("_getDataService");
		Class cls = ClassAsDataService.class;
		IDataServiceResolver resolver = mock(IDataServiceResolver.class);
		Session client = mock(Session.class);
		Map<String, Object> userProperties = new HashMap<>();
		Map<String, Object> sessions = new HashMap<>();
		userProperties.put(Constants.SESSION_BEANS, sessions);
		
		when(resolver.getScope(any(Class.class))).thenReturn(Scope.MANAGED).thenReturn(Scope.SESSION);
		when(resolver.resolveDataService(cls)).thenReturn(new ClassAsDataService());
		doReturn(resolver).when(instance).getResolver("TEST");
		when(client.getUserProperties()).thenReturn(userProperties);

		Object result = instance._getDataService(client, cls);
		assertThat(result).isInstanceOf(cls);
		assertThat(sessions).doesNotContainKey(cls.getName());

		result = instance._getDataService(client, cls);
		assertThat(result).isInstanceOf(cls);
		assertThat(sessions).containsKey(cls.getName());
	}

	@Test
	public void testGetDataService() throws Exception {
		System.out.println("getDataService");
		Class cls = ClassAsDataService.class;
		Session client = mock(Session.class);
		ClassAsDataService expected = new ClassAsDataService();
		
		doReturn(expected).when(instance)._getDataService(any(Session.class), any(Class.class));

		Object result = instance.getDataService(client, cls);
		
		assertThat(result).isEqualTo(expected);
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
		Session client = mock(Session.class);
		
		instance.getDataService(client, cls);
	}

	/**
	 * Test of getDataService method, of class CallServiceManager.
	 *
	 * @throws org.ocelotds.spi.DataServiceException
	 */
	@Test(expected = DataServiceException.class)
	public void testGetDataServiceFail2() throws DataServiceException, Exception {
		System.out.println("getDataServiceFail2");
		Class cls = ClassAsDataService.class;
		Session client = mock(Session.class);
		
		doThrow(Exception.class).when(instance)._getDataService(any(Session.class), any(Class.class));

		instance.getDataService(client, cls);
	}

	private class ClassAsNotDataService {
	}

	/**
	 * Test of sendMessageToClient method, of class CallServiceManager.
	 *
	 * @throws org.ocelotds.spi.DataServiceException
	 * @throws java.lang.NoSuchMethodException
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
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
		doReturn(resolver).when(instance).getResolver("TEST");

		instance.sendMessageToClient(message, client);
		// Method with Session injection
		message.setOperation("methodReturnString2");
		instance.sendMessageToClient(message, client);

		message.setOperation("methodReturnCachedString");
		instance.sendMessageToClient(message, client);

		message.setOperation("methodUnknown");
		instance.sendMessageToClient(message, client);

		message.setOperation("methodThrowException");
		instance.sendMessageToClient(message, client);

		Mockito.doThrow(NoSuchMethodException.class).when(instance).getNonProxiedMethod(cls, "methodReturnString", String.class);
		message.setOperation("methodReturnString");
		instance.sendMessageToClient(message, client);

		message.setOperation("methodWithMarshaller");
		instance.sendMessageToClient(message, client);

		ArgumentCaptor<MessageToClient> captureMsg = ArgumentCaptor.forClass(MessageToClient.class);
		verify(async, times(7)).sendObject(captureMsg.capture());
		List<MessageToClient> result = captureMsg.getAllValues();
		assertThat(result.get(0).getResponse()).isEqualTo(new ClassAsDataService().methodReturnString("e"));
		assertThat(result.get(1).getResponse()).isEqualTo(new ClassAsDataService().methodReturnString2("e"));
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
	
	
	@Test
	public void testBuildFault() {
		System.out.println("buildFault");
		when(configuration.getStacktracelength()).thenReturn(1).thenReturn(3);
		try {
			throw new Exception("ERROR_MESSAGE");
		} catch (Exception e) {
			Fault fault = instance.buildFault(e);
			assertThat(fault.getClassname()).isEqualTo("java.lang.Exception");
			assertThat(fault.getMessage()).isEqualTo("ERROR_MESSAGE");
			String[] stacktraces = fault.getStacktrace();
			assertThat(stacktraces).hasSize(1);
			assertThat(stacktraces[0]).startsWith(this.getClass().getName()+".testBuildFault("+this.getClass().getSimpleName()+".java:");

			fault = instance.buildFault(e);
			stacktraces = fault.getStacktrace();
			assertThat(stacktraces).hasSize(3);
		}
	}
}
