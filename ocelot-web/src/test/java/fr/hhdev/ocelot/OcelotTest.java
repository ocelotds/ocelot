/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.ocelot;

import fr.hhdev.ocelot.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.hhdev.ocelot.messaging.Fault;
import fr.hhdev.ocelot.messaging.Command;
import fr.hhdev.ocelot.messaging.MessageFromClient;
import fr.hhdev.ocelot.messaging.MessageToClient;
import fr.hhdev.ocelot.messaging.MessageEvent;
import fr.hhdev.ocelot.resolvers.DataServiceException;
import fr.hhdev.ocelot.resolvers.DataServiceResolver;
import fr.hhdev.ocelot.resolvers.DataServiceResolverIdLitteral;
import fr.hhdev.ocelot.resolvers.EJBResolver;
import fr.hhdev.ocelot.resolvers.PojoResolver;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.el.MethodNotFoundException;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import lombok.extern.slf4j.Slf4j;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.weld.exceptions.UnsatisfiedResolutionException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author hhfrancois
 */
@Slf4j
@RunWith(Arquillian.class)
public class OcelotTest {

	private final long WAITING = 100;

	@Inject
	@MessageEvent
	Event<MessageToClient> wsEvent;

	@Inject
	@Any
	private Instance<DataServiceResolver> resolvers;

	private DataServiceResolver getResolver(String type) {
		return resolvers.select(new DataServiceResolverIdLitteral(type)).get();
	}

	private PojoDataService destination = new PojoDataService();

	static Session wssession;

	private Command command;

