/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.ejb.EJBAccessException;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.inject.Inject;
import javax.websocket.Session;
import javax.ws.rs.client.Client;
import static org.assertj.core.api.Assertions.assertThat;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocelotds.Constants;
import org.ocelotds.FileNameProvider;
import org.ocelotds.OcelotServices;
import org.ocelotds.integration.dataservices.cache.CacheDataService;
import org.ocelotds.integration.dataservices.cdi.RequestCdiDataService;
import org.ocelotds.integration.dataservices.cdi.SessionCdiDataService;
import org.ocelotds.integration.dataservices.cdi.SingletonCdiDataService;
import org.ocelotds.integration.dataservices.ejb.RequestEjbDataService;
import org.ocelotds.integration.dataservices.ejb.SessionEjbDataService;
import org.ocelotds.integration.dataservices.ejb.SingletonEjbDataService;
import org.ocelotds.integration.dataservices.locale.LocaleMsgDataService;
import org.ocelotds.integration.dataservices.topic.MyTopicAccessControler;
import org.ocelotds.integration.dataservices.types.ArgumentTypeDataService;
import org.ocelotds.integration.dataservices.types.ReturnTypeDataService;
import org.ocelotds.messaging.MessageEvent;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.objects.Result;
import org.ocelotds.resolvers.CdiResolver;
import org.ocelotds.resolvers.DataServiceResolverIdLitteral;
import org.ocelotds.spi.IDataServiceResolver;
import org.ocelotds.annotations.JsTopicAccessControl;
import org.ocelotds.integration.dataservices.monitor.MonitorDataService;
import org.ocelotds.integration.dataservices.topic.TopicAccessControler;
import org.ocelotds.integration.dataservices.topic.TopicDataService;
import org.ocelotds.security.JsTopicAccessController;
import static org.assertj.core.api.Assertions.fail;
import org.ocelotds.integration.dataservices.security.AccessCDIDataService;
import org.ocelotds.integration.dataservices.security.AccessEJBDataService;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.messaging.MessageType;

/**
 *
 * @author hhfrancois
 */
@RunWith(Arquillian.class)
public class OcelotTest extends AbstractOcelotTest {

	final int NB_SIMUL_METHODS = 200;

	@Inject
	@MessageEvent
	Event<MessageToClient> wsEvent;

	@Inject
	@JsTopicAccessControl("mytopic")
	JsTopicAccessController myTopicAccessControler;

	@Inject
	JsTopicAccessController topicAccessControler;

	@Inject
	@Any
	private ReturnTypeDataService returnTypeDataService;

	@Inject
	@Any
	private ArgumentTypeDataService argumentTypeDataService;

	@Inject
	@Any
	private Instance<FileNameProvider> jsProviders;

	@Inject
	@Any
	private Instance<IDataServiceResolver> resolvers;

	private IDataServiceResolver getResolver(String type) {
		return resolvers.select(new DataServiceResolverIdLitteral(type)).get();
	}

	@Deployment
	public static WebArchive createWarGlassfishArchive() {
		return createWarArchive();
	}

	/**
	 * Test that provider return name of js
	 *
	 */
	@Test
	public void testJsServiceProvider() {
		System.out.println("testJsServiceProvider");
		for (FileNameProvider provider : jsProviders) {
			Package aPackage = provider.getClass().getPackage();
			try {
				String filename = provider.getFilename();
				assertThat(filename).isEqualTo(aPackage.getName() + Constants.JS);
			} catch (IllegalAccessError ex) {
			}
		}
	}

	/**
	 * Check minification javascripts
	 *
	 */
	//@Test
	public void testJavascriptCoreMinification() {
		System.out.println("testJavascriptCoreMinification");
		String resource = Constants.OCELOT + Constants.JS;
		HttpURLConnection connection1 = null;
		HttpURLConnection connection2 = null;
		try {
			connection1 = getConnectionForResource(resource, true, false);
			int minlength = countByte(connection1.getInputStream());
			connection2 = getConnectionForResource(resource, false, false);
			int length = countByte(connection2.getInputStream());
			assertThat(minlength).isLessThan(length).as("Minification of %s didn't work, same size of file magnifier : %s / minifer : %s", resource, length, minlength);
		} catch (IOException e) {
			fail(e.getMessage());
		} finally {
			if (connection1 != null) {
				connection1.disconnect();
			}
			if (connection2 != null) {
				connection2.disconnect();
			}
		}
	}

