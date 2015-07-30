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
import fr.hhdev.ocelot.OcelotServices;
import fr.hhdev.ocelot.i18n.Locale;
import fr.hhdev.ocelot.messaging.Fault;
import fr.hhdev.ocelot.messaging.MessageFromClient;
import fr.hhdev.ocelot.messaging.MessageToClient;
import fr.hhdev.ocelot.messaging.MessageEvent;
import fr.hhdev.ocelot.messaging.MessageType;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.container.ResourceContainer;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.weld.exceptions.UnsatisfiedResolutionException;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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

	private final static Random random = new Random();

	private final static Logger logger = LoggerFactory.getLogger(OcelotTest.class);

	private final static long TIMEOUT = 1000;
	private final static String PORT = "8282";

	private final static String ctxpath = "ocelot-test";

	@Inject
	@MessageEvent
	Event<MessageToClient> wsEvent;

	@Inject
	@Any
	private Instance<IDataServiceResolver> resolvers;

	@Inject
	TestTopicAccessControler accessControl;

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
		File localeFr = new File("src/test/resources/test_fr_FR.properties");
		File localeUs = new File("src/test/resources/test_en_US.properties");
		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, ctxpath + ".war")
				  .addAsLibraries(libs)
				  .addAsLibraries(createLibArchive())
				  .addPackages(true, OcelotTest.class.getPackage())
				  .addAsResource(new FileAsset(logback), "logback.xml")
				  .addAsResource(new FileAsset(localeUs), "test_en_US.properties")
				  .addAsResource(new FileAsset(localeFr), "test_fr_FR.properties")
				  .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
		addJSAndProvider("target/test-classes", webArchive, webArchive);
		return webArchive;
	}

	/**
	 * Build ocelot-web.jar
	 *
	 * @return
	 */
	public static JavaArchive createLibArchive() {
		File bean = new File("src/main/resources/META-INF/beans.xml");
		File core = new File("src/main/resources/ocelot-core.js");
		JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class, String.format("ocelot-web-%s.jar", random.nextInt(10)))
				  .addClass(OcelotServices.class)
				  .addPackages(true, "fr.hhdev.ocelot.encoders")
				  .addPackages(true, "fr.hhdev.ocelot.exceptions")
				  .addPackages(true, "fr.hhdev.ocelot.resolvers")
				  .addPackages(true, "fr.hhdev.ocelot.web")
				  .addPackages(true, "fr.hhdev.ocelot.core")
				  .addPackages(true, "fr.hhdev.ocelot.configuration")
				  .addAsManifestResource(new FileAsset(bean), "beans.xml")
				  .addAsResource(new FileAsset(core), "ocelot-core2.js");
		addJSAndProvider("target/classes", javaArchive, javaArchive);
		return javaArchive;
	}

	/**
	 * Add srv_xxxx.js and srv_xxxx.ServiceProvider.class
	 *
	 * @param root
	 * @param resourceContainer
	 * @param classContainer
	 */
	private static void addJSAndProvider(final String root, ResourceContainer resourceContainer, ClassContainer classContainer) {
		File classes = new File(root);
		File[] jsFiles = classes.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				String name = file.getName();
				return file.isFile() && name.startsWith("srv_") && name.endsWith(".js");
			}
		});
		for (File file : jsFiles) {
			String jsName = file.getName();
			String providerPackage = jsName.replaceAll(".js$", "");
			classContainer.addPackage(providerPackage);
			resourceContainer.addAsResource(new FileAsset(file), file.getName());
		}
	}

	@BeforeClass
	public static void setUpClass() {
		System.out.println("===============================================================================================================");
	}

	@AfterClass
	public static void tearDownClass() {
		System.out.println("===============================================================================================================");
	}

	/**
	 * Créer une session localement au test
	 *
	 * @return
	 */
	public static Session createAndGetSession() {
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		try {
			StringBuilder sb = new StringBuilder("ws://localhost:");
			sb.append(PORT).append(Constants.SLASH).append(ctxpath).append(Constants.SLASH).append("ocelot-endpoint");
			URI uri = new URI(sb.toString());
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

	private MessageFromClient getMessageFromClient(Class cls, String operation, String paramNames, String... params) {
		MessageFromClient messageFromClient = getMessageFromClient(cls.getName(), operation, params);
		messageFromClient.setParameterNames(Arrays.asList(paramNames.split(",")));
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
	 * Handler de message de type result Si le handler compte un id, il decomptera le lock uniquement s'il arrive à  récuperer un message avec le bon id Sinon la récupération d'un message décompte le
	 * lock
	 */
	private static class CountDownMessageHandler implements MessageHandler.Whole<String> {

		private final CountDownLatch lock;
		private MessageToClient messageToClient = null;
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
			if (id == null || id.equals(messageToClientIn.getId())) {
				messageToClient = messageToClientIn;
				lock.countDown();
			}
		}

		public MessageToClient getMessageToClient() {
			return messageToClient;
		}
	}

	/**
	 * Cette methode appel via la session passé en argument sur la classe l'operation et retourne le resultat
	 *
	 * @param wsSession
	 * @param clazz
	 * @param operation
	 * @return
	 */
	private Object getResultAfterSendInSession(Session wsSession, Class clazz, String operation, String... params) {
		return getMessageToClientAfterSendInSession(wsSession, clazz.getName(), operation, params).getResponse();
	}

	/**
	 * Cette methode appel via la session passé en argument sur la classe l'operation et decompte le lock
	 *
	 * @param session
	 * @param className
	 * @param operation
	 * @return
	 */
	private void checkMessageAfterSendInSession(Session session, String className, String operation, String... params) {
		// contruction de l'objet command
		MessageFromClient messageFromClient = getMessageFromClient(className, operation, params);
		// on crée un handler client de reception de la réponse
		// send
		session.getAsyncRemote().sendText(messageFromClient.toJson());
	}

	private MessageToClient getMessageToClientAfterSendInSession(Session session, String classname, String operation, String... params) {
		MessageToClient result = null;
		try {
			long t0 = System.currentTimeMillis();
			// construction de la commande
			MessageFromClient messageFromClient = getMessageFromClient(classname, operation, params);
			// on pose un locker
			CountDownLatch lock = new CountDownLatch(1);
			// on crée un handler client de reception de la réponse
			CountDownMessageHandler messageHandler = new CountDownMessageHandler(messageFromClient.getId(), lock);
			session.addMessageHandler(messageHandler);
			// send
			session.getAsyncRemote().sendText(messageFromClient.toJson());
			// wait le delock ou timeout
			boolean await = lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			// lockCount doit être à  zero sinon, on a pas eu le resultat
			long t1 = System.currentTimeMillis();
			assertTrue("Timeout. waiting " + (t1 - t0) + " ms. Remain " + lock.getCount() + "/1 msgs", await);
			// lecture du resultat dans le handler
			result = messageHandler.getMessageToClient();
			assertNotNull(result);
			session.removeMessageHandler(messageHandler);
		} catch (InterruptedException ex) {
			fail("Bean not reached");
		}
		return result;
	}

	/**
	 * Teste de la récupération de 2 instances du mm bean, il doivent être differents on met l'un dans un thread, on lui set value à  500 en dehors du thread on recup un autre bean, et on compare
	 * value, elles doivent etre different
	 */
	private void testDifferentInstancesInDifferentThreads(final Class<? extends GetValue> clazz, String resolverId) {
		final IDataServiceResolver resolver = getResolver(resolverId);
		try {
			// hors session, deux beans session scope doivent être differents
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			executorService.execute(new CallRunnable(clazz, resolver));
			executorService.shutdown();
			GetValue bean2 = resolver.resolveDataService(clazz);
			assertNotNull(bean2);
			Assert.assertNotEquals("two instances of session bean should be differents", bean2.getValue(), 500);
		} catch (DataServiceException ex) {
			fail(resolverId + " bean not reached");
		}
	}

	private static class CallRunnable implements Runnable {

		private final Class<? extends GetValue> clazz;
		private final IDataServiceResolver resolver;

		public CallRunnable(Class<? extends GetValue> clazz, IDataServiceResolver resolver) {
			this.clazz = clazz;
			this.resolver = resolver;
		}

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
	 * Teste de la récupération d'un Singleton On excecute une methode via 2 session distincte sur le même bean. le resultat stocké  l'interieur du bean doit etre identique
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
	 * Teste de la récupération d'un bean session, on le récupere deux fois et on check que le resultat soit identique pour une meme session, puis on crée une new session cela doit donner un resultat
	 * different
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
	 * Récupere la resource via un HttpConnection
	 *
	 * @param resource
	 * @param min
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public HttpURLConnection getConnectionForResource(String resource, boolean min) throws MalformedURLException, IOException {
		StringBuilder sb = new StringBuilder("http://localhost:");
		sb.append(PORT).append(Constants.SLASH).append(ctxpath).append(Constants.SLASH).append(resource);
		if (!min) {
			sb.append("?").append(Constants.MINIFY_PARAMETER).append("=false");
		}
		URL url = new URL(sb.toString());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.connect();
		assertEquals(200, connection.getResponseCode());
		return connection;
	}

	/**
	 * Vérification de la minification des javascripts
	 *
	 * @param resource : nom de la ressource sans slash
	 */
	public void javascriptMinification(final String resource) {
		HttpURLConnection connection1 = null;
		HttpURLConnection connection2 = null;
		try {
			connection1 = getConnectionForResource(resource, true);
			int minlength = connection1.getContentLength();
//			traceFile(connection1.getInputStream());
			connection2 = getConnectionForResource(resource, false);
			int length = connection2.getContentLength();
//			traceFile(connection2.getInputStream());
			assertTrue("Minification of " + resource + " didn't work, same size of file magnifier : " + length + " / minifer : " + minlength, minlength < length);
		} catch (Exception e) {
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

	private void traceFile(InputStream input) {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(input, Constants.UTF_8))) {
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				System.out.write(inputLine.getBytes(Constants.UTF_8));
				System.out.write(Constants.BACKSLASH_N.getBytes(Constants.UTF_8));
			}
		} catch (IOException e) {
		}
	}

	/**
	 * Vérification de la generation du core
	 */
	@Test
	public void testJavascriptGeneration() {
		System.out.println("testJavascriptCoreGeneration");
		try {
			HttpURLConnection connection = getConnectionForResource(Constants.OCELOT + Constants.JS, false);
			boolean replaced;
			try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), Constants.UTF_8))) {
				String inputLine;
				replaced = false;
				while ((inputLine = in.readLine()) != null) {
					assertFalse("Dynamic replacement of " + Constants.CTXPATH + " doen't work", inputLine.contains(Constants.CTXPATH));
					replaced |= inputLine.contains(ctxpath);
				}
			}
			assertTrue("Dynamic replacement of context doen't work", replaced);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Vérification de la minification des services
	 */
