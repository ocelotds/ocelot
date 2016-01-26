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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.inject.Inject;
import javax.websocket.Session;
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
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.messaging.MessageType;
import org.ocelotds.objects.Result;
import org.ocelotds.resolvers.CdiResolver;
import org.ocelotds.resolvers.DataServiceResolverIdLitteral;
import org.ocelotds.spi.IDataServiceResolver;
import org.ocelotds.annotations.JsTopicAccessControl;
import org.ocelotds.integration.dataservices.monitor.MonitorDataService;
import org.ocelotds.integration.dataservices.topic.TopicAccessControler;
import org.ocelotds.integration.dataservices.topic.TopicDataService;
import org.ocelotds.security.JsTopicAccessController;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

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
//	@Test
	public void testJavascriptCoreMinification() {
		System.out.println("testJavascriptCoreMinification");
		String resource = Constants.OCELOT + Constants.JS;
		HttpURLConnection connection1 = null;
		HttpURLConnection connection2 = null;
		try {
			connection1 = getConnectionForResource(resource, true);
			int minlength = countByte(connection1.getInputStream());
			connection2 = getConnectionForResource(resource, false);
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
			HttpURLConnection connection = getConnectionForResource(Constants.OCELOT + Constants.JS, false);
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
		int nb = NB_SIMUL_METHODS;
		System.out.println("call" + nb + "MethodsMultiSession");
		ExecutorService executorService = Executors.newCachedThreadPool();
		final List<Session> sessions = new ArrayList<>();
		try {
			final Class clazz = RequestCdiDataService.class;
			final String methodName = "getValue";
			long t0 = System.currentTimeMillis();
			final CountDownLatch lock = new CountDownLatch(nb);
			for (int i = 0; i < nb; i++) {
				Session session = createAndGetSession();
				sessions.add(session);
				session.addMessageHandler(new CountDownMessageHandler(lock));
				executorService.execute(new TestThread(clazz, methodName, session));
			}
			boolean await = lock.await(10L * nb, TimeUnit.MILLISECONDS);
			long t1 = System.currentTimeMillis();
			assertThat(await).isTrue().as("Timeout. waiting %f ms. Remain %s/%s msgs", t1 - t0, lock.getCount(), nb);
			System.out.println("testCallMultiMethodsMultiSessions Timeout. waiting " + (t1 - t0) + " ms. Remain " + lock.getCount() + "/" + nb + " msgs");
		} catch (InterruptedException ex) {
			fail(ex.getMessage());
		} finally {
			for (Session session : sessions) {
				try {
					session.close();
				} catch (IOException ex) {
				}
			}
			executorService.shutdown();
		}
	}

	/**
	 * Test multi call in same session
	 */
	@Test
	public void testCallMultiMethodsMonoSessions() {
		int nb = NB_SIMUL_METHODS;
		System.out.println("call" + nb + "MethodsMonoSession");
		ExecutorService executorService = Executors.newCachedThreadPool();
		try (Session session = createAndGetSession()) {
			final CountDownLatch lock = new CountDownLatch(nb);
			CountDownMessageHandler messageHandler = new CountDownMessageHandler(lock);
			session.addMessageHandler(messageHandler);
			final Class clazz = RequestCdiDataService.class;
			final String methodName = "getValue";
			long t0 = System.currentTimeMillis();
			for (int i = 0; i < nb; i++) {
				executorService.execute(new TestThread(clazz, methodName, session));
			}
			boolean await = lock.await(10L * nb, TimeUnit.MILLISECONDS);
			long t1 = System.currentTimeMillis();
			assertThat(await).isTrue().as("Timeout. waiting %f ms. Remain %s/%s msgs", t1 - t0, lock.getCount(), nb);
			System.out.println("testCallMultiMethodsMonoSessions Timeout. waiting " + (t1 - t0) + " ms. Remain " + lock.getCount() + "/" + nb + " msgs");
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		} finally {
			executorService.shutdown();
		}
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
		testCallWithoutResult(OcelotServices.class, "getServices");
	}

	/**
	 * Check service getUsername
	 */
	@Test
	public void testGetUsername() {
		testCallWithResult(OcelotServices.class, "getUsername", getJson("ANONYMOUS"));
	}

	/**
	 * Check service getLocale/setLocale
	 */
	@Test
	public void testGetSetLocale() {
		try (Session wssession = createAndGetSession()) {
			// default locale is US
			testCallWithResultInSession(wssession, OcelotServices.class, "getLocale", "{\"country\":\"US\",\"language\":\"en\"}");
			// switch to French
			testCallWithoutResultInSession(wssession, OcelotServices.class, "setLocale", "{\"country\":\"FR\",\"language\":\"fr\"}");
			// check
			testCallWithResultInSession(wssession, OcelotServices.class, "getLocale", "{\"country\":\"FR\",\"language\":\"fr\"}");
		} catch (IOException exception) {
		}
		// locale is session scope
		try (Session wssession = createAndGetSession()) {
			// default locale is US
			testCallWithResultInSession(wssession, OcelotServices.class, "getLocale", "{\"country\":\"US\",\"language\":\"en\"}");
		} catch (IOException exception) {
		}
	}

	/**
	 * Check service getLocale/setLocale
	 */
	@Test
	public void testSetLocale() {
		try (Session wssession = createAndGetSession()) {
			// default locale is US
			testCallWithResultInSession(wssession, LocaleMsgDataService.class, "getLocaleHello", "\"Hello François\"", getJson("François"));
			// switch to French
			testCallWithoutResultInSession(wssession, OcelotServices.class, "setLocale", "{\"country\":\"FR\",\"language\":\"fr\"}");
			// locale is French
			testCallWithResultInSession(wssession, LocaleMsgDataService.class, "getLocaleHello", "\"Bonjour François\"", getJson("François"));
		} catch (IOException exception) {
		}
	}

	/**
	 * RETURNTYPEDATASERVICE
	 */
	/**
	 * Test ReturnType is correct
	 */
	@Test
	public void testReturnTypeGetBool() {
		testCallWithResult(ReturnTypeDataService.class, "getBool", getJson(returnTypeDataService.getBool()));
	}

	@Test
	public void testReturnTypeGetBoolean() {
		testCallWithResult(ReturnTypeDataService.class, "getBoolean", getJson(returnTypeDataService.getBoolean()));
	}

	@Test
	public void testReturnTypeGetCollectionInteger() {
		testCallWithResult(ReturnTypeDataService.class, "getCollectionInteger", getJson(returnTypeDataService.getCollectionInteger()));
	}

	@Test
	public void testReturnTypeGetCollectionOfCollectionResult() {
		testCallWithResult(ReturnTypeDataService.class, "getCollectionOfCollectionResult", getJson(returnTypeDataService.getCollectionOfCollectionResult()));
	}

	@Test
	public void testReturnTypeGetCollectionResult() {
		testCallWithResult(ReturnTypeDataService.class, "getCollectionResult", getJson(returnTypeDataService.getCollectionResult()));
	}

	@Test
	public void testReturnTypeGetDate() {
		testCallWithResult(ReturnTypeDataService.class, "getDate", getJson(returnTypeDataService.getDate()));
	}

	@Test
	public void testReturnTypeGetMapResult() {
		testCallWithResult(ReturnTypeDataService.class, "getMapResult", getJson(returnTypeDataService.getMapResult()));
	}

	@Test
	public void testReturnTypeGetNum() {
		testCallWithResult(ReturnTypeDataService.class, "getNum", getJson(returnTypeDataService.getNum()));
	}

	@Test
	public void testReturnTypeGetNumber() {
		testCallWithResult(ReturnTypeDataService.class, "getNumber", getJson(returnTypeDataService.getNumber()));
	}

	@Test
	public void testReturnTypeGetResult() {
		testCallWithResult(ReturnTypeDataService.class, "getResult", getJson(returnTypeDataService.getResult()));
	}

	@Test
	public void testReturnTypeGetString() {
		testCallWithResult(ReturnTypeDataService.class, "getString", getJson(returnTypeDataService.getString()));
	}

	@Test
	public void testReturnTypeGetVoid() {
		testCallWithResult(ReturnTypeDataService.class, "getVoid", getJson(null));
	}

	/**
	 * ARGUMENTTYPEDATASERVICE
	 */
	/**
	 * Test ArgumentType is sent correctly
	 */
	@Test
	public void testMethodWithAlmostSameSignature() {
		testCallWithResult(ArgumentTypeDataService.class, "methodWithAlmostSameSignature", getJson(argumentTypeDataService.methodWithAlmostSameSignature(Integer.SIZE)), getJson(Integer.SIZE));
	}

	@Test
	public void testMethodWithAlmostSameSignature2() {
		String argument = "FOO";
		testCallWithResult(ArgumentTypeDataService.class, "methodWithAlmostSameSignature", getJson(argumentTypeDataService.methodWithAlmostSameSignature(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithAlmostSameSignature3() {
		testCallWithResult(ArgumentTypeDataService.class, "methodWithAlmostSameSignature", getJson(argumentTypeDataService.methodWithAlmostSameSignature("FOO", "FOO")), getJson("FOO"), getJson("FOO"));
	}

	@Test
	public void testMethodWithArrayInteger() {
		Integer[] argument = new Integer[]{Integer.SIZE, Integer.SIZE};
		testCallWithResult(ArgumentTypeDataService.class, "methodWithArrayInteger", getJson(argumentTypeDataService.methodWithArrayInteger(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithArrayResult() {
		Result[] argument = new Result[]{Result.getMock(), Result.getMock()};
		testCallWithResult(ArgumentTypeDataService.class, "methodWithArrayResult", getJson(argumentTypeDataService.methodWithArrayResult(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithBool() {
		boolean argument = Boolean.TRUE;
		testCallWithResult(ArgumentTypeDataService.class, "methodWithBool", getJson(argumentTypeDataService.methodWithBool(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithBoolean() {
		boolean argument = Boolean.FALSE;
		testCallWithResult(ArgumentTypeDataService.class, "methodWithBoolean", getJson(argumentTypeDataService.methodWithBoolean(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithCollectionInteger() {
		Collection<Integer> argument = Arrays.asList(Integer.SIZE, Integer.SIZE, Integer.SIZE);
		testCallWithResult(ArgumentTypeDataService.class, "methodWithCollectionInteger", getJson(argumentTypeDataService.methodWithCollectionInteger(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithCollectionOfCollectionResult() {
		Collection<Result> arg = Arrays.asList(Result.getMock(), Result.getMock());
		Collection<Collection<Result>> argument = Arrays.asList(arg, arg);
		testCallWithResult(ArgumentTypeDataService.class, "methodWithCollectionOfCollectionResult", getJson(argumentTypeDataService.methodWithCollectionOfCollectionResult(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithCollectionResult() {
		Collection<Result> argument = Arrays.asList(Result.getMock(), Result.getMock());
		testCallWithResult(ArgumentTypeDataService.class, "methodWithCollectionResult", getJson(argumentTypeDataService.methodWithCollectionResult(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithDate() {
		Date argument = new Date();
		testCallWithResult(ArgumentTypeDataService.class, "methodWithDate", getJson(argumentTypeDataService.methodWithDate(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithManyParameters() {
		String arg0 = "FOO";
		int arg1 = Integer.SIZE;
		Result arg2 = Result.getMock();
		Collection<String> arg3 = Arrays.asList("FOO0", "FOO1", "FOO2");
		testCallWithResult(ArgumentTypeDataService.class, "methodWithManyParameters", getJson(argumentTypeDataService.methodWithManyParameters(arg0, arg1, arg2, arg3)), getJson(arg0), getJson(arg1), getJson(arg2), getJson(arg3));
	}

	@Test
	public void testMethodWithMapResult() {
		Map<String, Result> argument = new HashMap();
		argument.put("A", Result.getMock());
		argument.put("B", Result.getMock());
		testCallWithResult(ArgumentTypeDataService.class, "methodWithMapResult", getJson(argumentTypeDataService.methodWithMapResult(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithNum() {
		int argument = Integer.SIZE;
		testCallWithResult(ArgumentTypeDataService.class, "methodWithNum", getJson(argumentTypeDataService.methodWithNum(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithNumber() {
		Integer argument = Integer.SIZE;
		testCallWithResult(ArgumentTypeDataService.class, "methodWithNumber", getJson(argumentTypeDataService.methodWithNumber(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithResult() {
		Result argument = Result.getMock();
		testCallWithResult(ArgumentTypeDataService.class, "methodWithResult", getJson(argumentTypeDataService.methodWithResult(argument)), getJson(argument));
	}

	@Test
	public void testMethodThatThrowException() {
		testCallThrowException(ArgumentTypeDataService.class, "methodThatThrowException", MethodException.class);
	}

	/**
	 * Check monitor works
	 */
	@Test
	public void testGetTime() {
		Class clazz = MonitorDataService.class;
		String methodName = "testMonitor";
		System.out.println(clazz + "." + methodName);
		try (Session wssession = createAndGetSession(true)) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz, methodName, getJson(500));
			assertThat(messageToClient.getType()).isEqualTo(MessageType.RESULT);
			long result = messageToClient.getTime();
			assertThat(result).isNotZero();
			assertThat(result).isGreaterThanOrEqualTo(500);
			assertThat(result).isLessThan(600);
		} catch (IOException exception) {
		}
	}

	/**
	 * Test send message that generate a cleancache message
	 */
	@Test
	public void testSendRemoveCacheMessage() {
		System.out.println("sendRemoveCacheMessage");
		try (Session wssession = createAndGetSession()) {
			subscribeToTopicInSession(wssession, Constants.Cache.CLEANCACHE_TOPIC);
			long t0 = System.currentTimeMillis();
			MessageFromClient messageFromClient = getMessageFromClientWithParamNames(CacheDataService.class, "generateCleanCacheMessage", "\"a\",\"r\"", getJson(""), getJson(new Result(5)));
			CountDownLatch lock = new CountDownLatch(2);
			// on crée un handler client de reception de la réponse
			CountDownMessageHandler messageHandler = new CountDownMessageHandler(lock);
			wssession.addMessageHandler(messageHandler);
			// send
			wssession.getAsyncRemote().sendText(messageFromClient.toJson());
			// wait le delock ou timeout
			boolean await = lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			// lockCount doit être à  zero sinon, on a pas eu le resultat
			long t1 = System.currentTimeMillis();
			assertThat(await).as("Timeout. waiting %d ms. Remain %d/%d msgs", t1 - t0, lock.getCount(), 2).isTrue();
			wssession.removeMessageHandler(messageHandler);
		} catch (InterruptedException | IOException ex) {
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
			 			testCallWithoutResult(TopicDataService.class, "sendMessageInMyTopic");
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
			 			testCallWithoutResult(TopicDataService.class, "sendMessageInDynTopic", getJson(topic));
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
			 			testCallWithoutResult(TopicDataService.class, "sendXMessageInMyTopic", getJson(nbMsg));
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
			 			testCallWithoutResult(TopicDataService.class, "sendXMessageInDynTopic", getJson(nbMsg), getJson(topic));
					}
				}
			);
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}
}
