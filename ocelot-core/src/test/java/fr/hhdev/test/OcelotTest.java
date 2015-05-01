/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.test;

import fr.hhdev.test.dataservices.EJBDataService;
import fr.hhdev.test.dataservices.CDIDataService;
import fr.hhdev.test.dataservices.SingletonCDIDataService;
import fr.hhdev.test.dataservices.PojoDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.exceptions.DataServiceNotFoundException;
import fr.hhdev.ocelot.messaging.Fault;
import fr.hhdev.ocelot.messaging.Command;
import fr.hhdev.ocelot.messaging.MessageFromClient;
import fr.hhdev.ocelot.messaging.MessageToClient;
import fr.hhdev.ocelot.messaging.MessageEvent;
import fr.hhdev.ocelot.resolvers.CdiResolver;
import fr.hhdev.ocelot.spi.DataServiceException;
import fr.hhdev.ocelot.spi.IDataServiceResolver;
import fr.hhdev.ocelot.resolvers.DataServiceResolverIdLitteral;
import fr.hhdev.ocelot.resolvers.EJBResolver;
import fr.hhdev.ocelot.resolvers.PojoResolver;
import fr.hhdev.test.dataservices.SingletonEJBDataService;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.weld.exceptions.UnsatisfiedResolutionException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
@RunWith(Arquillian.class)
public class OcelotTest {

	final static Logger logger = LoggerFactory.getLogger(OcelotTest.class);

	private final long TIMEOUT = 1000;

	@Inject
	@MessageEvent
	Event<MessageToClient> wsEvent;

	@Inject
	@Any
	private Instance<IDataServiceResolver> resolvers;

	private IDataServiceResolver getResolver(String type) {
		return resolvers.select(new DataServiceResolverIdLitteral(type)).get();
	}

	private final PojoDataService destination = new PojoDataService();

	static Session wssession;

	private Command command;

	private CountDownMessageHandler messageHandler = null;

	/**
	 * Pour tester l'api dans le contener JEE on crée un war
	 *
	 * @return
	 */
	@Deployment(name = "glassfish")
	public static WebArchive createWarGlassfishArchive() {
		return createWarArchive();
	}

	/**
	 * Pour tester l'api dans le contener JEE on crée un war
	 *
	 * @return
	 */
//	@Deployment(name = "tomcat")
//	public static WebArchive createWarTomcatArchive() {
//		File web = new File("src/test/resources/tomcat/web.xml");
//		File context = new File("src/test/resources/tomcat/context.xml");
//		return createWarArchive()
//				  .addAsManifestResource(new FileAsset(context), "context.xml")
//				  .addAsWebInfResource(new FileAsset(web), "web.xml");
//	}

	public static WebArchive createWarArchive() {
		File[] libs = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies().resolve().withTransitivity().asFile();
		File logback = new File("src/test/resources/logback.xml");
		return ShrinkWrap.create(WebArchive.class, "ocelot-test.war")
				  .addAsLibraries(libs)
				  .addAsLibraries(createLibArchive())
				  .addPackages(true, OcelotTest.class.getPackage())
				  .addAsWebInfResource(new FileAsset(logback), "logback.xml")
				  .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}
	
	public static JavaArchive createLibArchive() {
		File bean = new File("src/main/resources/META-INF/beans.xml");
		return ShrinkWrap.create(JavaArchive.class, "ocelot-core.jar")
				  .addPackages(true, Constants.class.getPackage())
				  .addAsManifestResource(new FileAsset(bean), "beans.xml");
	}

