/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJBAccessException;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.inject.Inject;
import javax.websocket.Session;
import javax.ws.rs.client.Client;
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
import org.ocelotds.integration.dataservices.types.ArgumentTypeDataService;
import org.ocelotds.integration.dataservices.types.ReturnTypeDataService;
import org.ocelotds.messaging.MessageEvent;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.objects.Result;
import org.ocelotds.resolvers.CdiResolver;
import org.ocelotds.resolvers.DataServiceResolverIdLitteral;
import org.ocelotds.spi.IDataServiceResolver;
import org.ocelotds.integration.dataservices.monitor.MonitorDataService;
import org.ocelotds.integration.dataservices.topic.TopicDataService;
import org.ocelotds.security.JsTopicAccessController;
import org.ocelotds.integration.dataservices.security.AccessCDIDataService;
import org.ocelotds.integration.dataservices.security.AccessEJBDataService;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.messaging.MessageType;
import org.ocelotds.integration.dataservices.marshalling.ClassServices;
import org.ocelotds.integration.dataservices.validation.ValidationCdiDataService;
import org.ocelotds.messaging.ConstraintViolation;
import org.ocelotds.objects.WithConstraint;
import org.ocelotds.security.JsTopicCtrlAnnotationLiteral;
import static org.ocelotds.integration.AbstractOcelotTest.getJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.junit.Before;
import org.ocelotds.integration.dataservices.topic.TopicAccessController;

/**
 *
 * @author hhfrancois
 */
@RunWith(Arquillian.class)
public class OcelotTest extends AbstractOcelotTest {

	final int NB_SIMUL_METHODS = 10;

	@Inject
	@MessageEvent
	Event<MessageToClient> wsEvent;

	@Inject
	@Any
	Instance<JsTopicAccessController> myTopicAccessControllers;

	@Inject
	JsTopicAccessController topicAccessController;

	void setSpecificJsTopicAccess(String topic, boolean access) {
		JsTopicAccessController jtac = myTopicAccessControllers.select(new JsTopicCtrlAnnotationLiteral("mytopic")).get();
		((TopicAccessController) jtac).setAccess(access);
	}

	void setGlobalJsTopicAccess(boolean access) {
		((TopicAccessController) topicAccessController).setAccess(access);
	}

