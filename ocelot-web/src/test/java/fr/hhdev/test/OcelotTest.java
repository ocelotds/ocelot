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
import fr.hhdev.ocelot.annotations.JsCacheResult;
import fr.hhdev.ocelot.annotations.JsCacheStore;
import fr.hhdev.ocelot.i18n.Locale;
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
import fr.hhdev.test.dataservices.GetValue;
import fr.hhdev.test.dataservices.SessionCDIDataService;
import fr.hhdev.test.dataservices.SessionEJBDataService;
import fr.hhdev.test.dataservices.SingletonEJBDataService;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import org.junit.Assert;
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

	private final static String ctxpath = "ocelot-test";

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
		return ShrinkWrap.create(WebArchive.class, ctxpath + ".war")
				  .addAsLibraries(libs)
				  .addAsLibraries(createLibArchive())
				  .addPackages(true, OcelotTest.class.getPackage())
				  .addAsWebInfResource(new FileAsset(logback), "logback.xml")
				  .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	public static JavaArchive createLibArchive() {
		File bean = new File("src/main/resources/META-INF/beans.xml");
		File core = new File("src/main/resources/ocelot-core.js");
		File localeFr = new File("src/main/resources/test_fr_FR.properties");
		File localeUs = new File("src/main/resources/test_en_US.properties");
		return ShrinkWrap.create(JavaArchive.class, "ocelot-web.jar")
				  .addPackages(true, "fr.hhdev.ocelot.encoders")
				  .addPackages(true, "fr.hhdev.ocelot.exceptions")
				  .addPackages(true, "fr.hhdev.ocelot.resolvers")
				  .addPackages(true, "fr.hhdev.ocelot.web")
				  .addPackages(true, "fr.hhdev.ocelot.core")
				  .addPackages(true, "fr.hhdev.ocelot.configuration")
				  .addAsManifestResource(new FileAsset(bean), "beans.xml")
				  .addAsResource(new FileAsset(core), "ocelot-core.js")
				  .addAsResource(new FileAsset(localeUs), "test_en_US.properties")
				  .addAsResource(new FileAsset(localeFr), "test_fr_FR.properties");
	}

	@BeforeClass
	public static void setUpClass() {
		System.out.println("===============================================================================================================");
	}

	@AfterClass
	public static void tearDownClass() {
		System.out.println("===============================================================================================================");
	}

	@Before
	public void setUp() {
		System.out.println("---------------------------------------------------------------------------------------------------------------");
	}

	@After
	public void tearDown() {
		System.out.println("---------------------------------------------------------------------------------------------------------------");
	}

	/**
	 * Créer une session localement au test
	 *
	 * @return
	 */
	public static Session createAndGetSession() {
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		try {
			System.out.println("TRY TO CONNECT");
			URI uri = new URI("ws://localhost:8282/" + ctxpath + "/endpoint");
			return container.connectToServer(OcelotClientEnpoint.class, uri);
		} catch (URISyntaxException | DeploymentException | IOException ex) {
			fail("CONNEXION FAILED " + ex.getMessage());
		}
		return null;
	}

	/**
	 * Crée un message formé avec les argments en parametres
	 *
	 * @param clazz
	 * @param operation
	 * @param params
	 * @return
	 */
	private MessageFromClient getMessageFromClient(String classname, String operation, String... params) {
		MessageFromClient messageFromClient = new MessageFromClient();
		messageFromClient.setId(UUID.randomUUID().toString());
		messageFromClient.setDataService(classname);
		messageFromClient.setOperation(operation);
		if (params != null) {
			messageFromClient.getParameters().addAll(Arrays.asList(params));
		}
		return messageFromClient;
	}

	/**
	 * Transforme un objet en json, attention aux string
	 *
	 * @param obj
	 * @return
	 */
	private String getJson(Object obj) {
		try {
			if (String.class.isInstance(obj)) {
				return "\"" + obj + "\"";
			}
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(obj);
		} catch (IOException ex) {
			return null;
		}
	}

	/**
	 * Handler de message de type result
	 * Si le handler compte un id, il decomptera le lock uniquement s'il arrive à récuperer un message avec le bon id
	 * Sinon la récupération d'un message décompte le lock
	 */
	private class CountDownMessageHandler implements MessageHandler.Whole<String> {

		private final CountDownLatch lock;
		private Object result = null;
		private MessageToClient messageToClient = null;
		private Fault fault = null;
		private String id = null;

		CountDownMessageHandler(String id, CountDownLatch lock) {
			this.lock = lock;
			this.id = id;
		}

		CountDownMessageHandler(CountDownLatch lock) {
			this.lock = lock;
		}

		@Override
		public void onMessage(String message) {
			logger.debug("RECEIVE RESPONSE FROM SERVER = {}", message);
			MessageToClient messageToClientIn = MessageToClient.createFromJson(message);
			if ((id != null && id.equals(messageToClientIn.getId())) || (id == null && messageToClientIn.getId() != null)) {
				messageToClient = messageToClientIn;
				result = messageToClientIn.getResult();
				fault = messageToClientIn.getFault();
				synchronized (lock) {
					lock.countDown();
				}
			}
		}

		public MessageToClient getMessageToClient() {
			return messageToClient;
		}

		public Object getResult() {
			return result;
		}

		public Fault getFault() {
			return fault;
		}

		public CountDownLatch getLock() {
			return lock;
		}

	}

	/**
	 * Cette methode appel via la session passé en argument sur la classe l'operationet retourne le resultat
	 *
	 * @param wsSession
	 * @param clazz
	 * @param operation
	 * @return
	 */
	private Object getResultAfterSendInSession(Session wsSession, Class clazz, String operation, String... params) {
		return getMessageToClientAfterSendInSession(wsSession, clazz.getName(), operation, params).getResult();
	}

	/**
	 * Cette methode appel via la session passé en argument sur la classe l'operation et decompte le lock
	 *
	 * @param session
	 * @param clazz
	 * @param operation
	 * @return
	 */
	private void checkMessageAfterSendInSession(Session session, Class clazz, String operation, String... params) {
		// contruction de l'objet command
		Command cmd = new Command();
		cmd.setCommand(Constants.Command.Value.CALL);
		// construction de lac commande
		MessageFromClient messageFromClient = getMessageFromClient(clazz.getName(), operation, params);
		cmd.setMessage(messageFromClient.toJson());
		// on crée un handler client de reception de la réponse
		try {
			// send
			session.getBasicRemote().sendText(cmd.toJson());
		} catch (IOException ex) {
			fail("Bean not reached");
		}
	}

	private MessageToClient getMessageToClientAfterSendInSession(Session session, String classname, String operation, String... params) {
		MessageToClient result = null;
		try {
			// contruction de l'objet command
			Command cmd = new Command();
			cmd.setCommand(Constants.Command.Value.CALL);
			// construction de lac commande
			MessageFromClient messageFromClient = getMessageFromClient(classname, operation, params);
			cmd.setMessage(messageFromClient.toJson());
			// on pose un locker
			CountDownLatch lock = new CountDownLatch(1);
			// on crée un handler client de reception de la réponse
			CountDownMessageHandler messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			session.addMessageHandler(messageHandler);
			// send
			session.getBasicRemote().sendText(cmd.toJson());
			// wait le delock ou timeout
			lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			// lockCount doit être à zero sinon, on a pas eu le resultat
			assertEquals("Timeout", 0, lock.getCount());
			// lecture du resultat dans le handler
			result = messageHandler.getMessageToClient();
			assertNotNull(result);
			session.removeMessageHandler(messageHandler);
		} catch (InterruptedException | IOException ex) {
			fail("Bean not reached");
		}
		return result;
	}

	/**
	 * Teste de la récupération de 2 instances du mm bean, il doivent être differents on met l'un dans un thread, on lui set value à 500 en dehors du thread on
	 * recup un autre bean, et on compare value, elles doivent etre different
	 */
	private void testDifferentInstancesInDifferentThreads(final Class<? extends GetValue> clazz, String resolverId) {
		final IDataServiceResolver resolver = getResolver(resolverId);
		try {
			// hors session, deux beans session scope doivent être differents
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
						GetValue bean1 = resolver.resolveDataService(clazz);
						bean1.setValue(500);
						Thread.sleep(1000);
						assertNotNull(bean1);
						assertTrue(clazz.isInstance(bean1));
					} catch (DataServiceException | InterruptedException ex) {
					}
				}
			});
			executorService.shutdown();
			GetValue bean2 = resolver.resolveDataService(clazz);
			assertNotNull(bean2);
			Assert.assertNotEquals("two instances of session bean should be differents", bean2.getValue(), 500);
		} catch (DataServiceException ex) {
			fail(resolverId + " bean not reached");
		}
	}

	/**
	 * Teste de la récupération de 2 beans request, il doivent être differents
	 */
	private void testInstanceRequestScope(Class clazz, String resolverId) {
		IDataServiceResolver resolver = getResolver(resolverId);
		try {
			// deux beans doivent être differents
			Object bean1 = resolver.resolveDataService(clazz);
			assertNotNull(bean1);
			assertTrue(clazz.isInstance(bean1));
			Object bean2 = resolver.resolveDataService(clazz);
			assertNotNull(bean2);
			assertFalse("two instances of request bean should be differents", bean1.equals(bean2));
		} catch (DataServiceException ex) {
			fail(resolverId + " bean not reached");
		}
	}

	/**
	 * Teste de la récupération d'un bean avec scope request
	 *
	 * @param clazz
	 */
	public void testResultRequestScope(Class clazz) {
		// premiere requete 
		Object firstResult = null;
		try (Session wssession = createAndGetSession()) {
			firstResult = getResultAfterSendInSession(wssession, clazz, "getValue");
		} catch (IOException exception) {
		}
		// deuxieme requetesur une autre session
		Object secondResult = null;
		try (Session wssession = createAndGetSession()) {
			secondResult = getResultAfterSendInSession(wssession, clazz, "getValue");
		} catch (IOException exception) {
		}
		// controle
		Assert.assertNotEquals("two instances of request bean should be differents", firstResult, secondResult); // doit etre different
	}

	/**
	 * Teste de la récupération de 2 beans singleton, il doivent être identiques
	 */
	private void testInstanceSingletonScope(Class clazz, String resolverId) {
		IDataServiceResolver resolver = getResolver(resolverId);
		try {
			// deux singletons doivent être identiques
			Object singleton1 = resolver.resolveDataService(clazz);
			assertNotNull(singleton1);
			Object singleton2 = resolver.resolveDataService(clazz);
			assertNotNull(singleton2);
			assertEquals(singleton1, singleton2);
		} catch (DataServiceException ex) {
			fail(resolverId + " bean not reached");
		}
	}

	/**
	 * Teste de la récupération d'un Singleton On excecute une methode via 2 session distincte sur le même bean. le resultat stockéà l'interieur du bean doit
	 * etre identique
	 *
	 * @param clazz
	 */
	public void testResultSingletonScope(Class clazz) {
		// premiere requete 
		Object firstResult = null;
		try (Session wssession = createAndGetSession()) {
			firstResult = getResultAfterSendInSession(wssession, clazz, "getValue");
		} catch (IOException exception) {
		}
		// deuxieme requete sur autre session
		Object secondResult = null;
		try (Session wssession = createAndGetSession()) {
			secondResult = getResultAfterSendInSession(wssession, clazz, "getValue");
		} catch (IOException exception) {
		}
		// controle, doit etre identique
		assertEquals(firstResult, secondResult);
	}

	/**
	 * Teste de la récupération de 2 beans session hors sessions, il doivent être differents
	 */
	private void testInstanceSessionScope(Class clazz, String resolverId) {
		IDataServiceResolver resolver = getResolver(resolverId);
		try {
			// hors session, deux beans session scope doivent être differents
			Object bean1 = resolver.resolveDataService(clazz);
			assertNotNull(bean1);
			assertTrue(clazz.isInstance(bean1));
			Object bean2 = resolver.resolveDataService(clazz);
			assertNotNull(bean2);
			assertFalse("two instances of session bean should be differents", bean1.equals(bean2));
		} catch (DataServiceException ex) {
			fail(resolverId + " bean not reached");
		}
	}

	/**
	 * Teste de la récupération d'un bean session, on le récupere deux fois et on check que le resultat soit identique pour une meme session, puis on crée une
	 * new session cela doit donner un resultat different
	 */
	private void testResultSessionScope(Class clazz) {
		// premiere requete 
		Object firstResult = null;
		Object secondResult = null;
		try (Session wssession = createAndGetSession()) {
			firstResult = getResultAfterSendInSession(wssession, clazz, "getValue");
			// deuxieme requete 
			secondResult = getResultAfterSendInSession(wssession, clazz, "getValue");
		} catch (IOException exception) {
		}
		// controle : sur la meme session cela doit se comporter comme un singleton, donc meme resultat
		assertEquals(secondResult, firstResult);
		// troisiement appel sur une session differente
		Object thirdResult = null;
		try (Session wssession = createAndGetSession()) {
			thirdResult = getResultAfterSendInSession(wssession, clazz, "getValue");
		} catch (IOException exception) {
		}
		// controle : sur != session cela doit etre different
		Assert.assertNotEquals(secondResult, thirdResult);
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
	 * Teste de la récupération d'EJBs par default les EJB on un scope REQUEST
	 */
	@Test
	public void testGetEjbs() {
		System.out.println("getEjbs");
		String resolverId = Constants.Resolver.EJB;
		testDifferentInstancesInDifferentThreads(EJBDataService.class, resolverId);
	}

	/**
	 * Teste de la récupération d'EJBs statefull les EJBs statefull on un scope REQUEST hors session il doivent etre donc distinct
	 */
	@Test
	public void testGetEJBStatefull() {
		System.out.println("getEJBSession");
		String resolverId = Constants.Resolver.EJB;
//		testDifferentInstancesInDifferentThreads(SessionEJBDataService.class, resolverId);
		testInstanceSessionScope(SessionEJBDataService.class, Constants.Resolver.EJB);
	}

	/**
	 * Teste de la récupération d'un ejb session (stateful), on le récupere deux fois et on check que le resultat soit identique pour une meme session, puis on
	 * crée une new session cela doit donner un resultat different les EJBs stateful on un scope SESSION
	 */
	@Test
	public void testGetResultEJBSession() {
		System.out.println("getResultEJBSession");
		testResultSessionScope(SessionEJBDataService.class);
	}

	/**
	 * Teste de la récupération d'EJBs singleton les EJBs singleton on un scope APPLICATION
	 */
	@Test
	public void testGetEJBSingleton() {
		System.out.println("getEJBSingleton");
		testInstanceSingletonScope(SingletonEJBDataService.class, Constants.Resolver.EJB);
	}

	/**
	 * Teste de la récupération d'un EJB Singleton les EJBs Singleton on un scope APPLICATION
	 */
	@Test
	public void testGetResultEjbSingleton() {
		System.out.println("getResultEjbSingleton");
		testResultSingletonScope(SingletonEJBDataService.class);
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
	 * Teste de la récupération de cdi beans par default les EJB on un scope REQUEST
	 */
	@Test
	public void testGetCdiBeans() {
		System.out.println("getCdiBeans");
		testInstanceRequestScope(CDIDataService.class, Constants.Resolver.CDI);
	}

	/**
	 * Teste de la récupération de cdi beans et test les resultats par default les EJB on un scope REQUEST
	 */
	@Test
	public void testGetResultCdiBeans() {
		System.out.println("getResultCdiBeans");
		testResultRequestScope(CDIDataService.class);
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
	 * Teste de la récupération de cdi beans annoté Dependent effectivement il depend du scope de l'objet le gérant donc hors session c'est comme un scope
	 * REQUEST
	 */
	@Test
	public void testGetCdiBeanSession() {
		System.out.println("getCdiBeanSession");
		testInstanceSessionScope(SessionCDIDataService.class, Constants.Resolver.CDI);
	}

	/**
	 * Teste de la récupération d'un cdi bean session, on le récupere deux fois et on check que le resultat soit identique pour une meme session, puis on crée
	 * une new session cela doit donner un resultat different
	 */
	@Test
	public void testGetResultCdiBeanSession() {
		System.out.println("getResultCdiBeanSession");
		testResultSessionScope(SessionCDIDataService.class);
	}

	/**
	 * Teste de la récupération d'un bean CDI singleton les singleton on un scope APPLICATION
	 */
	@Test
	public void testGetCdiBeanSingleton() {
		System.out.println("getCdiBeanSingleton");
		testInstanceSingletonScope(SingletonCDIDataService.class, Constants.Resolver.CDI);
	}

	/**
	 * Teste de la récupération d'un cdi bean singleton, on le récupere deux fois et on check que c'est la meme classe
	 */
	@Test
	public void testGetResultCdiBeanSingleton() {
		System.out.println("getResultCdiBeanSingleton");
		testResultSingletonScope(SingletonCDIDataService.class);
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
		String json = String.format("{\"%s\":\"%s\",\"%s\":%s,\"%s\":\"%s\",\"%s\":%s}",
				  Constants.Message.ID, uuid, Constants.Message.DEADLINE, 0, Constants.Message.STORE, JsCacheStore.NONE, Constants.Message.RESULT, expectedResult);
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
		String json = String.format("{\"%s\":\"%s\",\"%s\":%s,\"%s\":\"%s\",\"%s\":%s}",
				  Constants.Message.ID, uuid, Constants.Message.DEADLINE, 0, Constants.Message.STORE, JsCacheStore.NONE, Constants.Message.RESULT, expectedResultJS);
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
		String json = String.format("{\"%s\":\"%s\",\"%s\":%s,\"%s\":\"%s\",\"%s\":%s}",
				  Constants.Message.ID, uuid, Constants.Message.DEADLINE, 0, Constants.Message.STORE, JsCacheStore.NONE, Constants.Message.RESULT, expectedResult);
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
		Fault f = new Fault(new NullPointerException("Message d'erreur"), 0);
		String json;
		try {
			json = String.format("{\"%s\":\"%s\",\"%s\":%s,\"%s\":\"%s\",\"%s\":%s}",
					  Constants.Message.ID, uuid, Constants.Message.DEADLINE, 0, Constants.Message.STORE, JsCacheStore.NONE, Constants.Message.FAULT, f.toJson());
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
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":[\"%s\",\"%s\"],\"%s\":[%s,%s]}",
				  Constants.Message.ID, uuid, Constants.Message.DATASERVICE, PojoDataService.class.getName(), Constants.Message.OPERATION, operation,
				  Constants.Message.ARGUMENTNAMES, "r", "m", Constants.Message.ARGUMENTS, resultJS, mapResultJS);
		MessageFromClient result = MessageFromClient.createFromJson(json);
		assertEquals(uuid, result.getId());
		assertEquals(PojoDataService.class.getName(), result.getDataService());
		assertEquals(operation, result.getOperation());
		List<String> parameters = result.getParameters();
		assertEquals(resultJS, parameters.get(0));
		assertEquals(mapResultJS, parameters.get(1));
	}

	/**
	 * Vérifie l'acces à la locale
	 */
	@Test
	public void testLocale() {
		try (Session wssession = createAndGetSession()) {
			// Par default la locale est US
			String methodName = "getLocale";
			System.out.println(methodName);
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, "fr.hhdev.ocelot.OcelotServices", methodName);
			Object result = messageToClient.getResult();
			assertEquals("{\"language\":\"en\",\"country\":\"US\"}", result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);

			// Récup du message en us
			methodName = "getLocaleHello";
			System.out.println(methodName);
			messageToClient = getMessageToClientAfterSendInSession(wssession, EJBDataService.class.getName(), methodName, getJson("François"));
			result = messageToClient.getResult();
			assertEquals("\"Hello François\"", result);
			fault = messageToClient.getFault();
			assertEquals(null, fault);

			// On change pour le francais
			methodName = "setLocale";
			System.out.println(methodName);
			Locale locale = new Locale();
			locale.setLanguage("fr");
			locale.setCountry("FR");
			messageToClient = getMessageToClientAfterSendInSession(wssession, "fr.hhdev.ocelot.OcelotServices", methodName, getJson(locale));
			result = messageToClient.getResult();
			assertEquals(null, result);
			fault = messageToClient.getFault();
			assertEquals(null, fault);

			// Vérification
			methodName = "getLocale";
			System.out.println(methodName);
			messageToClient = getMessageToClientAfterSendInSession(wssession, "fr.hhdev.ocelot.OcelotServices", methodName);
			result = messageToClient.getResult();
			assertEquals("{\"language\":\"fr\",\"country\":\"FR\"}", result);
			fault = messageToClient.getFault();
			assertEquals(null, fault);

			//  Récup du message en francais
			methodName = "getLocaleHello";
			System.out.println(methodName);
			messageToClient = getMessageToClientAfterSendInSession(wssession, EJBDataService.class.getName(), methodName, getJson("François"));
			result = messageToClient.getResult();
			assertEquals("\"Bonjour François\"", result);
			fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}

		// Une autre session doit en revanche pas être impacté
		try (Session wssession = createAndGetSession()) {
			String methodName = "getLocale";
			System.out.println(methodName);
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, "fr.hhdev.ocelot.OcelotServices", methodName);
			Object result = messageToClient.getResult();
			assertEquals("{\"language\":\"en\",\"country\":\"US\"}", result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Vérifie que l'appel à une methode inconue remonte bien une erreur adéquate
	 */
	@Test
	public void testMethodUnknow() {
		Class clazz = PojoDataService.class;
		String methodName = "getUnknownMethod";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			Object result = messageToClient.getResult();
			assertEquals(null, result);
			Fault fault = messageToClient.getFault();
			assertNotNull(fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à une méthode retournant void (sans resultat)
	 */
	@Test
	public void testMethodNoResult() {
		Class clazz = PojoDataService.class;
		String methodName = "getVoid";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			Object result = messageToClient.getResult();
			assertEquals(null, result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à une méthode retournant une String Attention la string retourné est sous la forme "foo" avec les double côtes
	 */
	@Test
	public void testGetString() {
		Class clazz = PojoDataService.class;
		String methodName = "getString";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.getString()), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à une méthode retournant un int
	 */
	@Test
	public void testGetNum() {
		Class clazz = PojoDataService.class;
		String methodName = "getNum";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.getNum()), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à une méthode retournant un Integer
	 */
	@Test
	public void testGetNumber() {
		Class clazz = PojoDataService.class;
		String methodName = "getNumber";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.getNumber()), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à une méthode retournant un boolean
	 */
	@Test
	public void testGetBool() {
		Class clazz = PojoDataService.class;
		String methodName = "getBool";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.getBool()), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à une méthode retournant un Boolean
	 */
	@Test
	public void testGetBoolean() {
		Class clazz = PojoDataService.class;
		String methodName = "getBoolean";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.getBoolean()), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à une méthode retournant une date
	 */
	@Test
	public void testGetDate() {
		System.out.println("getDate");
		final Date before = new Date();
		System.out.println("BEFORE = " + before.getTime());
		try (Session wssession = createAndGetSession()) {
			Thread.sleep(1000);
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, PojoDataService.class.getName(), "getDate");
			Object result = messageToClient.getResult();
			assertNotNull(result);
			Date res = new Date(Long.parseLong(result.toString()));
			System.out.println("RES = " + res.getTime());
			assertTrue(before.before(res));
			Thread.sleep(1000);
			Date after = new Date();
			System.out.println("AFTER = " + after.getTime());
			assertTrue(after.after(res));
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		} catch (InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel à une méthode retournant un objet de type Result
	 */
	@Test
	public void testGetResult() {
		Class clazz = PojoDataService.class;
		String methodName = "getResult";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.getResult()), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à une méthode retournant une Collection&lt;Integer&gt;
	 */
	@Test
	public void testGetCollectionInteger() {
		Class clazz = PojoDataService.class;
		String methodName = "getCollectionInteger";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.getCollectionInteger()), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à une méthode retournant une Collection&lt;Result&gt;
	 */
	@Test
	public void testGetCollectionResult() {
		Class clazz = PojoDataService.class;
		String methodName = "getCollectionResult";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.getCollectionResult()), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à une méthode retournant une Collection&lt;Collection&lt;Result&gt;&gt;
	 */
	@Test
	public void testGetCollectionOfCollectionResult() {
		Class clazz = PojoDataService.class;
		String methodName = "getCollectionOfCollectionResult";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.getCollectionOfCollectionResult()), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à une méthode retournant une Map&lt;Result&gt;
	 */
	@Test
	public void testGetMapResult() {
		Class clazz = PojoDataService.class;
		String methodName = "getMapResult";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.getMapResult()), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument un int
	 */
	@Test
	public void testMethodWithNum() {
		Class clazz = PojoDataService.class;
		String methodName = "methodWithNum";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName, getJson(1));
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.methodWithNum(1)), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument un Integer
	 */
	@Test
	public void testMethodWithNumber() {
		Class clazz = PojoDataService.class;
		String methodName = "methodWithNumber";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName, getJson(2));
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.methodWithNumber(2)), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument un boolean
	 */
	@Test
	public void testMethodWithBool() {
		Class clazz = PojoDataService.class;
		String methodName = "methodWithBool";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName, getJson(true));
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.methodWithBool(true)), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument un Boolean
	 */
	@Test
	public void testMethodWithBoolean() {
		Class clazz = PojoDataService.class;
		String methodName = "methodWithBoolean";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName, getJson(false));
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.methodWithBoolean(false)), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument une Date
	 */
	@Test
	public void testMethodWithDate() {
		Class clazz = PojoDataService.class;
		String methodName = "methodWithDate";
		System.out.println(methodName);
		Object arg = new Date();
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName, getJson(arg));
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.methodWithDate((Date) arg)), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument un objet Result
	 */
	@Test
	public void testMethodWithResult() {
		Class clazz = PojoDataService.class;
		String methodName = "methodWithResult";
		System.out.println(methodName);
		Object arg = new Result(6);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName, getJson(arg));
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.methodWithResult((Result) arg)), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument un Integer[]
	 */
	@Test
	public void testMethodWithArrayInteger() {
		Class clazz = PojoDataService.class;
		String methodName = "methodWithArrayInteger";
		System.out.println(methodName);
		Object arg = new Integer[]{1, 2};
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName, getJson(arg));
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.methodWithArrayInteger((Integer[]) arg)), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument une Collection&lt;Integer&gt;
	 */
	@Test
	public void testMethodWithCollectionInteger() {
		Class clazz = PojoDataService.class;
		String methodName = "methodWithCollectionInteger";
		System.out.println(methodName);
		Object arg = destination.getCollectionInteger();
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName, getJson(arg));
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.methodWithCollectionInteger((Collection<Integer>) arg)), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument un Result[]
	 */
	@Test
	public void testMethodWithArrayResult() {
		Class clazz = PojoDataService.class;
		String methodName = "methodWithArrayResult";
		System.out.println(methodName);
		Object arg = new Result[]{new Result(1), new Result(2)};
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName, getJson(arg));
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.methodWithArrayResult((Result[]) arg)), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument une Collection&lt;Result&gt;
	 */
	@Test
	public void testMethodWithCollectionResult() {
		Class clazz = PojoDataService.class;
		String methodName = "methodWithCollectionResult";
		System.out.println(methodName);
		Object arg = destination.getCollectionResult();
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName, getJson(arg));
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.methodWithCollectionResult((Collection<Result>) arg)), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument une Map&lt;Result&gt;
	 */
	@Test
	public void testMethodWithMapResult() {
		Class clazz = PojoDataService.class;
		String methodName = "methodWithMapResult";
		System.out.println(methodName);
		Object arg = destination.getMapResult();
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName, getJson(arg));
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.methodWithMapResult((Map<String, Result>) arg)), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en argument une Collection&lt;Collection&lt;Result&gt;&gt;
	 */
	@Test
	public void testMethodWithCollectionOfCollectionResult() {
		Class clazz = PojoDataService.class;
		String methodName = "methodWithCollectionOfCollectionResult";
		System.out.println(methodName);
		Object arg = destination.getCollectionOfCollectionResult();
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName, getJson(arg));
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.methodWithCollectionOfCollectionResult((Collection<Collection<Result>>) arg)), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel d'une méthode prenant en arguments plusieurs objets un objet REsultet une Collection&lt;Result&gt;
	 */
	@Test
	public void testMethodWithManyParameters() {
		Class clazz = PojoDataService.class;
		String methodName = "methodWithManyParameters";
		System.out.println(methodName);
		Collection<String> cl = new ArrayList<>();
		cl.add("foo");
		cl.add("foo");
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName, getJson("foo"), getJson(5), getJson(new Result(3)), getJson(cl));
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.methodWithManyParameters("foo", 5, new Result(3), cl)), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel d'une méthode levant une exception MethodException
	 */
	@Test
	public void testMethodThatThrowException() {
		Class clazz = PojoDataService.class;
		String methodName = "methodThatThrowException";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			Object result = messageToClient.getResult();
			assertEquals(null, result);
			Fault fault = messageToClient.getFault();
			assertEquals(MethodException.class.getName(), fault.getClassname());
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel de methode avec la même signature, sauf les arguments
	 */
	@Test
	public void testMethodWithAlmostSameSignature1() {
		Class clazz = PojoDataService.class;
		String methodName = "methodWithAlmostSameSignature";
		System.out.println(methodName + "(int)");
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName, getJson(5));
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.methodWithAlmostSameSignature(5)), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel de methode avec la même signature, sauf les arguments
	 */
	@Test
	public void testMethodWithAlmostSameSignature2() {
		Class clazz = PojoDataService.class;
		String methodName = "methodWithAlmostSameSignature";
		System.out.println(methodName + "(string)");
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName, getJson("foo"));
			Object result = messageToClient.getResult();
			assertEquals(getJson(destination.methodWithAlmostSameSignature("foo")), result);
			Fault fault = messageToClient.getFault();
			assertEquals(null, fault);
		} catch (IOException exception) {
		}

	}

	final int NB_SIMUL_METHODS = 500;
	/**
	 * Teste l'appel simultané de methodes sur autant de session differentes<br>
	 * TODO Voir pourquoi cela ne marche pas au dela des 900 cnx
	 */
	@Test
	public void testCallMultiMethodsMultiSessions() {
		int nb = NB_SIMUL_METHODS;
		System.out.println("call"+nb+"MethodsMultiSession");
		ExecutorService executorService = Executors.newFixedThreadPool(nb);
		final List<Session> sessions = new ArrayList<>();
		try {
			final Class clazz = EJBDataService.class;
			final String methodName = "getValue";
			long t0 = System.currentTimeMillis();
			final CountDownLatch lock = new CountDownLatch(nb);
			for (int i = 0; i < nb; i++) {
				Session session = OcelotTest.createAndGetSession();
				sessions.add(session);
				CountDownMessageHandler messageHandler = new CountDownMessageHandler(lock);
				session.addMessageHandler(messageHandler);
				executorService.execute(new TestThread(clazz, methodName, session));
			}
			lock.await(10*nb, TimeUnit.MILLISECONDS);
			long t1 = System.currentTimeMillis();
			System.out.println("Excecution de " + nb + " appels multisession en " + (t1 - t0) + "ms");
			assertEquals("Timeout", 0, lock.getCount());
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
	 * Teste l'appel simultané de methodes sur une seule session<br>
	 */
	@Test
	public void testCallMultiMethodsMonoSessions() {
		int nb = NB_SIMUL_METHODS;
		System.out.println("call"+nb+"MethodsMonoSession");
		ExecutorService executorService = Executors.newFixedThreadPool(nb);
		try (Session session = OcelotTest.createAndGetSession()) {
			final CountDownLatch lock = new CountDownLatch(nb);
			CountDownMessageHandler messageHandler = new CountDownMessageHandler(lock);
			session.addMessageHandler(messageHandler);
			final Class clazz = EJBDataService.class;
			final String methodName = "getValue";
			long t0 = System.currentTimeMillis();
			for (int i = 0; i < nb; i++) {
				executorService.execute(new TestThread(clazz, methodName, session));
			}
			lock.await(10*nb, TimeUnit.MILLISECONDS);
			long t1 = System.currentTimeMillis();
			System.out.println("Excecution de " + nb + " appels monosession en " + (t1 - t0) + "ms");
			assertEquals("Timeout", 0, lock.getCount());
		} catch (IOException | InterruptedException ex) {
			fail(ex.getMessage());
		} finally {
			executorService.shutdown();
		}
	}

	private class TestThread implements Runnable {

		private final Class clazz;
		private final String methodName;
		private final Session wsSession;

		public TestThread(Class clazz, String methodName, Session wsSession) {
			this.clazz = clazz;
			this.methodName = methodName;
			this.wsSession = wsSession;
		}

		@Override
		public void run() {
			checkMessageAfterSendInSession(wsSession, clazz, methodName);
		}

	}

	/**
	 * Test d'envoi d'un message à un topic
	 */
	@Test
	public void testSendMessageToTopic() {
		System.out.println("sendMessageToTopic");
		final String topic = "mytopic";
		System.out.println("Enregistrement au Topic '" + topic + "'");
		Command command = new Command();
		command.setCommand(Constants.Command.Value.SUBSCRIBE);
		command.setMessage("\"" + topic + "\"");
		try (Session wssession = createAndGetSession()) {
			wssession.getBasicRemote().sendText(command.toJson());
			Thread.sleep(TIMEOUT);
			int nbMsg = 10;
			CountDownLatch lock = new CountDownLatch(nbMsg);
			CountDownMessageHandler messageHandler = new CountDownMessageHandler(topic, lock);
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
			wssession.removeMessageHandler(messageHandler);
		} catch (InterruptedException | IOException ex) {
			fail(ex.getMessage());
		}
	}
}