	@BeforeClass
	public static void setUpClass() {
		System.out.println("===============================================================================================================");
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		try {
			System.out.println("TRY TO CONNECT");
			URI uri = new URI("ws://localhost:8282/ocelot-test/endpoint");
			wssession = container.connectToServer(OcelotClientEnpoint.class, uri);
			System.out.println("CONNECTED");
		} catch (URISyntaxException | DeploymentException | IOException ex) {
			System.out.println("CONNEXION FAILED : " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	@AfterClass
	public static void tearDownClass() {
		try {
			wssession.close();
		} catch (IOException ex) {
			ex.printStackTrace();
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
		if (messageHandler != null) {
			wssession.removeMessageHandler(messageHandler);
		}
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
	 * Handler de message de type result
	 */
	private class CountDownMessageHandler implements MessageHandler.Whole<String> {
		private final CountDownLatch lock;
		private Object result;
		private Fault fault;
		private final String id;
		CountDownMessageHandler(String id, CountDownLatch lock) {
			this.lock = lock;
			this.id = id;
		}
		@Override
		public void onMessage(String message) {
			logger.debug("RECEIVE RESPONSE FROM SERVER = {}", message);
			MessageToClient messageToClient = MessageToClient.createFromJson(message);
			if(id.equals(messageToClient.getId())) {
				lock.countDown();
				result = messageToClient.getResult();
				fault  = messageToClient.getFault();
			}
		}
		public Object getResult() {
			return result;
		}
		public Fault getFault() {
			return fault;
		}
		
	}

	/**
	 * Vérification qu'un resolver inconnu remonte bien une exception
	 */
	@Test(expected = UnsatisfiedResolutionException.class)
	public void testDataServiceExceptionOnUnknownResolver() {
		System.out.println("failResolveDataService");
		IDataServiceResolver resolver = getResolver("foo");
	}

	/**
	 * Teste de récupération du resolver d'EJB
	 */
	@Test
	public void testGetResolverEjb() {
		System.out.println("getResolverEjb");
		IDataServiceResolver resolver = getResolver(Constants.Resolver.EJB);
		assertNotNull(resolver);
		assertTrue(EJBResolver.class.isInstance(resolver));
	}

	/**
	 * Teste de la récupération d'un EJB
	 */
	@Test
	public void testGetEjb() {
		System.out.println("getEjb");
		IDataServiceResolver resolver = getResolver(Constants.Resolver.EJB);
		try {
			EJBDataService ejb = resolver.resolveDataService(EJBDataService.class);
			assertNotNull(ejb);
			assertTrue(EJBDataService.class.isInstance(ejb));
//			EJBDataService ejb2 = resolver.resolveDataService(EJBDataService.class);
//			assertNotNull(ejb2);
//			assertFalse("two instances of ejb should be differents", ejb.equals(ejb2));
		} catch (DataServiceException ex) {
			fail("EJB not reached");
		}
	}

	/**
	 * Teste de la récupération d'un EJB Singleton
	 */
	@Test
	public void testGetEjbSingleton() {
		System.out.println("getEjbSingleton");
		IDataServiceResolver resolver = getResolver(Constants.Resolver.EJB);
		try {
			SingletonEJBDataService sejb = resolver.resolveDataService(SingletonEJBDataService.class);
			assertNotNull(sejb);
			SingletonEJBDataService sejb2 = resolver.resolveDataService(SingletonEJBDataService.class);
			assertNotNull(sejb2);
			assertEquals(sejb, sejb2);
		} catch (DataServiceException ex) {
			fail("EJB not reached");
		}
	}

	/**
	 * Teste de récupération du resolver de POJO
	 */
	@Test
	public void testGetResolverPojo() {
		System.out.println("getResolverPojo");
		IDataServiceResolver resolver = getResolver(Constants.Resolver.POJO);
		assertNotNull(resolver);
		assertTrue(PojoResolver.class.isInstance(resolver));
	}

	/**
	 * Teste de la récupération d'un Pojo
	 */
	@Test
	public void testGetPojo() {
		System.out.println("getPojo");
		IDataServiceResolver resolver = getResolver(Constants.Resolver.POJO);
		try {
			PojoDataService resolveDataService = resolver.resolveDataService(PojoDataService.class);
			assertNotNull(resolveDataService);
			assertEquals(PojoDataService.class, resolveDataService.getClass());
		} catch (DataServiceException ex) {
			fail("Pojo not reached");
		}
	}

	/**
	 * Teste de récupération du resolver de CDI
	 */
	@Test
	public void testGetResolverCdi() {
		System.out.println("getResolverCdi");
		IDataServiceResolver resolver = getResolver(Constants.Resolver.CDI);
		assertNotNull(resolver);
		assertTrue(CdiResolver.class.isInstance(resolver));
	}
	
	/**
	 * Teste de la récupération d'un cdi bean
	 */
	@Test
	public void testGetCdiBean() {
		System.out.println("getCdiBean");
		IDataServiceResolver resolver = getResolver(Constants.Resolver.CDI);
		try {
			CDIDataService cdids = resolver.resolveDataService(CDIDataService.class);
			assertNotNull(cdids);
			assertEquals(CDIDataService.class, cdids.getClass());
			CDIDataService cdids2 = resolver.resolveDataService(CDIDataService.class);
			assertNotNull(cdids2);
			assertFalse("two instances of cdi bean should be differents", cdids.equals(cdids2));
		} catch (DataServiceException ex) {
			fail("Cdi bean not reached");
		}
	}

	/**
	 * Teste de la récupération d'un cdi bean et verify que la classe est bien managé en controllant la presence d'un injection à l'interieur
	 */
	@Test
	public void testGetCdiBeanIsManaged() {
		System.out.println("getCdiBeanIsManaged");
		IDataServiceResolver resolver = getResolver(Constants.Resolver.CDI);
		try {
			CDIDataService cdids = resolver.resolveDataService(CDIDataService.class);
			assertNotNull(cdids);
			assertEquals(CDIDataService.class, cdids.getClass());
			assertNotNull(cdids.getBeanManager());
		} catch (DataServiceException ex) {
			fail("Cdi bean not reached");
		}
	}

	/**
	 * Teste de la récupération d'un cdi bean singleton, on le récupere deux fois et on check que c'est la meme classe
	 */
	@Test
	public void testGetCdiBeanSingleton() {
		System.out.println("getCdiBeanSingleton");
		IDataServiceResolver resolver = getResolver(Constants.Resolver.CDI);
		try {
			SingletonCDIDataService scdids = resolver.resolveDataService(SingletonCDIDataService.class);
			assertNotNull(scdids);
			SingletonCDIDataService scdids2 = resolver.resolveDataService(SingletonCDIDataService.class);
			assertNotNull(scdids2);
			assertEquals(scdids, scdids2);
		} catch (DataServiceException ex) {
			fail("Cdi bean not reached");
		}
	}

	/**
	 * Teste de récupération du resolver de SPRING
	 */
	@Test
	public void testGetResolverSpring() {
		System.out.println("getResolverSpring");
		// IDataServiceResolver resolver = getResolver(Constants.Resolver.SPRING);
		// TODO tester lappresence du resolver
	}

	/**
	 * Teste de la récupération d'un Pojo
	 */
	@Test
	public void testGetSpringBean() {
		System.out.println("getSpringBean");
		//DataServiceResolver resolver = getResolver(Constants.Resolver.SPRING);
		// TODO 
//		try {
//			Object resolveDataService = resolver.resolveDataService(SpringDataService.class);
//			assertNotNull(resolveDataService);
//			assertEquals(SpringDataService.class, resolveDataService.getClass());
//		} catch (DataServiceException ex) {
//			fail("Spring bean not reached");
//		}
	}

	/**
	 * Vérifie qu'un service n'existant pas remonte bien une exception
	 *
	 * @throws fr.hhdev.ocelot.spi.DataServiceException
	 */
	@Test(expected = DataServiceException.class)
	public void testDataServiceExceptionOnResolveDataService() throws DataServiceException {
		System.out.println("failResolveDataService");
		IDataServiceResolver resolver = getResolver(Constants.Resolver.POJO);
		resolver.resolveDataService(String.class);
	}

	/**
	 * Vérifie que le pojo-resolver remonte le bien PojoDataService
	 */
	@Test
	public void testResolvePojoDataService() {
		System.out.println("resolveDataService");
		try {
			IDataServiceResolver resolver = getResolver(Constants.Resolver.POJO);
			Object dest = resolver.resolveDataService(PojoDataService.class);
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
		String json;
		try {
			json = String.format("{\"%s\":\"%s\",\"%s\":%s}",
					  Constants.Message.ID, uuid, Constants.Message.FAULT, f.toJson());
			MessageToClient result = MessageToClient.createFromJson(json);
			assertEquals(uuid, result.getId());
			assertEquals(f.getClassname(), result.getFault().getClassname());
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			//messageFromClient.getId(), MethodNotFoundException.class);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(null, result);
			Fault fault = messageHandler.getFault();
			assertNotNull(fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(null, result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.getString()), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.getNum()), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.getNumber()), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.getBool()), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.getBoolean()), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			final Date before = new Date();
			Thread.sleep(1000);
			System.out.println("BEFORE = "+before.getTime());
			MessageFromClient messageFromClient = getMessageFromClient("getDate");
			command.setMessage(messageFromClient.toJson());
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertNotNull(result);
			Date res = new Date(Long.parseLong(result.toString()));
			System.out.println("RES = "+res.getTime());
			assertTrue(before.before(res));
			Thread.sleep(1000);
			Date after = new Date();
			System.out.println("AFTER = "+after.getTime());
			assertTrue(after.after(res));
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.getResult()), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.getCollectionInteger()), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.getCollectionResult()), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.getCollectionOfCollectionResult()), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.getMapResult()), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.methodWithNum(1)), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.methodWithNumber(2)), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.methodWithBool(true)), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.methodWithBoolean(false)), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			Date now = new Date();
			MessageFromClient messageFromClient = getMessageFromClient("methodWithDate", getJson(now));
			command.setMessage(messageFromClient.toJson());
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.methodWithDate(now)), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			MessageFromClient messageFromClient = getMessageFromClient("methodWithResult", getJson(new Result(6)));
			command.setMessage(messageFromClient.toJson());
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.methodWithResult(new Result(6))), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument un Integer[]
	 */
	@Test
	public void testMethodWithArrayInteger() {
		System.out.println("methodWithArrayInteger");
		try {
			Integer[] al = new Integer[]{1, 2};
			MessageFromClient messageFromClient = getMessageFromClient("methodWithArrayInteger", getJson(al));
			command.setMessage(messageFromClient.toJson());
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.methodWithArrayInteger(al)), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.methodWithCollectionInteger(cl)), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument un Result[]
	 */
	@Test
	public void testMethodWithArrayResult() {
		System.out.println("methodWithArrayResult");
		try {
			Result[] al = new Result[]{new Result(1), new Result(2)};
			MessageFromClient messageFromClient = getMessageFromClient("methodWithArrayResult", getJson(al));
			command.setMessage(messageFromClient.toJson());
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.methodWithArrayResult(al)), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.methodWithCollectionResult(cl)), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.methodWithMapResult(cl)), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.methodWithCollectionOfCollectionResult(cl)), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT*2, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.methodWithManyParameters("foo", 5, new Result(3), cl)), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(null, result);
			Fault fault = messageHandler.getFault();
			assertEquals(MethodException.class.getName(), fault.getClassname());
		} catch (InterruptedException | IOException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel de methode avec la même signature, sauf les arguments
	 */
	@Test
	public void testMethodWithAlmostSignature1() {
		System.out.println("methodWithAlmostSameSignature1");
		try {
			MessageFromClient messageFromClient = getMessageFromClient("methodWithAlmostSameSignature", getJson(5));
			command.setMessage(messageFromClient.toJson());
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.methodWithAlmostSameSignature(5)), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel de methode avec la même signature, sauf les arguments
	 */
	@Test
	public void testMethodWithAlmostSignature2() {
		System.out.println("methodWithAlmostSameSignature2");
		try {
			MessageFromClient messageFromClient = getMessageFromClient("methodWithAlmostSameSignature", getJson("foo"));
			command.setMessage(messageFromClient.toJson());
			CountDownLatch lock = new CountDownLatch(1);
			messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			wssession.addMessageHandler(messageHandler);
			wssession.getBasicRemote().sendText(command.toJson());
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
			Object result = messageHandler.getResult();
			assertEquals(getJson(destination.methodWithAlmostSameSignature("foo")), result);
			Fault fault = messageHandler.getFault();
			assertEquals(null, fault);
		} catch (InterruptedException | IOException ex) {
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
			final String topic = "mytopic";
			System.out.println("Enregistrement au Topic '" + topic + "'");
			command.setTopic(topic);
			command.setCommand(Constants.Command.Value.SUBSCRIBE);
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(TIMEOUT);
			int nbMsg = 10;
			CountDownLatch lock = new CountDownLatch(nbMsg);
			messageHandler = new CountDownMessageHandler(topic, lock);
			wssession.addMessageHandler(messageHandler);

			MessageToClient toTopic = new MessageToClient();
			toTopic.setId(topic);
			for (int i = 0; i < nbMsg; i++) {
				System.out.println("Envois d'un message au Topic '" + topic + "'");
				toTopic.setResult(new Result(i));
				wsEvent.fire(toTopic);
			}
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			assertEquals("Timeout", 0, lock.getCount());
		} catch (InterruptedException | IOException ex) {
			fail(ex.getMessage());
		}
	}
}
