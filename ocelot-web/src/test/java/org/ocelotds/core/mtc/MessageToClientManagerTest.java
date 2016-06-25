/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.core.mtc;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.enterprise.inject.Instance;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolationException;
import javax.websocket.Session;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.cache.CacheManager;
import org.ocelotds.core.services.ArgumentServices;
import org.ocelotds.core.services.ClassAsDataService;
import org.ocelotds.core.services.ConstraintServices;
import org.ocelotds.core.services.FaultServices;
import org.ocelotds.core.services.MethodServices;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.messaging.Fault;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.resolvers.CdiResolver;
import org.ocelotds.resolvers.DataServiceResolverIdLitteral;
import org.ocelotds.spi.DataServiceException;
import org.ocelotds.spi.IDataServiceResolver;
import org.ocelotds.spi.Scope;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageToClientManagerTest {


	@Mock
	private Logger logger;

	@Mock
	private Instance<IDataServiceResolver> resolvers;

	@Mock
	private CacheManager cacheManager;

	@Mock
	private MethodServices methodServices;

	@Mock
	private ArgumentServices argumentServices;

	@Mock
	private FaultServices faultServices;
	
	@Mock
	private ConstraintServices constraintServices;

	@Spy
	@InjectMocks
	private MessageToClientManager instance = new MessageToClientManager() {
		@Override
		public Map<String, Object> getSessionBeans(Object session) {
			return new HashMap<>();
		}
	};

	/**
	 * Test of getResolver method, of class WSMessageToClientManager.
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
	 * Test of getDataService method, of class WSMessageToClientManager.
	 *
	 * @throws org.ocelotds.spi.DataServiceException
	 */
	@Test
	public void test_GetDataService() throws Exception {
		System.out.println("_getDataService");
		Class cls = ClassAsDataService.class;
		IDataServiceResolver resolver = mock(IDataServiceResolver.class);
		Session session = mock(Session.class);
		Map<String, Object> sessions = new HashMap<>();
		ClassAsDataService ds1 = new ClassAsDataService();
		ClassAsDataService ds2 = new ClassAsDataService();
		ClassAsDataService ds3 = new ClassAsDataService();
		when(resolver.getScope(any(Class.class))).thenReturn(Scope.MANAGED).thenReturn(Scope.SESSION);
		when(resolver.resolveDataService(cls)).thenReturn(ds1).thenReturn(ds2).thenReturn(ds3);
		doReturn(sessions).when(instance).getSessionBeans(session);
		doReturn(resolver).when(instance).getResolver("TEST");

		// normal scope
		Object result = instance._getDataService(session, cls);
		assertThat(result).isEqualTo(ds1);
		assertThat(sessions).doesNotContainKey(cls.getName());

		// session scope
		result = instance._getDataService(session, cls);
		assertThat(result).isEqualTo(ds2);
		assertThat(sessions).containsKey(cls.getName());
		result = instance._getDataService(session, cls);
		assertThat(result).isEqualTo(ds2);
		assertThat(sessions).containsKey(cls.getName());

		// no session
		result = instance._getDataService(null, cls);
		assertThat(result).isEqualTo(ds3);
	}

	@Test
	public void testGetDataService() throws Exception {
		System.out.println("getDataService");
		Class cls = ClassAsDataService.class;
		Session session = mock(Session.class);
		ClassAsDataService expected = new ClassAsDataService();
		
		doReturn(expected).when(instance)._getDataService(any(Session.class), any(Class.class));

		Object result = instance.getDataService(session, cls);
		
		assertThat(result).isEqualTo(expected);
	}
	/**
	 * Test of getDataService method, of class WSMessageToClientManager.
	 *
	 * @throws org.ocelotds.spi.DataServiceException
	 */
	@Test(expected = DataServiceException.class)
	public void testGetDataServiceFail() throws DataServiceException {
		System.out.println("getDataServiceFail");
		Class cls = ClassAsNotDataService.class;
		Session session = mock(Session.class);
		
		instance.getDataService(session, cls);
	}

	/**
	 * Test of getDataService method, of class WSMessageToClientManager.
	 *
	 * @throws org.ocelotds.spi.DataServiceException
	 */
	@Test(expected = DataServiceException.class)
	public void testGetDataServiceFail2() throws DataServiceException, Exception {
		System.out.println("getDataServiceFail2");
		Class cls = ClassAsDataService.class;
		Session session = mock(Session.class);
		
		doThrow(DataServiceException.class).when(instance)._getDataService(any(Session.class), any(Class.class));

		instance.getDataService(session, cls);
	}

	private class ClassAsNotDataService {
	}

	/**
	 * Test of _createMessageToClient method, of class WSMessageToClientManager.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateMessageToClient() throws Exception {
		String methodname = "methodReturnString";
		System.out.println("_createMessageToClient("+methodname+")");
		Class cls = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setDataService(cls.getName());
		message.setOperation(methodname);
		message.setParameters(Arrays.asList("\"v\""));
		message.setId(UUID.randomUUID().toString());
		Session session = mock(Session.class);
		Method method = cls.getMethod(methodname, String.class);

		ClassAsDataService obj = new ClassAsDataService();
		
		doReturn(obj).when(instance).getDataService(any(Session.class), any(Class.class));
		doReturn(Arrays.asList("v")).when(instance).getArrayList();
		when(methodServices.getMethodFromDataService(any(Class.class), any(MessageFromClient.class), anyList())).thenReturn(method);
		doNothing().when(instance).injectSession(any(Class[].class), anyList(), anyObject());
		when(methodServices.getNonProxiedMethod(any(Class.class), anyString(), any(Class[].class))).thenReturn(method);
		
		MessageToClient result = instance.createMessageToClient(message, session);

		assertThat(result.getResponse()).isEqualTo(obj.methodReturnString("v"));
	}

	/**
	 * Test of _createMessageToClient method, of class WSMessageToClientManager.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateMessageToClient2() throws Exception {
		String methodname = "methodReturnString2";
		System.out.println("_createMessageToClient("+methodname+")");
		Class cls = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setDataService(cls.getName());
		message.setOperation(methodname);
		message.setParameters(Arrays.asList("\"v\""));
		message.setId(UUID.randomUUID().toString());
		Session session = mock(Session.class);
		Method method = cls.getMethod(methodname, String.class);

		ClassAsDataService obj = new ClassAsDataService();
		
		doReturn(obj).when(instance).getDataService(any(Session.class), any(Class.class));
		doReturn(Arrays.asList("v")).when(instance).getArrayList();
		when(methodServices.getMethodFromDataService(any(Class.class), any(MessageFromClient.class), anyList())).thenReturn(method);
		doNothing().when(instance).injectSession(any(Class[].class), anyList(), anyObject());
		when(methodServices.getNonProxiedMethod(any(Class.class), anyString(), any(Class[].class))).thenThrow(NoSuchMethodException.class);
		
		MessageToClient result = instance.createMessageToClient(message, session);

		assertThat(result.getResponse()).isEqualTo(obj.methodReturnString2("v"));

		ArgumentCaptor<String> captureLog = ArgumentCaptor.forClass(String.class);
		verify(logger).error(captureLog.capture(), any(NoSuchMethodException.class));
		assertThat(captureLog.getValue()).isEqualTo("Fail to process extra annotations (JsCacheResult, JsCacheRemove) for method : "+methodname);
	}

	/**
	 * Test of _createMessageToClient method, of class WSMessageToClientManager.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateMessageToClient3() throws Exception {
		String methodname = "methodWithMarshaller";
		System.out.println("_createMessageToClient("+methodname+")");
		Class cls = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setDataService(cls.getName());
		message.setOperation(methodname);
		message.setParameters(Arrays.asList("\"v\""));
		message.setId(UUID.randomUUID().toString());
		Session session = mock(Session.class);
		Method method = cls.getMethod(methodname, String.class);

		ClassAsDataService obj = new ClassAsDataService();
		
		doReturn(obj).when(instance).getDataService(any(Session.class), any(Class.class));
		doReturn(Arrays.asList("v")).when(instance).getArrayList();
		when(methodServices.getMethodFromDataService(any(Class.class), any(MessageFromClient.class), anyList())).thenReturn(method);
		doNothing().when(instance).injectSession(any(Class[].class), anyList(), anyObject());
		when(methodServices.getNonProxiedMethod(any(Class.class), anyString(), any(Class[].class))).thenReturn(method);
		when(argumentServices
				  .getJsonResultFromSpecificMarshaller(any(JsonMarshaller.class), any(Object.class)))
				  .thenReturn("{\"language\":\"fr\",\"country\":\"FR\"}");
		
		MessageToClient result = instance.createMessageToClient(message, session);

		assertThat(result.getJson()).isEqualTo("{\"language\":\"fr\",\"country\":\"FR\"}");
	}

	/**
	 * Test of _createMessageToClient method, of class WSMessageToClientManager.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateMessageToClient4() throws Exception {
		String methodname = "methodThrowException";
		System.out.println("_createMessageToClient("+methodname+")");
		Class cls = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setDataService(cls.getName());
		message.setOperation(methodname);
		message.setParameters(Arrays.asList("\"v\""));
		message.setId(UUID.randomUUID().toString());

		Session session = mock(Session.class);
		Method method = cls.getMethod(methodname, String.class);
		Fault fault = mock(Fault.class);

		ClassAsDataService obj = new ClassAsDataService();
		
		doReturn(obj).when(instance).getDataService(any(Session.class), any(Class.class));
		doReturn(Arrays.asList("v")).when(instance).getArrayList();
		when(methodServices.getMethodFromDataService(any(Class.class), any(MessageFromClient.class), anyList())).thenReturn(method);
		doNothing().when(instance).injectSession(any(Class[].class), anyList(), anyObject());
		when(faultServices.buildFault(any(Throwable.class))).thenReturn(fault);
		
		MessageToClient result = instance.createMessageToClient(message, session);
		assertThat(result.getResponse()).isEqualTo(fault);
	}

	/**
	 * Test of _createMessageToClient method, of class WSMessageToClientManager.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateMessageToClient5() throws Exception {
		String methodname = "methodReturnString";
		System.out.println("_createMessageToClient("+methodname+") Failed");
		Session session = mock(Session.class);
		Fault fault = mock(Fault.class);
		Class cls = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setDataService("BadClass");
		when(faultServices.buildFault(any(Throwable.class))).thenReturn(fault);
		
		MessageToClient result = instance.createMessageToClient(message, session);

		assertThat(result.getResponse()).isEqualTo(fault);
	}

	/**
	 * Test of _createMessageToClient method, of class WSMessageToClientManager.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateMessageToClient6() throws Exception {
		String methodname = "methodThrowViolationConstraint";
		System.out.println("_createMessageToClient("+methodname+")");
		Class cls = ClassAsDataService.class;
		MessageFromClient message = new MessageFromClient();
		message.setDataService(cls.getName());
		message.setOperation(methodname);
		message.setParameters(Arrays.asList("\"v\""));
		message.setId(UUID.randomUUID().toString());

		Session session = mock(Session.class);
		Method method = cls.getMethod(methodname, String.class);
		Fault fault = mock(Fault.class);

		ClassAsDataService obj = new ClassAsDataService();
		
		doReturn(obj).when(instance).getDataService(any(Session.class), any(Class.class));
		doReturn(Arrays.asList("v")).when(instance).getArrayList();
		when(methodServices.getMethodFromDataService(any(Class.class), any(MessageFromClient.class), anyList())).thenReturn(method);
		doNothing().when(instance).injectSession(any(Class[].class), anyList(), anyObject());
		when(faultServices.buildFault(any(Throwable.class))).thenReturn(fault);
		when(constraintServices.extractViolations(any(ConstraintViolationException.class))).thenReturn(null);
		
		MessageToClient result = instance.createMessageToClient(message, session);
		verify(constraintServices).extractViolations(any(ConstraintViolationException.class));
	}
	
	/**
	 * Test of injectSession method, of class.
	 */
	@Test
	public void injectSessionTest() {
		System.out.println("injectSession");
		Class<?>[] parameterTypes0 = new Class<?>[] {};
		Class<?>[] parameterTypes1 = new Class<?>[] {String.class};
		Class<?>[] parameterTypes2 = new Class<?>[] {String.class, Session.class};
		Class<?>[] parameterTypes3 = new Class<?>[] {HttpSession.class, String.class};
		List<Object> arguments = Arrays.asList(null, null); 
		Object session = mock(Session.class);
		HttpSession httpSession = mock(HttpSession.class);
		instance.injectSession(null, arguments, null);
		assertThat(arguments.get(0)).isNull();
		assertThat(arguments.get(1)).isNull();
		arguments = Arrays.asList(null, null); 

		instance.injectSession(parameterTypes0, arguments, session);
		assertThat(arguments.get(0)).isNull();
		assertThat(arguments.get(1)).isNull();
		arguments = Arrays.asList(null, null); 
		
		instance.injectSession(parameterTypes0, arguments, httpSession);
		assertThat(arguments.get(0)).isNull();
		assertThat(arguments.get(1)).isNull();
		arguments = Arrays.asList(null, null); 

		instance.injectSession(parameterTypes1, arguments, session);
		assertThat(arguments.get(0)).isNull();
		assertThat(arguments.get(1)).isNull();
		arguments = Arrays.asList(null, null); 
		
		instance.injectSession(parameterTypes1, arguments, httpSession);
		assertThat(arguments.get(0)).isNull();
		assertThat(arguments.get(1)).isNull();
		arguments = Arrays.asList(null, null); 

		instance.injectSession(parameterTypes2, arguments, session);
		assertThat(arguments.get(0)).isNull();
		assertThat(arguments.get(1)).isEqualTo(session);
		arguments = Arrays.asList(null, null); 

		instance.injectSession(parameterTypes2, arguments, httpSession);
		assertThat(arguments.get(0)).isNull();
		assertThat(arguments.get(1)).isNull();
		arguments = Arrays.asList(null, null); 

		instance.injectSession(parameterTypes3, arguments, session);
		assertThat(arguments.get(0)).isNull();
		assertThat(arguments.get(1)).isNull();
		arguments = Arrays.asList(null, null); 

		instance.injectSession(parameterTypes3, arguments, httpSession);
		assertThat(arguments.get(0)).isEqualTo(httpSession);
		assertThat(arguments.get(1)).isNull();
	}

	/**
	 * Test of getArrayList method, of class WSMessageToClientManager.
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetArrayList() throws Exception {
		System.out.println("getArrayList");
		List<Object> arrayList = instance.getArrayList();
		assertThat(arrayList).isNotNull();
	}
}