	/**
	 * Pour tester l'api dans le contener JEE on crée un war
	 *
	 * @return
	 */
	@Deployment
	public static WebArchive createWarArchive() {
		File[] libs = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies().resolve().withTransitivity().asFile();
		File logback = new File("src/test/resources/logback.xml");
		return ShrinkWrap.create(WebArchive.class, "testOcelot.war")
				  .addAsLibraries(libs)
				  .addPackages(true, "fr.hhdev.ocelot")
				  .addAsWebInfResource(new FileAsset(logback), "logback.xml")
				  .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@BeforeClass
	public static void setUpClass() {
		System.out.println("===============================================================================================================");
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		try {
			wssession = container.connectToServer(OcelotClientEnpoint.class, new URI("ws://localhost:8282/ocelot/endpoint"));
		} catch (URISyntaxException | DeploymentException | IOException ex) {
			ex.printStackTrace();
		}
	}

	@AfterClass
	public static void tearDownClass() {
		try {
			wssession.close();
		} catch (IOException ex) {
		}
		System.out.println("===============================================================================================================");
	}

	@Before
	public void setUp() {
		System.out.println("---------------------------------------------------------------------------------------------------------------");
		command = new Command();
		command.setCommand(Constants.Command.Value.CALL);
		command.setTopic("pojo");
	}

	@After
	public void tearDown() {
		System.out.println("---------------------------------------------------------------------------------------------------------------");
	}

	private MessageFromClient getMessageFromClient(String operation, String... params) {
		MessageFromClient messageFromClient = new MessageFromClient();
		messageFromClient.setId(UUID.randomUUID().toString());
		messageFromClient.setDataService(PojoDataService.class.getName());
		messageFromClient.setOperation(operation);
		if (params != null) {
			messageFromClient.getParameters().addAll(Arrays.asList(params));
		}
		return messageFromClient;
	}

	private String getJson(Object obj) {
		try {
			if (String.class.isInstance(obj)) {
				return "\"" + obj + "\"";
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(obj);
		} catch (IOException ex) {
			return null;
		}
	}

	/**
	 * Vérification qu'un resolver inconnu remonte bien une exception
	 */
	@Test(expected = UnsatisfiedResolutionException.class)
	public void testDataServiceExceptionOnUnknownResolver() {
		System.out.println("failResolveDataService");
		DataServiceResolver resolver = getResolver("foo");
	}

	/**
	 * Teste de récupération du resolver d'EJB
	 */
	@Test
	public void testGetResolverEjb() {
		System.out.println("getResolverEjb");
		DataServiceResolver resolver = getResolver(Constants.Resolver.EJB);
		assertNotNull(resolver);
		assertEquals(EJBResolver.class, resolver.getClass());
	}

	/**
	 * Teste de récupération du resolver de POJO
	 */
	@Test
	public void testGetResolverPojo() {
		System.out.println("getResolverPojo");
		DataServiceResolver resolver = getResolver(Constants.Resolver.POJO);
		assertNotNull(resolver);
		assertEquals(PojoResolver.class, resolver.getClass());
	}

	/**
	 * Teste de récupération du resolver de SPRING
	 */
	@Test(expected = UnsatisfiedResolutionException.class)
	public void testGetResolverSpring() {
		System.out.println("getResolverSpring");
		DataServiceResolver resolver = getResolver(Constants.Resolver.SPRING);
		assertNotNull(resolver);
//		assertEquals(SpringResolver.class, resolver.getClass());
	}

	/**
	 * Vérifie qu'un service n'existant pas remonte bien une exception
	 *
	 * @throws fr.hhdev.ocelot.resolvers.DataServiceException
	 */
	@Test(expected = DataServiceException.class)
	public void testDataServiceExceptionOnResolveDataService() throws DataServiceException {
		System.out.println("failResolveDataService");
		DataServiceResolver resolver = getResolver(Constants.Resolver.POJO);
		resolver.resolveDataService("foo");
	}

	/**
	 * Vérifie que le pojo-resolver remonte le bien PojoDataService
	 */
	@Test
	public void testResolvePojoDataService() {
		System.out.println("resolveDataService");
		try {
			DataServiceResolver resolver = getResolver(Constants.Resolver.POJO);
			Object dest = resolver.resolveDataService(PojoDataService.class.getName());
			assertNotNull(dest);
			assertEquals(PojoDataService.class, dest.getClass());
		} catch (DataServiceException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Vérifie la désérialisation de MessageToClient result = 1
	 */
	@Test
	public void testMessageIntResultToClientCreator() {
		System.out.println("MessageToClient.createFromJson");
		String uuid = UUID.randomUUID().toString();
		Object expectedResult = 1;
		String operation = "methodWithResult";
		String json = String.format("{\"%s\":\"%s\",\"%s\":%s}",
				  Constants.Message.ID, uuid, Constants.Message.RESULT, expectedResult);
		MessageToClient result = MessageToClient.createFromJson(json);
		assertEquals(uuid, result.getId());
		assertEquals("" + expectedResult, result.getResult());
	}

	/**
	 * Vérifie la désérialisation de MessageToClient result = "foo"
	 */
	@Test
	public void testMessageStringResultToClientCreator() {
		System.out.println("MessageToClient.createFromJson");
		String uuid = UUID.randomUUID().toString();
		String expectedResultJS = "\"foo\"";
		String json = String.format("{\"%s\":\"%s\",\"%s\":%s}",
				  Constants.Message.ID, uuid, Constants.Message.RESULT, expectedResultJS);
		MessageToClient result = MessageToClient.createFromJson(json);
		assertEquals(uuid, result.getId());
		assertEquals(expectedResultJS, result.getResult());
	}

	/**
	 * Vérifie la désérialisation de MessageToClient result = {"integer": 5, "foo": "foo"}
	 */
	@Test
	public void testMessageObjectResultToClientCreator() {
		System.out.println("MessageToClient.createFromJson");
		String uuid = UUID.randomUUID().toString();
		Object expectedResult = "{\"integer\":5,\"foo\":\"foo\"}";
		String json = String.format("{\"%s\":\"%s\",\"%s\":%s}",
				  Constants.Message.ID, uuid, Constants.Message.RESULT, expectedResult);
		MessageToClient result = MessageToClient.createFromJson(json);
		assertEquals(uuid, result.getId());
		assertEquals(expectedResult, result.getResult());
	}

	/**
	 * Vérifie la désérialisation de MessageToClient fault = "java.lang.NullPointerException"
	 */
	@Test
	public void testMessageFaultToClientCreator() {
		System.out.println("MessageToClient.createFromJson");
		String uuid = UUID.randomUUID().toString();
		Fault f = new Fault();
		f.setMessage("Message d'erreur");
		f.setClassname(NullPointerException.class.getName());
		f.setStacktrace(null);
		String json = String.format("{\"%s\":\"%s\",\"%s\":%s}",
				  Constants.Message.ID, uuid, Constants.Message.FAULT, f.toJson());
		logger.debug("MESSAGe AVEC ERREUR : " + json);
		MessageToClient result = MessageToClient.createFromJson(json);
		assertEquals(uuid, result.getId());
		assertEquals(f.getClassname(), result.getFault().getClassname());
	}

	/**
	 * Vérifie la désérialisation de MessageFromClient arg = "java.lang.NullPointerException"
	 */
	@Test
	public void testMessageFromClientCreator() {
		System.out.println("MessageFromClient.createFromJson");
		String uuid = UUID.randomUUID().toString();
		String resultJS = getJson(new Result(6));
		String mapResultJS = getJson(destination.getMapResult());
		String operation = "methodWithResult";
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":[%s,%s]}",
				  Constants.Message.ID, uuid, Constants.Message.DATASERVICE, PojoDataService.class.getName(), Constants.Message.OPERATION, operation,
				  Constants.Message.ARGUMENTS, resultJS, mapResultJS);
		MessageFromClient result = MessageFromClient.createFromJson(json);
		assertEquals(uuid, result.getId());
		assertEquals(PojoDataService.class.getName(), result.getDataService());
		assertEquals(operation, result.getOperation());
		List<String> parameters = result.getParameters();
		assertEquals(resultJS, parameters.get(0));
		assertEquals(mapResultJS, parameters.get(1));
	}

	/**
	 * Vérifie que l'appel à une methode inconue remonte bien une erreur adéquate
	 */
	@Test
	public void testMethodUnknow() {
		System.out.println("getUnknownMethod");
		try {
			MessageFromClient messageFromClient = getMessageFromClient("getUnknownMethod");
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			assertEquals(result, null);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(MethodNotFoundException.class.getName(), fault.getClassname());
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel à une méthode retournant void (sans resultat)
	 */
	@Test
	public void testMethodNoResult() {
		System.out.println("getVoid");
		try {
			MessageFromClient messageFromClient = getMessageFromClient("getVoid");
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			assertEquals(result, null);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel à une méthode retournant une String Attention la string retourné est sous la forme "foo" avec les double côtes
	 */
	@Test
	public void testGetString() {
		System.out.println("getString");
		try {
			MessageFromClient messageFromClient = getMessageFromClient("getString");
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.getString());
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel à une méthode retournant un int
	 */
	@Test
	public void testGetNum() {
		System.out.println("getNum");
		try {
			MessageFromClient messageFromClient = getMessageFromClient("getNum");
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.getNum());
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel à une méthode retournant un Integer
	 */
	@Test
	public void testGetNumber() {
		System.out.println("getNumber");
		try {
			MessageFromClient messageFromClient = getMessageFromClient("getNumber");
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.getNumber());
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel à une méthode retournant un boolean
	 */
	@Test
	public void testGetBool() {
		System.out.println("getBool");
		try {
			MessageFromClient messageFromClient = getMessageFromClient("getBool");
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.getBool());
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel à une méthode retournant un Boolean
	 */
	@Test
	public void testGetBoolean() {
		System.out.println("getBoolean");
		try {
			MessageFromClient messageFromClient = getMessageFromClient("getBoolean");
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.getBoolean());
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel à une méthode retournant une date
	 */
	@Test
	public void testGetDate() {
		System.out.println("getDate");
		try {
			Date before = Date.from(Instant.now());
			MessageFromClient messageFromClient = getMessageFromClient("getDate");
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			assertNotNull(result);

			Date res = Date.from(Instant.ofEpochMilli(Long.parseLong(result)));
			assertTrue(before.before(res));

			Date after = Date.from(Instant.now());
			assertTrue(after.after(res));
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel à une méthode retournant un objet de type Result
	 */
	@Test
	public void testGetResult() {
		System.out.println("getResult");
		try {
			MessageFromClient messageFromClient = getMessageFromClient("getResult");
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.getResult());
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel à une méthode retournant une Collection&lt;Integer&gt;
	 */
	@Test
	public void testGetCollectionInteger() {
		System.out.println("getCollectionInteger");
		try {
			MessageFromClient messageFromClient = getMessageFromClient("getCollectionInteger");
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.getCollectionInteger());
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel à une méthode retournant une Collection&lt;Result&gt;
	 */
	@Test
	public void testGetCollectionResult() {
		System.out.println("getCollectionResult");
		try {
			MessageFromClient messageFromClient = getMessageFromClient("getCollectionResult");
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.getCollectionResult());
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel à une méthode retournant une Collection&lt;Collection&lt;Result&gt;&gt;
	 */
	@Test
	public void testGetCollectionOfCollectionResult() {
		System.out.println("getCollectionOfCollectionResult");
		try {
			MessageFromClient messageFromClient = getMessageFromClient("getCollectionOfCollectionResult");
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.getCollectionOfCollectionResult());
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel à une méthode retournant une Map&lt;Result&gt;
	 */
	@Test
	public void testGetMapResult() {
		System.out.println("getMapResult");
		try {
			MessageFromClient messageFromClient = getMessageFromClient("getMapResult");
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.getMapResult());
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument un int
	 */
	@Test
	public void testMethodWithNum() {
		System.out.println("methodWithNum");
		try {
			MessageFromClient messageFromClient = getMessageFromClient("methodWithNum", getJson(1));
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.methodWithNum(1));
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument un Integer
	 */
	@Test
	public void testMethodWithNumber() {
		System.out.println("methodWithNumber");
		try {
			MessageFromClient messageFromClient = getMessageFromClient("methodWithNumber", getJson(2));
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.methodWithNumber(2));
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument un boolean
	 */
	@Test
	public void testMethodWithBool() {
		System.out.println("methodWithBool");
		try {
			MessageFromClient messageFromClient = getMessageFromClient("methodWithBool", getJson(true));
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.methodWithBool(true));
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument un Boolean
	 */
	@Test
	public void testMethodWithBoolean() {
		System.out.println("methodWithBoolean");
		try {
			MessageFromClient messageFromClient = getMessageFromClient("methodWithBoolean", getJson(false));
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.methodWithBoolean(false));
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument une Date
	 */
	@Test
	public void testMethodWithDate() {
		System.out.println("methodWithDate");
		try {
			Date now = Date.from(Instant.now());
			MessageFromClient messageFromClient = getMessageFromClient("methodWithDate", getJson(now));
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.methodWithDate(now));
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument un objet Result
	 */
	@Test
	public void testMethodWithResult() {
		System.out.println("methodWithResult");
		try {
			Date now = Date.from(Instant.now());
			MessageFromClient messageFromClient = getMessageFromClient("methodWithResult", getJson(new Result(6)));
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.methodWithResult(new Result(6)));
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument une Collection&lt;Integer&gt;
	 */
	@Test
	public void testMethodWithCollectionInteger() {
		System.out.println("methodWithCollectionInteger");
		try {
			Collection<Integer> cl = destination.getCollectionInteger();
			MessageFromClient messageFromClient = getMessageFromClient("methodWithCollectionInteger", getJson(cl));
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.methodWithCollectionInteger(cl));
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument une Collection&lt;Result&gt;
	 */
	@Test
	public void testMethodWithCollectionResult() {
		System.out.println("methodWithCollectionResult");
		try {
			Collection<Result> cl = destination.getCollectionResult();
			MessageFromClient messageFromClient = getMessageFromClient("methodWithCollectionResult", getJson(cl));
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.methodWithCollectionResult(cl));
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument une Map&lt;Result&gt;
	 */
	@Test
	public void testMethodWithMapResult() {
		System.out.println("methodWithMapResult");
		try {
			Map<String, Result> cl = destination.getMapResult();
			MessageFromClient messageFromClient = getMessageFromClient("methodWithMapResult", getJson(cl));
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.methodWithMapResult(cl));
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument une Collection&lt;Collection&lt;Result&gt;&gt;
	 */
	@Test
	public void testMethodWithCollectionOfCollectionResult() {
		System.out.println("methodWithCollectionOfCollectionResult");
		try {
			Collection<Collection<Result>> cl = destination.getCollectionOfCollectionResult();
			MessageFromClient messageFromClient = getMessageFromClient("methodWithCollectionOfCollectionResult", getJson(cl));
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.methodWithCollectionOfCollectionResult(cl));
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en arguments plusieurs objets un objet REsultet une Collection&lt;Result&gt;
	 */
	@Test
	public void testMethodWithManyParameters() {
		System.out.println("methodWithManyParameters");
		try {
			Collection<String> cl = new ArrayList<>();
			cl.add("foo");
			cl.add("foo");
			MessageFromClient messageFromClient = getMessageFromClient("methodWithManyParameters", getJson("foo"), getJson(5), getJson(new Result(3)), getJson(cl));
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			String json = getJson(destination.methodWithManyParameters("foo", 5, new Result(3), cl));
			assertEquals(json, result);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertEquals(fault, null);
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel d'une méthode levant une exception MethodException
	 */
	@Test
	public void testMethodThatThrowException() {
		System.out.println("methodThatThrowException");
		try {
			MessageFromClient messageFromClient = getMessageFromClient("methodThatThrowException");
			command.setMessage(messageFromClient.toJson());
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult(messageFromClient.getId());
			assertEquals(result, null);
			Fault fault = OcelotClientEnpoint.getFault(messageFromClient.getId());
			assertNotNull(fault);
			assertEquals(MethodException.class.getName(), fault.getClassname());
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Test d'envoi d'un message à un topic
	 */
	@Test
	public void testSendMessageToTopic() {
		System.out.println("sendMessageToTopic");
		try {
			logger.info("Enregistrement au Topic {}", "mytopic");
			Command command = new Command();
			command.setTopic("mytopic");
			command.setCommand(Constants.Command.Value.SUBSCRIBE);
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(WAITING);
			logger.info("Generation de message pour {}", "mytopic");
			MessageToClient toTopic = new MessageToClient();
			toTopic.setId("mytopic");

			toTopic.setResult(new Result(5));
			wsEvent.fire(toTopic);
			Thread.sleep(WAITING);
			String result = OcelotClientEnpoint.getResult("mytopic");
			assertEquals(result, getJson(toTopic.getResult()));

			toTopic.setResult(new Result(10));
			wsEvent.fire(toTopic);
			Thread.sleep(WAITING);
			result = OcelotClientEnpoint.getResult("mytopic");
			assertEquals(result, getJson(toTopic.getResult()));

			toTopic.setResult(new Result(15));
			wsEvent.fire(toTopic);
			Thread.sleep(WAITING);
			result = OcelotClientEnpoint.getResult("mytopic");
			assertEquals(result, getJson(toTopic.getResult()));
		} catch (InterruptedException | IOException ex) {
			fail(ex.getMessage());
		}
	}
}