	@Before
	public void initAccess() {
		setSpecificJsTopicAccess("mytopic", true);
		setGlobalJsTopicAccess(true);
	}
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
		System.out.println("testRequestScopeCdi");
		testResultRequestScope(RequestCdiDataService.class);
	}

	@Test
	public void testRequestScopeEjb() {
		System.out.println("testRequestScopeEjb");
		testResultRequestScope(RequestEjbDataService.class);
	}

	/**
	 * Test 2 call in same session for session scope : result must be equals call 1 more in different session : result must be different
	 */
	@Test
	public void testSessionScopeCdi() {
		System.out.println("testSessionScopeCdi");
		testResultSessionScope(SessionCdiDataService.class);
	}

	@Test
	public void testSessionScopeEjb() {
		System.out.println("testSessionScopeEjb");
		testResultSessionScope(SessionEjbDataService.class);
	}

	/**
	 * Test 2 call in same session for session scope : result must be equals call 1 more in different session : result must be equals
	 */
	@Test
	public void testSingletonScopeCdi() {
		System.out.println("testSingletonScopeCdi");
		testResultSingletonScope(SingletonCdiDataService.class);
	}

	@Test
	public void testSingletonScopeEjb() {
		System.out.println("testSingletonScopeEjb");
		testResultSingletonScope(SingletonEjbDataService.class);
	}

	/**
	 * Test multi call in different sessions
	 */
	@Test
	public void testCallMultiMethodsMultiSessions() {
		System.out.println("testCallMultiMethodsMultiSessions");
		Client client = null;
		testCallMultiMethodsInClient("testCallMultiMethodsMultiSessions", NB_SIMUL_METHODS, client);
	}

	/**
	 * Test multi call in same session
	 */
	@Test
	public void testCallMultiMethodsMonoSessions() {
		System.out.println("testCallMultiMethodsMonoSessions");
		Client client = null;
		try {
			client = getClient();
			testCallMultiMethodsInClient("testCallMultiMethodsMonoSessions", NB_SIMUL_METHODS, client);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	/**
	 * OCELOTSERVICES
	 */
	/**
	 * Check service getServices return not empty list
	 */
//	@Test
	public void testGetServices() {
		System.out.println("getServices");
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
		System.out.println("getUsername");
		testRSCallWithResult(OcelotServices.class, "getUsername", getJson("user"));
	}

	/**
	 * Check service getLocale/setLocale
	 */
	@Test
	public void testGetSetLocale() {
		System.out.println("getLocale");
		Client client = null;
		try {
			client = getClient();
			// default locale is US
			testRSCallWithResult(client, OcelotServices.class, "getLocale", "{\"country\":\"US\",\"language\":\"en\"}");
			// switch to French
			testRSCallWithoutResult(client, OcelotServices.class, "setLocale", "{\"country\":\"FR\",\"language\":\"fr\"}");
			// check
			testRSCallWithResult(client, OcelotServices.class, "getLocale", "{\"country\":\"FR\",\"language\":\"fr\"}");
		} finally {
			if (client != null) {
				client.close();
			}
		}
		try {
			client = getClient();
			testRSCallWithResult(client, OcelotServices.class, "getLocale", "{\"country\":\"US\",\"language\":\"en\"}");
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	/**
	 * Check service getLocale/setLocale
	 */
	@Test
	public void testSetLocale() {
		System.out.println("getLocaleHello");
		Client client = null;
		try {
			client = getClient();
			// default locale is US
			testRSCallWithResult(client, LocaleMsgDataService.class, "getLocaleHello", "\"Hello François\"", getJson("François"));
			// switch to French
			testRSCallWithoutResult(client, OcelotServices.class, "setLocale", "{\"country\":\"FR\",\"language\":\"fr\"}");
			// locale is French
			testRSCallWithResult(client, LocaleMsgDataService.class, "getLocaleHello", "\"Bonjour François\"", getJson("François"));
		} finally {
			if (client != null) {
				client.close();
			}
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
		System.out.println("getBool");
		testRSCallWithResult(ReturnTypeDataService.class, "getBool", getJson(returnTypeDataService.getBool()));
	}

	@Test
	public void testReturnTypeGetBoolean() {
		System.out.println("getBoolean");
		testRSCallWithResult(ReturnTypeDataService.class, "getBoolean", getJson(returnTypeDataService.getBoolean()));
	}

	@Test
	public void testReturnTypeGetCollectionInteger() {
		System.out.println("getCollectionInteger");
		testRSCallWithResult(ReturnTypeDataService.class, "getCollectionInteger", getJson(returnTypeDataService.getCollectionInteger()));
	}

	@Test
	public void testReturnTypeGetCollectionOfCollectionResult() {
		System.out.println("getCollectionOfCollectionResult");
		testRSCallWithResult(ReturnTypeDataService.class, "getCollectionOfCollectionResult", getJson(returnTypeDataService.getCollectionOfCollectionResult()));
	}

	@Test
	public void testReturnTypeGetCollectionResult() {
		System.out.println("getCollectionResult");
		testRSCallWithResult(ReturnTypeDataService.class, "getCollectionResult", getJson(returnTypeDataService.getCollectionResult()));
	}

	@Test
	public void testReturnTypeGetDate() {
		System.out.println("getDate");
		testRSCallWithResult(ReturnTypeDataService.class, "getDate", getJson(returnTypeDataService.getDate()));
	}

	@Test
	public void testReturnTypeGetMapResult() {
		System.out.println("getMapResult");
		testRSCallWithResult(ReturnTypeDataService.class, "getMapResult", getJson(returnTypeDataService.getMapResult()));
	}

	@Test
	public void testReturnTypeGetNum() {
		System.out.println("getNum");
		testRSCallWithResult(ReturnTypeDataService.class, "getNum", getJson(returnTypeDataService.getNum()));
	}

	@Test
	public void testReturnTypeGetNumber() {
		System.out.println("getNumber");
		testRSCallWithResult(ReturnTypeDataService.class, "getNumber", getJson(returnTypeDataService.getNumber()));
	}

	@Test
	public void testReturnTypeGetResult() {
		System.out.println("getResult");
		testRSCallWithResult(ReturnTypeDataService.class, "getResult", getJson(returnTypeDataService.getResult()));
	}

	@Test
	public void testReturnTypeGetString() {
		System.out.println("getString");
		testRSCallWithResult(ReturnTypeDataService.class, "getString", getJson(returnTypeDataService.getString()));
	}

	@Test
	public void testReturnTypeGetVoid() {
		System.out.println("getVoid");
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
		System.out.println("methodWithAlmostSameSignature");
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithAlmostSameSignature", getJson(argumentTypeDataService.methodWithAlmostSameSignature(Integer.SIZE)), getJson(Integer.SIZE));
	}

	@Test
	public void testMethodWithAlmostSameSignature2() {
		System.out.println("methodWithAlmostSameSignature");
		String argument = "FOO";
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithAlmostSameSignature", getJson(argumentTypeDataService.methodWithAlmostSameSignature(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithAlmostSameSignature3() {
		System.out.println("methodWithAlmostSameSignature");
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithAlmostSameSignature", getJson(argumentTypeDataService.methodWithAlmostSameSignature("FOO", "FOO")), getJson("FOO"), getJson("FOO"));
	}

	@Test
	public void testMethodWithArrayInteger() {
		System.out.println("methodWithArrayInteger");
		Integer[] argument = new Integer[]{Integer.SIZE, Integer.SIZE};
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithArrayInteger", getJson(argumentTypeDataService.methodWithArrayInteger(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithArrayResult() {
		System.out.println("methodWithArrayResult");
		Result[] argument = new Result[]{Result.getMock(), Result.getMock()};
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithArrayResult", getJson(argumentTypeDataService.methodWithArrayResult(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithBool() {
		System.out.println("methodWithBool");
		boolean argument = Boolean.TRUE;
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithBool", getJson(argumentTypeDataService.methodWithBool(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithBoolean() {
		System.out.println("methodWithBoolean");
		boolean argument = Boolean.FALSE;
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithBoolean", getJson(argumentTypeDataService.methodWithBoolean(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithCollectionInteger() {
		System.out.println("methodWithCollectionInteger");
		Collection<Integer> argument = Arrays.asList(Integer.SIZE, Integer.SIZE, Integer.SIZE);
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithCollectionInteger", getJson(argumentTypeDataService.methodWithCollectionInteger(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithCollectionOfCollectionResult() {
		System.out.println("methodWithCollectionOfCollectionResult");
		Collection<Result> arg = Arrays.asList(Result.getMock(), Result.getMock());
		Collection<Collection<Result>> argument = Arrays.asList(arg, arg);
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithCollectionOfCollectionResult", getJson(argumentTypeDataService.methodWithCollectionOfCollectionResult(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithCollectionResult() {
		System.out.println("methodWithCollectionResult");
		Collection<Result> argument = Arrays.asList(Result.getMock(), Result.getMock());
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithCollectionResult", getJson(argumentTypeDataService.methodWithCollectionResult(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithDate() {
		System.out.println("methodWithDate");
		Date argument = new Date();
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithDate", getJson(argumentTypeDataService.methodWithDate(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithManyParameters() {
		System.out.println("methodWithManyParameters");
		String arg0 = "FOO";
		int arg1 = Integer.SIZE;
		Result arg2 = Result.getMock();
		Collection<String> arg3 = Arrays.asList("FOO0", "FOO1", "FOO2");
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithManyParameters", getJson(argumentTypeDataService.methodWithManyParameters(arg0, arg1, arg2, arg3)), getJson(arg0), getJson(arg1), getJson(arg2), getJson(arg3));
	}

	@Test
	public void testMethodWithMapResult() {
		System.out.println("methodWithMapResult");
		Map<String, Result> argument = new HashMap();
		argument.put("A", Result.getMock());
		argument.put("B", Result.getMock());
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithMapResult", getJson(argumentTypeDataService.methodWithMapResult(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithNum() {
		System.out.println("methodWithNum");
		int argument = Integer.SIZE;
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithNum", getJson(argumentTypeDataService.methodWithNum(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithNumber() {
		System.out.println("methodWithNumber");
		Integer argument = Integer.SIZE;
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithNumber", getJson(argumentTypeDataService.methodWithNumber(argument)), getJson(argument));
	}

	@Test
	public void testMethodWithResult() {
		System.out.println("methodWithResult");
		Result argument = Result.getMock();
		testRSCallWithResult(ArgumentTypeDataService.class, "methodWithResult", getJson(argumentTypeDataService.methodWithResult(argument)), getJson(argument));
	}

	@Test
	public void testMethodThatThrowException() {
		System.out.println("methodThatThrowException");
		testRSCallThrowException(ArgumentTypeDataService.class, "methodThatThrowException", MethodException.class);
	}

	/**
	 * MARSHALLING
	 */
	@Test
	public void testMethodWithMarshaller() {
		System.out.println("getCls");
		testRSCallWithResult(ClassServices.class, "getCls", getJson(String.class.getName()), getJson(String.class.getName()));
	}

	@Test
	public void testMethodWithMarshallerIterable() {
		System.out.println("getClasses");
		List<String> list = Arrays.asList(String.class.getName(), String.class.getName());
		testRSCallWithResult(ClassServices.class, "getClasses", getJson(list), getJson(list));
	}

	/**
	 * VALIDATION
	 */
	@Test
	public void methodWithValidationArgumentsTest() {
		// public void methodWithValidationArguments(@NotNull String str0, @NotNull String str1, @NotNull String str2) {}
		System.out.println("methodWithValidationArguments");
		MessageFromClient mfc = getMessageFromClient(ValidationCdiDataService.class, "methodWithValidationArguments", getJson(null), getJson(""), getJson(null));
		mfc.setParameterNames(Arrays.asList("str0", "str1", "str2"));
		MessageToClient mtc = testRSCallWithoutResult(getClient(), mfc, MessageType.CONSTRAINT);
		ConstraintViolation[] cvs = (ConstraintViolation[]) mtc.getResponse();
		assertThat(cvs).hasSize(2);
		ConstraintViolation cv = cvs[0];
		if (cv.getIndex() == 0) {
			assertThat(cv.getIndex()).isEqualTo(0);
			assertThat(cv.getName()).isEqualTo("str0");
			cv = cvs[1];
			assertThat(cv.getIndex()).isEqualTo(2);
			assertThat(cv.getName()).isEqualTo("str2");
		} else {
			assertThat(cv.getIndex()).isEqualTo(2);
			assertThat(cv.getName()).isEqualTo("str2");
			cv = cvs[1];
			assertThat(cv.getIndex()).isEqualTo(0);
			assertThat(cv.getName()).isEqualTo("str0");
		}
	}

	@Test
	public void methodWithArgumentNotNullTest() {
		// public void methodWithArgumentNotNull(@NotNull String str0) {}
		System.out.println("methodWithArgumentNotNull");
		testUniqueConstraint(ValidationCdiDataService.class, "methodWithArgumentNotNull", getJson("foo"), getJson(null));
	}

	@Test
	public void methodWithArgumentNullTest() {
		// public void methodWithArgumentNull(@Null String str0) {}
		System.out.println("methodWithArgumentNull");
		testUniqueConstraint(ValidationCdiDataService.class, "methodWithArgumentNull", getJson(null), getJson("foo"));
	}

	@Test
	public void methodWithArgumentMaxTest() {
		// public void methodWithArgumentMax(@Max(10) int int0) {}
		System.out.println("methodWithArgumentMax");
		testUniqueConstraint(ValidationCdiDataService.class, "methodWithArgumentMax", getJson(6), getJson(15));
	}

	@Test
	public void methodWithArgumentMinTest() {
		// public void methodWithArgumentMin(@Min(10) int int0) {}
		System.out.println("methodWithArgumentMin");
		testUniqueConstraint(ValidationCdiDataService.class, "methodWithArgumentMin", getJson(15), getJson(6));
	}

	@Test
	public void methodWithArgumentFutureTest() {
		// public void methodWithArgumentFuture(@Future Date date0) {}
		System.out.println("methodWithArgumentFuture");
		Calendar future = Calendar.getInstance();
		future.add(Calendar.MONTH, 1);
		Calendar past = Calendar.getInstance();
		past.add(Calendar.MONTH, -1);
		testUniqueConstraint(ValidationCdiDataService.class, "methodWithArgumentFuture", getJson(future), getJson(past));
	}

	@Test
	public void methodWithArgumentPastTest() {
		// public void methodWithArgumentPast(@Past Date date0) {}
		System.out.println("methodWithArgumentPast");
		Calendar future = Calendar.getInstance();
		future.add(Calendar.MONTH, 1);
		Calendar past = Calendar.getInstance();
		past.add(Calendar.MONTH, -1);
		testUniqueConstraint(ValidationCdiDataService.class, "methodWithArgumentPast", getJson(past), getJson(future));
	}

	@Test
	public void methodWithArgumentFalseTest() {
		// public void methodWithArgumentFalse(@AssertFalse Boolean bool0) {}
		System.out.println("methodWithArgumentFalse");
		testUniqueConstraint(ValidationCdiDataService.class, "methodWithArgumentFalse", getJson(false), getJson(true));
	}

	@Test
	public void methodWithArgumentTrueTest() {
		// public void methodWithArgumentTrue(@AssertTrue Boolean bool0) {}
		System.out.println("methodWithArgumentTrue");
		testUniqueConstraint(ValidationCdiDataService.class, "methodWithArgumentTrue", getJson(true), getJson(false));
	}

	@Test
	public void methodWithArgumentDecimalMaxTest() {
		// public void methodWithArgumentDecimalMax(@DecimalMax("50") long lg0) {}
		System.out.println("methodWithArgumentDecimalMax");
		testUniqueConstraint(ValidationCdiDataService.class, "methodWithArgumentDecimalMax", getJson(20), getJson(60));
	}

	@Test
	public void methodWithArgumentDecimalMinTest() {
		// public void methodWithArgumentDecimalMin(@DecimalMin("50") long lg0) {}
		System.out.println("methodWithArgumentDecimalMin");
		testUniqueConstraint(ValidationCdiDataService.class, "methodWithArgumentDecimalMin", getJson(60), getJson(20));
	}

	@Test
	public void methodWithArgumentDigitsTest() {
		// public void methodWithArgumentDigits(@Digits(integer = 3, fraction = 2) float flt0) {}
		System.out.println("methodWithArgumentDigits");
		testUniqueConstraint(ValidationCdiDataService.class, "methodWithArgumentDigits", getJson(new BigDecimal(123.45)), getJson(new BigDecimal(1.3)));
	}

	@Test
	public void methodWithArgumentSize2_10Test() {
		// public void methodWithArgumentSize2_10(@Size(min = 2, max = 10) String str0) {}
		System.out.println("methodWithArgumentSize2_10");
		testUniqueConstraint(ValidationCdiDataService.class, "methodWithArgumentSize2_10", getJson("azerty"), getJson("qwertyuiop^"));
	}

	@Test
	public void methodWithArgumentPatternTest() {
		// public void methodWithArgumentPattern(@Pattern(regexp = "\\d*") String str0) {}
		System.out.println("methodWithArgumentPattern");
		testUniqueConstraint(ValidationCdiDataService.class, "methodWithArgumentPattern", getJson("12345"), getJson("123E456"));
	}

	@Test
	public void methodWithArgumentConstraintTest() {
		// public void methodWithArgumentConstraint(WithConstraint wc) {}
		System.out.println("methodWithArgumentConstraint");
		WithConstraint wc = new WithConstraint();
		wc.setName("foo");
		testRSCallWithoutResult(ValidationCdiDataService.class, "methodWithArgumentConstraint", getJson(wc));
		MessageFromClient mfc = getMessageFromClient(ValidationCdiDataService.class, "methodWithArgumentConstraint", getJson(new WithConstraint()));
		mfc.setParameterNames(Arrays.asList("str0"));
		MessageToClient mtc = testRSCallWithoutResult(getClient(), mfc, MessageType.CONSTRAINT);
		ConstraintViolation[] cvs = (ConstraintViolation[]) mtc.getResponse();
		assertThat(cvs).isNotNull();
		assertThat(cvs).hasSize(1);
		ConstraintViolation cv = cvs[0];
		assertThat(cv.getIndex()).isEqualTo(0);
		assertThat(cv.getName()).isEqualTo("str0");
		assertThat(cv.getProp()).isEqualTo("name");
	}

	/**
	 * SECURITY
	 */
	@Test
	public void testEJBPrincipal() {
		System.out.println("testEJBPrincipal");
		Client client = null;
		try {
			client = getClient("demo", "demo");
			testRSCallWithResult(client, AccessEJBDataService.class, "getPrincipalName", getJson("demo"));
			testRSCallWithResult(client, AccessEJBDataService.class, "getCallerName", getJson("demo"));
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	@Test
	public void testEJBMethodAllowedToTest() {
		System.out.println("testEJBMethodAllowedToTest");
		Client client = null;
		try {
			client = getClient("test", "test");
			testRSCallWithResult(client, AccessEJBDataService.class, "getPrincipalName", getJson("test"));
			testRSCallWithResult(client, AccessEJBDataService.class, "getCallerName", getJson("test"));
			testRSCallWithoutResult(client, AccessEJBDataService.class, "methodAllowedToTest");
			testRSCallThrowException(client, AccessEJBDataService.class, "methodAllowedToAdmin", EJBAccessException.class);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	@Test
	public void testEJBMethodAllowedToAdmin() {
		System.out.println("testEJBMethodAllowedToAdmin");
		Client client = null;
		try {
			client = getClient("admin", "admin");
			testRSCallWithResult(client, AccessEJBDataService.class, "getPrincipalName", getJson("admin"));
			testRSCallWithResult(client, AccessEJBDataService.class, "getCallerName", getJson("admin"));
			testRSCallWithoutResult(client, AccessEJBDataService.class, "methodAllowedToAdmin");
			testRSCallThrowException(client, AccessEJBDataService.class, "methodAllowedToTest", EJBAccessException.class);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	@Test
	public void testEJBIsCallerInRole() {
		System.out.println("testEJBIsCallerInRole");
		Client client = null;
		try {
			client = getClient("admin", "admin");
			testRSCallWithResult(client, AccessEJBDataService.class, "isCallerInRole", getJson(true), getJson("ADMINR"));
			testRSCallWithResult(client, AccessEJBDataService.class, "isCallerInRole", getJson(true), getJson("USERR"));
			testRSCallWithResult(client, AccessEJBDataService.class, "isCallerInRole", getJson(false), getJson("TESTR"));
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	@Test
	public void testCDIPrincipal() {
		System.out.println("testCDIPrincipal");
		Client client = null;
		try {
			client = getClient("demo", "demo");
			testRSCallWithResult(client, AccessCDIDataService.class, "getPrincipalName", getJson("demo"));
			testRSCallWithResult(client, AccessCDIDataService.class, "getOcelotContextName", getJson("demo"));
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	@Test
	public void testCDIIsUserInRole() {
		System.out.println("testCDIIsUserInRole");
		Client client = null;
		try {
			client = getClient("admin", "admin");
			testRSCallWithResult(client, AccessCDIDataService.class, "isUserInRole", getJson(true), getJson("ADMINR"));
			testRSCallWithResult(client, AccessCDIDataService.class, "isUserInRole", getJson(true), getJson("USERR"));
			testRSCallWithResult(client, AccessCDIDataService.class, "isUserInRole", getJson(false), getJson("TESTR"));
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	/**
	 * Check monitor works
	 */
	@Test
	public void testGetTime() {
		Class clazz = MonitorDataService.class;
		String methodName = "testMonitor";
		System.out.println(clazz + "." + methodName);
		Client client = getClient();
		getJsessionFromServer(client);
		MessageToClient mtc = testRSCallWithoutResult(client, MonitorDataService.class, "testMonitor", getJson(500));
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
		MessageFromClient mfc = getMessageFromClient(CacheDataService.class, "generateCleanCacheMessage", getJson(""), getJson(new Result(5)));
		mfc.setParameterNames(Arrays.asList("a", "r"));
		testReceiveXMessageToTopicWithMfc(1, topic, mfc, "generateCleanCacheMessage", "user", "user");
	}

	/**
	 * Test send message that generate a cleanallcache message
	 */
	@Test
	public void testSendRemoveAllCacheMessage() {
		System.out.println("sendRemoveAllCacheMessage");
		final String topic = Constants.Cache.CLEANCACHE_TOPIC;
		testReceive1MessageToTopic(topic, CacheDataService.class, "generateCleanAllCacheMessage", "user", "user");
	}

	/**
	 * Test send message to topic protected by specific access control
	 */
	@Test
	public void testSubscriptionToMyTopicFailCauseSpecificTAC() {
		System.out.println("subscriptionToMyTopic");
		setGlobalJsTopicAccess(false);
		subscribeToTopic("mytopic", getClient(), MessageType.FAULT);
	}

	/**
	 * Test send message to topic protected by global access control
	 */
	@Test
	public void testSubscriptionToMyTopicFailCauseGlobalTAC() {
		System.out.println("subscriptionToMyTopic");
		setGlobalJsTopicAccess(false);
		subscribeToTopic("mytopic", getClient(), MessageType.FAULT);
	}

	/**
	 * Test receive message to mytopic
	 */
	@Test
	public void testReceiveMessageToAdminTopic() {
		System.out.println("receiveMessageToAdminTopic");
		final String topic = "admintopic";
		Client client = getClient("user", "user");
		String jsession = getJsessionFromServer(client);
		try (Session wssession = createAndGetSession(jsession, "user:user")) {
			subscribeToTopic(topic, client, MessageType.RESULT);
			testWait0MessageToTopic(wssession, topic, new Runnable() {
				@Override
				public void run() {
					testRSCallWithoutResult(TopicDataService.class, "sendMessageInAdminTopic");
				}
			});
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		unsubscribeToTopic(topic, client, MessageType.RESULT);
		testReceive1MessageToTopic(topic, TopicDataService.class, "sendMessageInAdminTopic", "admin", "admin");
	}

	/**
	 * Test receive message to mytopic
	 */
	@Test
	public void testReceiveMessageToMyTopic() {
		System.out.println("receiveMessageToMyTopic");
		testReceive1MessageToTopic("mytopic", TopicDataService.class, "sendMessageInMyTopic", "user", "user");
	}

	/**
	 * Test receive X messages to mytopic
	 */
	@Test
	public void testReceiveXMessagesToMyTopic() {
		System.out.println("receiveXMessagesToMyTopic");
		testReceiveXMessagesToTopic(10, "mytopic", TopicDataService.class, "sendXMessageInMyTopic", "user", "user");
	}

	/**
	 * Test receive message to dynamic topic
	 */
	@Test
	public void testReceiveMessageToDynTopic() {
		System.out.println("receive1MessageToDynTopic");
		testReceive1MessagesToDynTopic("FOO", TopicDataService.class, "sendMessageInDynTopic", "user", "user");
	}

	/**
	 * Test receive X messages to mytopic
	 */
	@Test
	public void testReceiveXMessagesToDynTopic() {
		System.out.println("receiveXMessageToDynTopic");
		testReceiveXMessagesToDynTopic(10, "FOO", TopicDataService.class, "sendXMessageInDynTopic", "user", "user");
	}
}