	/**
	 * Check that ocelot-core.js is contextpath replaced
	 */
	@Test
	public void testJavascriptGeneration() {
		System.out.println("testJavascriptCoreGeneration");
		try {
			HttpURLConnection connection = getConnectionForResource(Constants.OCELOT + Constants.JS, false, false);
			try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), Constants.UTF_8))) {
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					assertThat(inputLine).doesNotContain(Constants.CTXPATH).as("Dynamic replacement of %s doesn't work", Constants.CTXPATH);
				}
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test bad resolver
	 */
	@Test(expected = UnsatisfiedResolutionException.class)
	public void testDataServiceExceptionOnUnknownResolver() {
		System.out.println("failResolveDataService");
		getResolver("foo");
	}

	/**
	 * Test resolver is resolved
	 */
	@Test
	public void testGetResolver() {
		System.out.println("getResolver");
		IDataServiceResolver resolver = getResolver(Constants.Resolver.CDI);
		assertThat(resolver).isNotNull();
		assertThat(resolver).isInstanceOf(CdiResolver.class);
		resolver = getResolver(Constants.Resolver.EJB);
		assertThat(resolver).isNotNull();
	}

	/**
	 * Test 2 call in differents sessions and differents threads for request scope : results must be differents
	 */
	@Test
	public void testRequestScopeCdi() {
		System.out.println("testRequestScope");
		testResultRequestScope(RequestCdiDataService.class);
	}

	@Test
	public void testRequestScopeEjb() {
		System.out.println("testRequestScope");
		testResultRequestScope(RequestEjbDataService.class);
	}

	/**
	 * Test 2 call in same session for session scope : result must be equals call 1 more in different session : result must be different
	 */
	@Test
	public void testSessionScopeCdi() {
		System.out.println("testSessionScope");
		testResultSessionScope(SessionCdiDataService.class);
	}

	@Test
	public void testSessionScopeEjb() {
		System.out.println("testSessionScope");
		testResultSessionScope(SessionEjbDataService.class);
	}

	/**
	 * Test 2 call in same session for session scope : result must be equals call 1 more in different session : result must be equals
	 */
	@Test
	public void testSingletonScopeCdi() {
		System.out.println("testSingletonScope");
		testResultSingletonScope(SingletonCdiDataService.class);
	}

	@Test
	public void testSingletonScopeEjb() {
		System.out.println("testSingletonScope");
		testResultSingletonScope(SingletonEjbDataService.class);
	}

	/**
	 * Test multi call in different sessions
	 */
	@Test
	public void testCallMultiMethodsMultiSessions() {
		final Client client = null;
		testCallMultiMethodsInClient("testCallMultiMethodsMultiSessions", NB_SIMUL_METHODS, client);
	}


	/**
	 * Test multi call in same session
	 */
	@Test
	public void testCallMultiMethodsMonoSessions() {
		final Client client = getClient();
		testCallMultiMethodsInClient("testCallMultiMethodsMonoSessions", NB_SIMUL_METHODS, client);
	}

	/**
	 * OCELOTSERVICES
	 */
	/**
	 * Check service getServices return not empty list
	 */
	@Test
	public void testGetServices() {
		Class clazz = OcelotServices.class;
		String methodName = "getServices";
		System.out.println(clazz + "." + methodName);
		testRSCallWithoutResult(clazz, methodName);
	}

	/**
	 * Check service getUsername
	 */
	@Test
	public void testGetUsername() {
		testRSCallWithResult(OcelotServices.class, "getUsername", getJson("user"));
	}

	/**
	 * Check service getLocale/setLocale
	 */
	@Test
	public void testGetSetLocale() {
		Client client = getClient();
		// default locale is US
		testRSCallWithResult(client, OcelotServices.class, "getLocale", "{\"country\":\"US\",\"language\":\"en\"}");
		// switch to French
		testRSCallWithoutResult(client, OcelotServices.class, "setLocale", "{\"country\":\"FR\",\"language\":\"fr\"}");
		// check
		testRSCallWithResult(client, OcelotServices.class, "getLocale", "{\"country\":\"FR\",\"language\":\"fr\"}");

		client = getClient();
		testRSCallWithResult(client, OcelotServices.class, "getLocale", "{\"country\":\"US\",\"language\":\"en\"}");
	}

	/**
	 * Check service getLocale/setLocale
	 */
	@Test
	public void testSetLocale() {
		Client client = getClient();
			// default locale is US
		testRSCallWithResult(client, LocaleMsgDataService.class, "getLocaleHello", "\"Hello François\"", getJson("François"));
		// switch to French
		testRSCallWithoutResult(client, OcelotServices.class, "setLocale", "{\"country\":\"FR\",\"language\":\"fr\"}");
		// locale is French
		testRSCallWithResult(client, LocaleMsgDataService.class, "getLocaleHello", "\"Bonjour François\"", getJson("François"));
	}

	/**
	 * RETURNTYPEDATASERVICE
	 */
	/**
	 * Test ReturnType is correct
	 */
	@Test
	public void testReturnTypeGetBool() {
		testRSCallWithResult(ReturnTypeDataService.class, "getBool", getJson(returnTypeDataService.getBool()));
	}

	@Test
	public void testReturnTypeGetBoolean() {
		testRSCallWithResult(ReturnTypeDataService.class, "getBoolean", getJson(returnTypeDataService.getBoolean()));
	}

	@Test
	public void testReturnTypeGetCollectionInteger() {
		testRSCallWithResult(ReturnTypeDataService.class, "getCollectionInteger", getJson(returnTypeDataService.getCollectionInteger()));
	}

	@Test
	public void testReturnTypeGetCollectionOfCollectionResult() {
		testRSCallWithResult(ReturnTypeDataService.class, "getCollectionOfCollectionResult", getJson(returnTypeDataService.getCollectionOfCollectionResult()));
	}

	@Test
	public void testReturnTypeGetCollectionResult() {
		testRSCallWithResult(ReturnTypeDataService.class, "getCollectionResult", getJson(returnTypeDataService.getCollectionResult()));
	}

	@Test
	public void testReturnTypeGetDate() {
		testRSCallWithResult(ReturnTypeDataService.class, "getDate", getJson(returnTypeDataService.getDate()));
	}

	@Test
	public void testReturnTypeGetMapResult() {
		testRSCallWithResult(ReturnTypeDataService.class, "getMapResult", getJson(returnTypeDataService.getMapResult()));
	}

	@Test
	public void testReturnTypeGetNum() {
		testRSCallWithResult(ReturnTypeDataService.class, "getNum", getJson(returnTypeDataService.getNum()));
	}

	@Test
	public void testReturnTypeGetNumber() {
		testRSCallWithResult(ReturnTypeDataService.class, "getNumber", getJson(returnTypeDataService.getNumber()));
	}

	@Test
	public void testReturnTypeGetResult() {
		testCallWithResult(ReturnTypeDataService.class, "getResult", getJson(returnTypeDataService.getResult()));
	}

	@Test
	public void testReturnTypeGetString() {
		testRSCallWithResult(ReturnTypeDataService.class, "getString", getJson(returnTypeDataService.getString()));
	}

	@Test
	public void testReturnTypeGetVoid() {
		testRSCallWithResult(ReturnTypeDataService.class, "getVoid", getJson(null));
	}

	/**
	 * ARGUMENTTYPEDATASERVICE
	 */
	/**
	 * Test ArgumentType is sent correctly
	 */
	@Test
	public void testMethodWithAlmostSameSignature() {
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithAlmostSameSignature", getJson(argumentTypeDataService.methodWithAlmostSameSignature(Integer.SIZE)), getJson(Integer.SIZE));
	}

	@Test
	public void testMethodWithAlmostSameSignature2() {
		String argument = "FOO";
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithAlmostSameSignature", getJson(argumentTypeDataService.methodWithAlmostSameSignature(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithAlmostSameSignature3() {
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithAlmostSameSignature", getJson(argumentTypeDataService.methodWithAlmostSameSignature("FOO", "FOO")), getJson("FOO"), getJson("FOO"));
	}

	@Test
	public void testMethodWithArrayInteger() {
		Integer[] argument = new Integer[]{Integer.SIZE, Integer.SIZE};
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithArrayInteger", getJson(argumentTypeDataService.methodWithArrayInteger(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithArrayResult() {
		Result[] argument = new Result[]{Result.getMock(), Result.getMock()};
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithArrayResult", getJson(argumentTypeDataService.methodWithArrayResult(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithBool() {
		boolean argument = Boolean.TRUE;
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithBool", getJson(argumentTypeDataService.methodWithBool(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithBoolean() {
		boolean argument = Boolean.FALSE;
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithBoolean", getJson(argumentTypeDataService.methodWithBoolean(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithCollectionInteger() {
		Collection<Integer> argument = Arrays.asList(Integer.SIZE, Integer.SIZE, Integer.SIZE);
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithCollectionInteger", getJson(argumentTypeDataService.methodWithCollectionInteger(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithCollectionOfCollectionResult() {
		Collection<Result> arg = Arrays.asList(Result.getMock(), Result.getMock());
		Collection<Collection<Result>> argument = Arrays.asList(arg, arg);
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithCollectionOfCollectionResult", getJson(argumentTypeDataService.methodWithCollectionOfCollectionResult(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithCollectionResult() {
		Collection<Result> argument = Arrays.asList(Result.getMock(), Result.getMock());
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithCollectionResult", getJson(argumentTypeDataService.methodWithCollectionResult(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithDate() {
		Date argument = new Date();
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithDate", getJson(argumentTypeDataService.methodWithDate(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithManyParameters() {
		String arg0 = "FOO";
		int arg1 = Integer.SIZE;
		Result arg2 = Result.getMock();
		Collection<String> arg3 = Arrays.asList("FOO0", "FOO1", "FOO2");
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithManyParameters", getJson(argumentTypeDataService.methodWithManyParameters(arg0, arg1, arg2, arg3)), getJson(arg0), getJson(arg1), getJson(arg2), getJson(arg3));
	}

	@Test
	public void testMethodWithMapResult() {
		Map<String, Result> argument = new HashMap();
		argument.put("A", Result.getMock());
		argument.put("B", Result.getMock());
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithMapResult", getJson(argumentTypeDataService.methodWithMapResult(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithNum() {
		int argument = Integer.SIZE;
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithNum", getJson(argumentTypeDataService.methodWithNum(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithNumber() {
		Integer argument = Integer.SIZE;
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithNumber", getJson(argumentTypeDataService.methodWithNumber(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithResult() {
		Result argument = Result.getMock();
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithResult", getJson(argumentTypeDataService.methodWithResult(argument)), getJson(argument));
	}

	@Test
	public void testMethodThatThrowException() {
		testRSCallThrowException(ArgumentTypeDataService.class, "methodThatThrowException", MethodException.class);
	}

	/**
	 * SECURITY
	 */
	@Test
	public void testEJBPrincipal() {
		System.out.println("testEJBPrincipal");
		Client client = getClient("demo", "demo");
		testRSCallWithResult(client, AccessEJBDataService.class, "getPrincipalName", getJson("demo"));
		testRSCallWithResult(client, AccessEJBDataService.class, "getCallerName", getJson("demo"));
	}

	@Test
	public void testEJBMethodAllowedToTest() {
		System.out.println("testEJBMethodAllowedToTest");
		Client client = getClient("test", "test");
		testRSCallWithResult(client, AccessEJBDataService.class, "getPrincipalName", getJson("test"));
		testRSCallWithResult(client, AccessEJBDataService.class, "getCallerName", getJson("test"));
		testRSCallWithoutResult(client, AccessEJBDataService.class, "methodAllowedToTest");
		testRSCallThrowException(client, AccessEJBDataService.class, "methodAllowedToAdmin", EJBAccessException.class);
	}

	@Test
	public void testEJBMethodAllowedToAdmin() {
		System.out.println("testEJBMethodAllowedToAdmin");
		Client client = getClient("admin", "admin");
		testRSCallWithResult(client, AccessEJBDataService.class, "getPrincipalName", getJson("admin"));
		testRSCallWithResult(client, AccessEJBDataService.class, "getCallerName", getJson("admin"));
		testRSCallWithoutResult(client, AccessEJBDataService.class, "methodAllowedToAdmin");
		testRSCallThrowException(client, AccessEJBDataService.class, "methodAllowedToTest", EJBAccessException.class);
	}

	@Test
	public void testEJBIsCallerInRole() {
		System.out.println("testEJBIsCallerInRole");
		Client client = getClient("admin", "admin");
		testRSCallWithResult(client, AccessEJBDataService.class, "isCallerInRole", getJson(true), getJson("ADMINR"));
		testRSCallWithResult(client, AccessEJBDataService.class, "isCallerInRole", getJson(true), getJson("USERR"));
		testRSCallWithResult(client, AccessEJBDataService.class, "isCallerInRole", getJson(false), getJson("TESTR"));
	}

	@Test
	public void testCDIPrincipal() {
		System.out.println("testCDIPrincipal");
		Client client = getClient("demo", "demo");
		testRSCallWithResult(client, AccessCDIDataService.class, "getPrincipalName", getJson("demo"));
		testRSCallWithResult(client, AccessCDIDataService.class, "getOcelotContextName", getJson("demo"));
	}

	@Test
	public void testCDIIsUserInRole() {
		System.out.println("testCDIIsUserInRole");
		Client client = getClient("admin", "admin");
		testRSCallWithResult(client, AccessCDIDataService.class, "isUserInRole", getJson(true), getJson("ADMINR"));
		testRSCallWithResult(client, AccessCDIDataService.class, "isUserInRole", getJson(true), getJson("USERR"));
		testRSCallWithResult(client, AccessCDIDataService.class, "isUserInRole", getJson(false), getJson("TESTR"));
	}

	/**
	 * Check monitor works
	 */
	@Test
	public void testGetTime() {
		Class clazz = MonitorDataService.class;
		String methodName = "testMonitor";
		System.out.println(clazz + "." + methodName);
		MessageToClient mtc = testRSCallWithoutResult(MonitorDataService.class, "testMonitor", getJson(500));
		long result = mtc.getTime();
		assertThat(result).isNotZero();
		assertThat(result).isGreaterThanOrEqualTo(500);
		assertThat(result).isLessThan(600);
	}

	/**
	 * Test send message that generate a cleancache message
	 */
	@Test
	public void testSendRemoveCacheMessage() {
		System.out.println("sendRemoveCacheMessage");
		final String topic = Constants.Cache.CLEANCACHE_TOPIC;
		try (Session wssession = createAndGetSession()) {
			subscribeToTopicInSession(wssession, topic);
			testWaitXMessageToTopic(wssession, 1, topic, new Runnable() {
				@Override
				public void run() {
					MessageFromClient mfc = getMessageFromClient(CacheDataService.class, "generateCleanCacheMessage", getJson(""), getJson(new Result(5)));
					mfc.setParameterNames(Arrays.asList("\"a\"", "\"r\""));
					testRSCallWithoutResult(getClient(), mfc, MessageType.RESULT);
				}
			}
			);
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Test send message that generate a cleanallcache message
	 */
	@Test
	public void testSendRemoveAllCacheMessage() {
		System.out.println("sendRemoveAllCacheMessage");
		final String topic = Constants.Cache.CLEANCACHE_TOPIC;
		try (Session wssession = createAndGetSession()) {
			subscribeToTopicInSession(wssession, topic);
			testWaitXMessageToTopic(wssession, 1, topic, new Runnable() {
				@Override
				public void run() {
					testRSCallWithoutResult(CacheDataService.class, "generateCleanAllCacheMessage");
				}
			}
			);
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}
	/**
	 * Test send message to topic
	 */
	@Test
	public void testSendMessageToMyTopic() {
		System.out.println("sendMessageToTopic");
		final String topic = "mytopic";
		((MyTopicAccessControler) myTopicAccessControler).setAccess(true);
		try (Session wssession = createAndGetSession()) {
			subscribeToTopicInSession(wssession, topic);
			long t0 = System.currentTimeMillis();
			int nbMsg = 10;
			CountDownLatch lock = new CountDownLatch(nbMsg);
			CountDownMessageHandler messageHandler = new CountDownMessageHandler(topic, lock);
			wssession.addMessageHandler(messageHandler);
			MessageToClient toTopic = new MessageToClient();
			toTopic.setId(topic);
			for (int i = 0; i < nbMsg; i++) {
				System.out.println("Send message to Topic '" + topic + "'");
				toTopic.setResponse(new Result(i));
				wsEvent.fire(toTopic);
			}
			boolean await = lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			long t1 = System.currentTimeMillis();
			assertThat(await).isTrue().as("Timeout. waiting %d ms. Remain %d/%d msgs", t1 - t0, lock.getCount(), nbMsg);
			wssession.removeMessageHandler(messageHandler);
		} catch (InterruptedException | IOException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Test send message to topic protected by specific access control
	 */
	@Test
	public void testSubscriptionToMyTopicFailCauseSpecificTAC() {
		System.out.println("subscriptionToMyTopic");
		final String topic = "mytopic";
		((MyTopicAccessControler) myTopicAccessControler).setAccess(false);
		try (Session wssession = createAndGetSession()) {
			testCallThrowExceptionInSession(wssession, OcelotServices.class, "subscribe", IllegalAccessException.class, getJson(topic));
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Test send message to topic protected by global access control
	 */
	@Test
	public void testSubscriptionToMyTopicFailCauseGlobalTAC() {
		System.out.println("subscriptionToMyTopic");
		final String topic = "mytopic";
		((MyTopicAccessControler) myTopicAccessControler).setAccess(true);
		((TopicAccessControler) topicAccessControler).setAccess(false);
		try (Session wssession = createAndGetSession()) {
			testCallThrowExceptionInSession(wssession, OcelotServices.class, "subscribe", IllegalAccessException.class, getJson(topic));
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Test receive message to mytopic
	 */
	@Test
	public void testReceiveMessageToMyTopic() {
		System.out.println("receiveMessageToMyTopic");
		final String topic = "mytopic";
		((TopicAccessControler) topicAccessControler).setAccess(true);
		((MyTopicAccessControler) myTopicAccessControler).setAccess(true);
		try (Session wssession = createAndGetSession()) {
			subscribeToTopicInSession(wssession, topic);
			testWaitXMessageToTopic(wssession, 1, topic, new Runnable() {
				@Override
				public void run() {
					testRSCallWithoutResult(TopicDataService.class, "sendMessageInMyTopic");
				}
			}
			);
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Test receive message to dynamic topic
	 */
	@Test
	public void testReceiveMessageToDynTopic() {
		System.out.println("receiveMessageToDynTopic");
		final String topic = "FOO";
		((TopicAccessControler) topicAccessControler).setAccess(true);
		((MyTopicAccessControler) myTopicAccessControler).setAccess(true);
		try (Session wssession = createAndGetSession()) {
			subscribeToTopicInSession(wssession, topic);
			testWaitXMessageToTopic(wssession, 1, topic, new Runnable() {
				@Override
				public void run() {
					testRSCallWithoutResult(TopicDataService.class, "sendMessageInDynTopic", getJson(topic));
				}
			}
			);
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Test receive X messages to mytopic
	 */
	@Test
	public void testReceiveXMessagesToMyTopic() {
		System.out.println("receiveXMessagesToMyTopic");
		final String topic = "mytopic";
		((TopicAccessControler) topicAccessControler).setAccess(true);
		((MyTopicAccessControler) myTopicAccessControler).setAccess(true);
		try (Session wssession = createAndGetSession()) {
			subscribeToTopicInSession(wssession, topic);
			final int nbMsg = 10;
			testWaitXMessageToTopic(wssession, nbMsg, topic, new Runnable() {
				@Override
				public void run() {
					testRSCallWithoutResult(TopicDataService.class, "sendXMessageInMyTopic", getJson(nbMsg));
				}
			}
			);
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Test receive X messages to dynamic topic
	 */
	@Test
	public void testReceiveXMessagesToDynTopic() {
		System.out.println("receiveXMessagesToDynTopic");
		final String topic = "FOO";
		((TopicAccessControler) topicAccessControler).setAccess(true);
		((MyTopicAccessControler) myTopicAccessControler).setAccess(true);
		try (Session wssession = createAndGetSession()) {
			subscribeToTopicInSession(wssession, topic);
			final int nbMsg = 10;
			testWaitXMessageToTopic(wssession, nbMsg, topic, new Runnable() {
				@Override
				public void run() {
					testRSCallWithoutResult(TopicDataService.class, "sendXMessageInDynTopic", getJson(nbMsg), getJson(topic));
				}
			}
			);
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}
}