//	@Test
	public void testJavascriptCoreMinification() {
		System.out.println("testJavascriptCoreMinification");
		javascriptMinification(Constants.OCELOT_CORE + Constants.JS);
	}

	/**
	 * Vérification qu'un resolver inconnu remonte bien une exception
	 */
	@Test(expected = UnsatisfiedResolutionException.class)
	public void testDataServiceExceptionOnUnknownResolver() {
		System.out.println("failResolveDataService");
		getResolver("foo");
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
	 * Teste de la récupération d'un ejb session (stateful), on le récupere deux fois et on check que le resultat soit identique pour une meme session, puis on crée une new session cela doit donner un
	 * resultat different les EJBs stateful on un scope SESSION
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
	 * Teste de la récupération d'un cdi bean et verify que la classe est bien managé en controllant la presence d'un injection à  l'interieur
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
	 * Teste de la récupération de cdi beans annoté Dependent effectivement il depend du scope de l'objet le gérant donc hors session c'est comme un scope REQUEST
	 */
	@Test
	public void testGetCdiBeanSession() {
		System.out.println("getCdiBeanSession");
		testInstanceSessionScope(SessionCDIDataService.class, Constants.Resolver.CDI);
	}

	/**
	 * Teste de la récupération d'un cdi bean session, on le récupere deux fois et on check que le resultat soit identique pour une meme session, puis on crée une new session cela doit donner un
	 * resultat different
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
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s,\"%s\":%s}",
				  Constants.Message.TYPE, MessageType.RESULT, Constants.Message.ID, uuid, Constants.Message.DEADLINE, 5, Constants.Message.RESPONSE, expectedResult);
		MessageToClient result = MessageToClient.createFromJson(json);
		assertEquals(MessageType.RESULT, result.getType());
		assertEquals(uuid, result.getId());
		assertEquals(5, result.getDeadline());
		assertEquals(MessageType.RESULT, result.getType());
		assertEquals("" + expectedResult, result.getResponse());
	}

	/**
	 * Vérifie la désérialisation de MessageToClient en tant que message pour un topic
	 */
	@Test
	public void testMessageToTopicCreator() {
		System.out.println("MessageToTopic.createFromJson");
		String uuid = UUID.randomUUID().toString();
		Object expectedResult = 1;
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s,\"%s\":%s}",
				  Constants.Message.TYPE, MessageType.MESSAGE, Constants.Message.ID, uuid, Constants.Message.DEADLINE, 5, Constants.Message.RESPONSE, expectedResult);
		MessageToClient result = MessageToClient.createFromJson(json);
		assertEquals(MessageType.MESSAGE, result.getType());
		assertEquals(uuid, result.getId());
		assertEquals(5, result.getDeadline());
		assertEquals("" + expectedResult, result.getResponse());
	}

	/**
	 * Vérifie la désérialisation de MessageToClient result = "foo"
	 */
	@Test
	public void testMessageStringResultToClientCreator() {
		System.out.println("MessageToClient.createFromJson");
		String uuid = UUID.randomUUID().toString();
		String expectedResultJS = "\"foo\"";
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s,\"%s\":%s}",
				  Constants.Message.TYPE, MessageType.RESULT, Constants.Message.ID, uuid, Constants.Message.DEADLINE, 10, Constants.Message.RESPONSE, expectedResultJS);
		MessageToClient result = MessageToClient.createFromJson(json);
		assertEquals(MessageType.RESULT, result.getType());
		assertEquals(uuid, result.getId());
		assertEquals(10, result.getDeadline());
		assertEquals(MessageType.RESULT, result.getType());
		assertEquals(expectedResultJS, result.getResponse());
	}

	/**
	 * Vérifie la désérialisation de MessageToClient result = {"integer": 5, "foo": "foo"}
	 */
	@Test
	public void testMessageObjectResultToClientCreator() {
		System.out.println("MessageToClient.createFromJson");
		String uuid = UUID.randomUUID().toString();
		Object expectedResult = "{\"integer\":5,\"foo\":\"foo\"}";
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s,\"%s\":%s}",
				  Constants.Message.TYPE, MessageType.RESULT, Constants.Message.ID, uuid, Constants.Message.DEADLINE, 20, Constants.Message.RESPONSE, expectedResult);
		MessageToClient result = MessageToClient.createFromJson(json);
		assertEquals(MessageType.RESULT, result.getType());
		assertEquals(uuid, result.getId());
		assertEquals(20, result.getDeadline());
		assertEquals(MessageType.RESULT, result.getType());
		assertEquals(expectedResult, result.getResponse());
	}

	/**
	 * Vérifie la désérialisation de MessageToClient fault = "java.lang.NullPointerException"
	 */
	@Test
	public void testMessageFaultToClientCreator() {
		System.out.println("MessageToClient.createFromJson");
		String uuid = UUID.randomUUID().toString();
		Fault f = new Fault(new NullPointerException("Message d'erreur"), 0);
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s,\"%s\":%s}",
				  Constants.Message.TYPE, MessageType.FAULT, Constants.Message.ID, uuid, Constants.Message.DEADLINE, 0, Constants.Message.RESPONSE, f.toJson());
		MessageToClient result = MessageToClient.createFromJson(json);
		assertEquals(MessageType.FAULT, result.getType());
		assertEquals(uuid, result.getId());
		assertEquals(0, result.getDeadline());
		assertEquals(MessageType.FAULT, result.getType());
		assertEquals(f.getClassname(), ((Fault) result.getResponse()).getClassname());
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
	 * Vérifie l'acces à  la locale
	 */
	@Test
	public void testLocale() {
		Class clazz = OcelotServices.class;
		try (Session wssession = createAndGetSession()) {
			// Par default la locale est US
			String methodName = "getLocale";
			System.out.println(methodName);
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals("{\"language\":\"en\",\"country\":\"US\"}", result);

			// Récup du message en us
			methodName = "getLocaleHello";
			System.out.println(methodName);
			messageToClient = getMessageToClientAfterSendInSession(wssession, EJBDataService.class.getName(), methodName, getJson("hhfrancois"));
			assertEquals(MessageType.RESULT, messageToClient.getType());
			result = messageToClient.getResponse();
			assertEquals("\"Hello hhfrancois\"", result);

			// On change pour le francais
			methodName = "setLocale";
			System.out.println(methodName);
			Locale locale = new Locale();
			locale.setLanguage("fr");
			locale.setCountry("FR");
			messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName, getJson(locale));
			assertEquals(MessageType.RESULT, messageToClient.getType());

			// Vérification
			methodName = "getLocale";
			System.out.println(methodName);
			messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			assertEquals(MessageType.RESULT, messageToClient.getType());
			result = messageToClient.getResponse();
			assertEquals("{\"language\":\"fr\",\"country\":\"FR\"}", result);

			//  Récup du message en francais
			methodName = "getLocaleHello";
			System.out.println(methodName);
			messageToClient = getMessageToClientAfterSendInSession(wssession, EJBDataService.class.getName(), methodName, getJson("hhfrancois"));
			assertEquals(MessageType.RESULT, messageToClient.getType());
			result = messageToClient.getResponse();
			assertEquals("\"Bonjour hhfrancois\"", result);
		} catch (IOException exception) {
		}

		// Une autre session doit en revanche pas être impacté
		try (Session wssession = createAndGetSession()) {
			String methodName = "getLocale";
			System.out.println(methodName);
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals("{\"language\":\"en\",\"country\":\"US\"}", result);
		} catch (IOException exception) {
		}
	}

	/**
	 * Vérifie que l'appel à  une methode inconue remonte bien une erreur adéquate
	 */
	@Test
	public void testMethodUnknow() {
		Class clazz = PojoDataService.class;
		String methodName = "getUnknownMethod";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			assertEquals(MessageType.FAULT, messageToClient.getType());
			Object fault = messageToClient.getResponse();
			assertNotNull(fault);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à  une méthode retournant void (sans resultat)
	 */
	@Test
	public void testMethodNoResult() {
		Class clazz = PojoDataService.class;
		String methodName = "getVoid";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			assertEquals(MessageType.RESULT, messageToClient.getType());
			assertEquals("null", messageToClient.getResponse());
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à  une méthode retournant une String Attention la string retourné est sous la forme "foo" avec les double cà´tes
	 */
	@Test
	public void testGetString() {
		Class clazz = PojoDataService.class;
		String methodName = "getString";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.getString()), result);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à  une méthode retournant un int
	 */
	@Test
	public void testGetNum() {
		Class clazz = PojoDataService.class;
		String methodName = "getNum";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.getNum()), result);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à  une méthode retournant un Integer
	 */
	@Test
	public void testGetNumber() {
		Class clazz = PojoDataService.class;
		String methodName = "getNumber";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.getNumber()), result);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à  une méthode retournant un boolean
	 */
	@Test
	public void testGetBool() {
		Class clazz = PojoDataService.class;
		String methodName = "getBool";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.getBool()), result);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à  une méthode retournant un Boolean
	 */
	@Test
	public void testGetBoolean() {
		Class clazz = PojoDataService.class;
		String methodName = "getBoolean";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.getBoolean()), result);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à  une méthode retournant une date
	 */
	@Test
	public void testGetDate() {
		System.out.println("getDate");
		final Date before = new Date();
		System.out.println("BEFORE = " + before.getTime());
		try (Session wssession = createAndGetSession()) {
			Thread.sleep(1000);
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, PojoDataService.class.getName(), "getDate");
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertNotNull(result);
			Date res = new Date(Long.parseLong(result.toString()));
			System.out.println("RES = " + res.getTime());
			assertTrue(before.before(res));
			Thread.sleep(1000);
			Date after = new Date();
			System.out.println("AFTER = " + after.getTime());
			assertTrue(after.after(res));
		} catch (IOException exception) {
		} catch (InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Teste l'appel à  une méthode retournant un objet de type Result
	 */
	@Test
	public void testGetResult() {
		Class clazz = PojoDataService.class;
		String methodName = "getResult";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.getResult()), result);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à  une méthode retournant une Collection&lt;Integer&gt;
	 */
	@Test
	public void testGetCollectionInteger() {
		Class clazz = PojoDataService.class;
		String methodName = "getCollectionInteger";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.getCollectionInteger()), result);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à  une méthode retournant une Collection&lt;Result&gt;
	 */
	@Test
	public void testGetCollectionResult() {
		Class clazz = PojoDataService.class;
		String methodName = "getCollectionResult";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.getCollectionResult()), result);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à  une méthode retournant une Collection&lt;Collection&lt;Result&gt;&gt;
	 */
	@Test
	public void testGetCollectionOfCollectionResult() {
		Class clazz = PojoDataService.class;
		String methodName = "getCollectionOfCollectionResult";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.getCollectionOfCollectionResult()), result);
		} catch (IOException exception) {
		}
	}

	/**
	 * Teste l'appel à  une méthode retournant une Map&lt;Result&gt;
	 */
	@Test
	public void testGetMapResult() {
		Class clazz = PojoDataService.class;
		String methodName = "getMapResult";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName);
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.getMapResult()), result);
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
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.methodWithNum(1)), result);
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
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.methodWithNumber(2)), result);
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
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.methodWithBool(true)), result);
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
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.methodWithBoolean(false)), result);
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
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.methodWithDate((Date) arg)), result);
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
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.methodWithResult((Result) arg)), result);
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
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.methodWithArrayInteger((Integer[]) arg)), result);
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
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.methodWithCollectionInteger((Collection<Integer>) arg)), result);
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
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.methodWithArrayResult((Result[]) arg)), result);
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
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.methodWithCollectionResult((Collection<Result>) arg)), result);
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
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.methodWithMapResult((Map<String, Result>) arg)), result);
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
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.methodWithCollectionOfCollectionResult((Collection<Collection<Result>>) arg)), result);
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
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.methodWithManyParameters("foo", 5, new Result(3), cl)), result);
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
			assertEquals(MessageType.FAULT, messageToClient.getType());
			Fault fault = (Fault) messageToClient.getResponse();
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
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.methodWithAlmostSameSignature(5)), result);
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
			assertEquals(MessageType.RESULT, messageToClient.getType());
			Object result = messageToClient.getResponse();
			assertEquals(getJson(destination.methodWithAlmostSameSignature("foo")), result);
		} catch (IOException exception) {
		}

	}

	final int NB_SIMUL_METHODS = 500;

	/**
	 * Teste l'appel simultané de methodes sur autant de session differentes<br>
	 */
	@Test
	public void testCallMultiMethodsMultiSessions() {
		int nb = NB_SIMUL_METHODS;
		System.out.println("call" + nb + "MethodsMultiSession");
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
				session.addMessageHandler(new CountDownMessageHandler(lock));
				executorService.execute(new TestThread(clazz, methodName, session));
			}
			boolean await = lock.await(20L * nb, TimeUnit.MILLISECONDS);
			long t1 = System.currentTimeMillis();
			assertTrue("Timeout. waiting " + (t1 - t0) + " ms. Remain " + lock.getCount() + "/" + nb + " msgs", await);
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
		System.out.println("call" + nb + "MethodsMonoSession");
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
			boolean await = lock.await(10L * nb, TimeUnit.MILLISECONDS);
			long t1 = System.currentTimeMillis();
			assertTrue("Timeout. waiting " + (t1 - t0) + " ms. Remain " + lock.getCount() + " msgs", await);
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
			checkMessageAfterSendInSession(wsSession, clazz.getName(), methodName);
		}

	}

	/**
	 * Test d'envoi d'un message generant un message de suppression de cache
	 */
	@Test
	public void testSendRemoveCacheMessage() {
		System.out.println("sendRemoveCacheMessage");
		final String topic = "ocelot-cleancache";
		System.out.println("Enregistrement au Topic '" + topic + "'");
		Class clazz = OcelotServices.class;
		String methodName = "subscribe";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName, getJson(topic));
			assertEquals(MessageType.RESULT, messageToClient.getType());
			assertEquals("null", messageToClient.getResponse());
			long t0 = System.currentTimeMillis();
			MessageFromClient messageFromClient = getMessageFromClient(EJBDataService.class, "generateCleanCacheMessage", "\"a\",\"r\"", getJson(""), getJson(new Result(5)));
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
			assertTrue("Timeout. waiting " + (t1 - t0) + " ms. Remain " + lock.getCount() + "/2 msgs", await);
			wssession.removeMessageHandler(messageHandler);
		} catch (InterruptedException | IOException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Test d'envoi d'un message à  un topic
	 */
	@Test
	public void testSendMessageToTopic() {
		System.out.println("sendMessageToTopic");
		final String topic = "mytopic";
		System.out.println("Enregistrement au Topic '" + topic + "'");
		Class clazz = OcelotServices.class;
		String methodName = "subscribe";
		System.out.println(methodName);
		try (Session wssession = createAndGetSession()) {
			MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz.getName(), methodName, getJson(topic));
			assertEquals(MessageType.RESULT, messageToClient.getType());
			long t0 = System.currentTimeMillis();
//			Thread.sleep(TIMEOUT);
			int nbMsg = 10;
			CountDownLatch lock = new CountDownLatch(nbMsg);
			CountDownMessageHandler messageHandler = new CountDownMessageHandler(topic, lock);
			wssession.addMessageHandler(messageHandler);

			MessageToClient toTopic = new MessageToClient();
			toTopic.setId(topic);
			for (int i = 0; i < nbMsg; i++) {
				System.out.println("Envois d'un message au Topic '" + topic + "'");
				toTopic.setResponse(new Result(i));
				wsEvent.fire(toTopic);
			}
			boolean await = lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			long t1 = System.currentTimeMillis();
			assertTrue("Timeout. waiting " + (t1 - t0) + " ms. Remain " + lock.getCount() + "/" + nbMsg + " msgs", await);
			wssession.removeMessageHandler(messageHandler);
		} catch (InterruptedException | IOException ex) {
			fail(ex.getMessage());
		}
	}
}
