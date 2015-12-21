/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.services;

import java.lang.reflect.Method;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.Spy;
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
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.ocelotds.resolvers.CdiResolver;
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
	private ArgumentServices argumentServices;

	@Spy
	@InjectMocks
	private CacheManager cacheManager = new CacheManager();

	@Mock
	private Logger logger;

	@Mock
	private MethodServices methodServices;

	@Mock
	private FaultServices faultServices;

	@Spy
	@InjectMocks
	private CallServiceManager instance;

	/**
	 * Test of getResolver method, of class CallServiceManager.
	 */
	@Test
	public void testGetResolver() {
		Instance<IDataServiceResolver> inst = mock(Instance.class);
		when(resolvers.select(eq(new DataServiceResolverIdLitteral("cdi")))).thenReturn(inst);
		when(inst.get()).thenReturn(new CdiResolver());
		IDataServiceResolver result = instance.getResolver("cdi");
		assertThat(result).isInstanceOf(CdiResolver.class);
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
	 * Test of createMessageToClient method, of class CallServiceManager.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateMessageToClient() throws Exception {
		String methodname = "methodReturnString";
		System.out.println("createMessageToClient("+methodname+")");
		Class cls = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setDataService(cls.getName());
		message.setOperation(methodname);
		message.setParameterNames(Arrays.asList("a"));
		message.setParameters(Arrays.asList("\"v\""));
		message.setId(UUID.randomUUID().toString());
		Session client = mock(Session.class);
		Method method = cls.getMethod(methodname, String.class);

		ClassAsDataService obj = new ClassAsDataService();
		
		doReturn(obj).when(instance).getDataService(any(Session.class), any(Class.class));
		doReturn(Arrays.asList("v")).when(instance).getArrayList();
		when(methodServices.getMethodFromDataService(any(Class.class), any(MessageFromClient.class), anyList())).thenReturn(method);
		when(methodServices.getNonProxiedMethod(any(Class.class), anyString(), any(Class[].class))).thenReturn(method);
		
		MessageToClient result = instance.createMessageToClient(message, client);

		assertThat(result.getResponse()).isEqualTo(obj.methodReturnString("v"));
	}

	/**
	 * Test of createMessageToClient method, of class CallServiceManager.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateMessageToClient2() throws Exception {
		String methodname = "methodReturnString2";
		System.out.println("createMessageToClient("+methodname+")");
		Class cls = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setDataService(cls.getName());
		message.setOperation(methodname);
		message.setParameterNames(Arrays.asList("a"));
		message.setParameters(Arrays.asList("\"v\""));
		message.setId(UUID.randomUUID().toString());
		Session client = mock(Session.class);
		Method method = cls.getMethod(methodname, String.class);

		ClassAsDataService obj = new ClassAsDataService();
		
		doReturn(obj).when(instance).getDataService(any(Session.class), any(Class.class));
		doReturn(Arrays.asList("v")).when(instance).getArrayList();
		when(methodServices.getMethodFromDataService(any(Class.class), any(MessageFromClient.class), anyList())).thenReturn(method);
		when(methodServices.getNonProxiedMethod(any(Class.class), anyString(), any(Class[].class))).thenThrow(NoSuchMethodException.class);
		
		MessageToClient result = instance.createMessageToClient(message, client);

		assertThat(result.getResponse()).isEqualTo(obj.methodReturnString2("v"));

		ArgumentCaptor<String> captureLog = ArgumentCaptor.forClass(String.class);
		verify(logger).error(captureLog.capture(), any(NoSuchMethodException.class));
		assertThat(captureLog.getValue()).isEqualTo("Fail to process extra annotations (JsCacheResult, JsCacheRemove) for method : "+methodname);
	}

	/**
	 * Test of createMessageToClient method, of class CallServiceManager.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateMessageToClient3() throws Exception {
		String methodname = "methodWithMarshaller";
		System.out.println("createMessageToClient("+methodname+")");
		Class cls = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setDataService(cls.getName());
		message.setOperation(methodname);
		message.setParameterNames(Arrays.asList("a"));
		message.setParameters(Arrays.asList("\"v\""));
		message.setId(UUID.randomUUID().toString());
		Session client = mock(Session.class);
		Method method = cls.getMethod(methodname, String.class);

		ClassAsDataService obj = new ClassAsDataService();
		
		doReturn(obj).when(instance).getDataService(any(Session.class), any(Class.class));
		doReturn(Arrays.asList("v")).when(instance).getArrayList();
		when(methodServices.getMethodFromDataService(any(Class.class), any(MessageFromClient.class), anyList())).thenReturn(method);
		when(methodServices.getNonProxiedMethod(any(Class.class), anyString(), any(Class[].class))).thenReturn(method);
		when(argumentServices
				  .getJsonResultFromSpecificMarshaller(any(JsonMarshaller.class), any(Object.class)))
				  .thenReturn("{\"language\":\"fr\",\"country\":\"FR\"}");
		
		MessageToClient result = instance.createMessageToClient(message, client);

		assertThat(result.getJson()).isEqualTo("{\"language\":\"fr\",\"country\":\"FR\"}");
	}

	/**
	 * Test of createMessageToClient method, of class CallServiceManager.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateMessageToClient4() throws Exception {
		String methodname = "methodThrowException";
		System.out.println("createMessageToClient("+methodname+")");
		Class cls = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setDataService(cls.getName());
		message.setOperation(methodname);
		message.setParameterNames(Arrays.asList("a"));
		message.setParameters(Arrays.asList("\"v\""));
		message.setId(UUID.randomUUID().toString());
		Session client = mock(Session.class);
		Method method = cls.getMethod(methodname, String.class);
		Fault fault = null;
		try {
			throw new AbstractMethodError("MyMessage");
		} catch (AbstractMethodError e) {
			fault = new Fault(e, 0);
		}

		ClassAsDataService obj = new ClassAsDataService();
		
		doReturn(obj).when(instance).getDataService(any(Session.class), any(Class.class));
		doReturn(Arrays.asList("v")).when(instance).getArrayList();
		when(methodServices.getMethodFromDataService(any(Class.class), any(MessageFromClient.class), anyList())).thenReturn(method);
		when(methodServices.getNonProxiedMethod(any(Class.class), anyString(), any(Class[].class))).thenReturn(method);
		when(faultServices.buildFault(any(Throwable.class))).thenReturn(fault);
		
		MessageToClient result = instance.createMessageToClient(message, client);
		assertThat(result.getResponse()).isEqualTo(fault);
	}

	/**
	 * Test of createMessageToClient method, of class CallServiceManager.
	 *
	 * @throws org.ocelotds.spi.DataServiceException
	 * @throws java.lang.NoSuchMethodException
	 * @throws org.ocelotds.marshalling.exceptions.JsonMarshallingException
	 */
//	@Test
	public void testCreateMessageToClient02() throws DataServiceException, NoSuchMethodException, JsonMarshallingException {
		System.out.println("createMessageToClient");
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

		doThrow(NoSuchMethodException.class).when(methodServices).getNonProxiedMethod(cls, "methodReturnString", String.class);
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
}
