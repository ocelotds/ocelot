/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jetty.connector.JettyConnectorProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.container.ResourceContainer;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.ocelotds.Constants;
import org.ocelotds.OcelotServices;
import org.ocelotds.integration.conditions.AreAtLeastOneDifferent;
import org.ocelotds.integration.dataservices.cdi.RequestCdiDataService;
import org.ocelotds.messaging.Fault;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.messaging.MessageType;

/**
 *
 * @author hhfrancois
 */
public abstract class AbstractOcelotTest {

	protected final static long TIMEOUT = 1000;
	protected final static String PORT = "8282";

	protected final static String CTXPATH = "ocelot-test";

	@BeforeClass
	public static void setUpClass() {
		System.out.println("===============================================================================================================");
	}

	@AfterClass
	public static void tearDownClass() {
		System.out.println("===============================================================================================================");
	}

	@Resource(mappedName = "org.glassfish.embeddable.CommandRunner")
	CommandRunner commandRunner;

	private static boolean INITIALIZED = false;

	@Before
	public void configureLoginRealm() {
		if (!INITIALIZED) {
			System.out.println("**************************************************************************************************************");
			INITIALIZED = true;
			File keyfile = new File("src/test/resources/glassfish/keyfile");
			String keyfilepath = keyfile.getAbsolutePath().replace("\\", "\\\\").replace(":", "\\:");
			System.out.println("KEYFILE : " + keyfilepath);
			CommandResult commandResult = commandRunner.run("create-auth-realm", "--classname=com.sun.enterprise.security.auth.realm.file.FileRealm",
					  "--property=jaas-context=fileRealm:file=" + keyfilepath, "test-file");
			System.out.println(commandResult.getExitStatus().toString() + " " + commandResult.getOutput());
			commandResult = commandRunner.run("list-auth-realms");
			System.out.println(commandResult.getExitStatus().toString() + " " + commandResult.getOutput());
			commandResult = commandRunner.run("list-file-users", "--authrealmname=test-file");
			System.out.println(commandResult.getExitStatus().toString() + " " + commandResult.getOutput());
			System.out.println("**************************************************************************************************************");
		}
	}

	/**
	 * For test api in JEE container, create war
	 *
	 * @return
	 */
	public static WebArchive createWarArchive() {
		File logback = new File("src/test/resources/logback.xml");
		File webxml = new File("src/test/resources/web.xml");
		File glwebxml = new File("src/test/resources/glassfish/glassfish-web.xml");
		File localeFr = new File("src/test/resources/test_fr_FR.properties");
		File localeUs = new File("src/test/resources/test_en_US.properties");
		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, CTXPATH + ".war")
				  .addPackages(true, "org.ocelotds.integration")
				  .addAsResource(logback).addAsResource(localeUs).addAsResource(localeFr)
				  .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
				  .addAsWebInfResource(webxml, "web.xml")
				  .addAsWebInfResource(glwebxml, "glassfish-web.xml");
		addOcelotJar(webArchive);
		addJSAndProvider("target/test-classes", webArchive, webArchive);
		return webArchive;
	}

	/**
	 * Add ocelot-war-xxxxx.jar as library
	 *
	 * @param webArchive
	 */
	public static void addOcelotJar(WebArchive webArchive) {
		File[] imports = Maven.resolver().loadPomFromFile("pom.xml").importCompileAndRuntimeDependencies().resolve().withTransitivity().asFile();
		webArchive.addAsLibraries(imports);
	}

	/**
	 * Add srv_xxxx.js and srv_xxxx.ServiceProvider.class
	 *
	 * @param root
	 * @param resourceContainer
	 * @param classContainer
	 */
	public static void addJSAndProvider(final String root, ResourceContainer resourceContainer, ClassContainer classContainer) {
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

	/**
	 * Récupere la resource via un HttpConnection
	 *
	 * @param resource
	 * @param min
	 * @param auth
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	protected HttpURLConnection getConnectionForResource(String resource, boolean min, boolean auth) throws MalformedURLException, IOException {
		StringBuilder sb = new StringBuilder("http://localhost:");
		sb.append(PORT).append(Constants.SLASH).append(CTXPATH).append(Constants.SLASH).append(resource);
		if (!min) {
			sb.append("?").append(Constants.MINIFY_PARAMETER).append("=false");
		}
		URL url = new URL(sb.toString());
		HttpURLConnection uc = (HttpURLConnection) url.openConnection();
		if (auth) {
			Authenticator.setDefault(new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("demo", "demo".toCharArray());
				}
			});
//			String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary("demo:demo".getBytes());
//			System.out.println(basicAuth);
//			uc.setRequestProperty("Authorization", basicAuth);
		}
		System.out.println("Content-type: " + uc.getContentType());
		System.out.println("Date: " + new Date(uc.getDate()));
		System.out.println("Last modified: " + new Date(uc.getLastModified()));
		System.out.println("Expiration date: " + new Date(uc.getExpiration()));
		System.out.println("Response code: " + uc.getResponseCode());
		assertThat(uc.getResponseCode()).isEqualTo(200).as("'%s' is unreachable", sb);
		return uc;
	}

	/**
	 * Count byte of inputstream
	 *
	 * @param in
	 * @return
	 * @throws IOException
	 */
	protected int countByte(InputStream in) throws IOException {
		int result = 0;
		byte[] buffer = new byte[1024];
		int n = 0;
		while (-1 != (n = in.read(buffer))) {
			result += n;
		}
		return result;
	}

	/**
	 * Transforme un objet en json, attention aux string
	 *
	 * @param obj
	 * @return
	 */
	protected String getJson(Object obj) {
		try {
			if (String.class.isInstance(obj)) {
				return Constants.QUOTE + obj + Constants.QUOTE;
			}
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(obj);
		} catch (IOException ex) {
			return null;
		}
	}

	/**
	 * Check call without result
	 *
	 * @param clazz
	 * @param operation
	 * @param params
	 * @return
	 */
	MessageToClient testRSCallWithoutResult(Class clazz, String operation, String... params) {
		Client client = null;
		try {
			client = getClient();
			return testRSCallWithoutResult(client, clazz, operation, MessageType.RESULT, params);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	MessageToClient testRSCallWithoutResult(String user, String pwd, Class clazz, String operation, String... params) {
		Client client = null;
		try {
			client = getClient(user, pwd);
			return testRSCallWithoutResult(client, clazz, operation, MessageType.RESULT, params);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	MessageToClient testRSCallWithoutResult(Client client, Class clazz, String operation, String... params) {
		return testRSCallWithoutResult(client, clazz, operation, MessageType.RESULT, params);
	}

	/**
	 * Check call failed
	 *
	 * @param clazz
	 * @param operation
	 * @param params
	 * @return
	 */
	MessageToClient testRSCallFailed(Class clazz, String operation, String... params) {
		Client client = null;
		try {
			client = getClient();
			return testRSCallWithoutResult(client, clazz, operation, MessageType.FAULT, params);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	MessageToClient testRSCallFailed(String user, String pwd, Class clazz, String operation, String... params) {
		Client client = null;
		try {
			client = getClient(user, pwd);
			return testRSCallWithoutResult(client, clazz, operation, MessageType.FAULT, params);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	MessageToClient testRSCallFailed(Client client, Class clazz, String operation, String... params) {
		return testRSCallWithoutResult(client, clazz, operation, MessageType.FAULT, params);
	}

	/**
	 * Check call failed with check exception
	 *
	 * @param clazz
	 * @param operation
	 * @param expected
	 * @param params
	 */
	void testRSCallThrowException(Class clazz, String operation, Class<? extends Exception> expected, String... params) {
		Client client = null;
		try {
			client = getClient();
			testRSCallThrowException(client, clazz, operation, expected, params);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	void testRSCallThrowException(String user, String pwd, Class clazz, String operation, Class<? extends Exception> expected, String... params) {
		Client client = null;
		try {
			client = getClient(user, pwd);
			testRSCallThrowException(client, clazz, operation, expected, params);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	void testRSCallThrowException(Client client, Class clazz, String operation, Class<? extends Exception> expected, String... params) {
		MessageToClient mtc = testRSCallWithoutResult(client, clazz, operation, MessageType.FAULT, params);
		Fault fault = (Fault) mtc.getResponse();
		assertThat(fault.getClassname()).isEqualTo(expected.getName());
	}

	/**
	 * Check call with result
	 *
	 * @param clazz
	 * @param operation
	 * @param expected
	 * @param params
	 */
	void testRSCallWithResult(Class clazz, String operation, String expected, String... params) {
		Client client = null;
		try {
			client = getClient();
			testRSCallWithResult(client, clazz, operation, expected, params);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	void testRSCallWithResult(String user, String pwd, Class clazz, String operation, String expected, String... params) {
		Client client = null;
		try {
			client = getClient(user, pwd);
			testRSCallWithResult(client, clazz, operation, expected, params);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	void testRSCallWithResult(Client client, Class clazz, String operation, String expected, String... params) {
		MessageToClient mtc = testRSCallWithoutResult(client, clazz, operation, MessageType.RESULT, params);
		assertThat(mtc.getResponse()).isEqualTo(expected);
	}

	MessageToClient testRSCallWithoutResult(Client client, Class clazz, String operation, MessageType resType, String... params) {
		MessageFromClient mfc = getMessageFromClient(clazz, operation, params);
		return testRSCallWithoutResult(client, mfc, resType);
	}

	MessageToClient testRSCallWithoutResult(Client client, MessageFromClient mfc, MessageType resType) {
		WebTarget target = client.target("http://localhost:" + PORT + "/" + CTXPATH + "/ocelot");
		Form form = new Form("mfc", mfc.toJson());
		Response res = target.path("endpoint").queryParam("monitor", true)
				  .request(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON)
				  .post(Entity.form(form));
		String result = res.readEntity(String.class);
		MessageToClient mtc = null;
		try {
			mtc = MessageToClient.createFromJson(result);
			assertThat(mtc.getType()).isEqualTo(resType);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return mtc;
	}

	/**
	 * Return REST client with user:user authentication
	 *
	 * @return
	 */
	Client getClient() {
		return getClient("user", "user");
	}

	/**
	 * REturn REST client with specific authentication
	 *
	 * @param user
	 * @param pwd
	 * @return
	 */
	Client getClient(String user, String pwd) {
		ClientConfig clientConfig = new ClientConfig();
//		clientConfig.connectorProvider(new ApacheConnectorProvider());
		clientConfig.connectorProvider(new JettyConnectorProvider()); // use jetty connector for keep jsession cookies, apache connector works too, grizzly connector, failed
		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(user, pwd);
		Client client = ClientBuilder.newClient(clientConfig);
		client.register(feature);
		return client;
	}

	/**
	 * Create session
	 *
	 * @return
	 */
	protected Session createAndGetSession() {
		return createAndGetSession(false);

	}

	/**
	 * Create session
	 *
	 * @param monitor
	 * @return
	 */
	protected Session createAndGetSession(boolean monitor) {
		return createAndGetSession("user:user", monitor);
	}

	/**
	 * Create session
	 *
	 * @param userpwd
	 * @param monitor
	 * @return
	 */
	protected Session createAndGetSession(String userpwd, boolean monitor) {
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		try {
			StringBuilder sb = new StringBuilder("ws://localhost:");
			sb.append(PORT).append(Constants.SLASH).append(CTXPATH).append(Constants.SLASH).append("ocelot-endpoint");
			if (monitor) {
				sb.append("?option=monitor");
			}
			URI uri = new URI(sb.toString());
			return container.connectToServer(new Endpoint() {
				@Override
				public void onOpen(Session session, EndpointConfig config) {
				}
			}, createClientEndpointConfigWithAuth(userpwd), uri);
		} catch (URISyntaxException | DeploymentException | IOException ex) {
			ex.printStackTrace();
			fail("CONNEXION FAILED " + ex.getMessage());
		}
		return null;
	}

	private ClientEndpointConfig createClientEndpointConfigWithAuth(final String userpwd) {
		ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {
			@Override
			public void beforeRequest(Map<String, List<String>> headers) {
				headers.put("Authorization", Arrays.asList("Basic " + DatatypeConverter.printBase64Binary(userpwd.getBytes())));
			}
		};
		return ClientEndpointConfig.Builder.create().configurator(configurator).build();
	}

	/**
	 * Cette methode appel via la session passé en argument sur la classe l'operation et retourne le resultat
	 *
	 * @param wsSession
	 * @param clazz
	 * @param operation
	 * @param params
	 * @return
	 */
	protected Object getResultAfterSendInSession(Session wsSession, Class clazz, String operation, String... params) {
		return getMessageToClientAfterSendInSession(wsSession, clazz, operation, params).getResponse();
	}

	protected MessageToClient getMessageToClientAfterSendInSession(final Session session, final Class clazz, final String operation, final String... params) {
		MessageToClient result = null;
		try {
			long t0 = System.currentTimeMillis();
			// construction de la commande
			MessageFromClient messageFromClient = getMessageFromClient(clazz, operation, params);
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
			assertThat(await).as("Timeout. waiting %d ms. Remain %d/1 msgs", t1 - t0, lock.getCount()).isTrue();
			// lecture du resultat dans le handler
			result = messageHandler.getMessageToClient();
			assertThat(result).isNotNull();
			session.removeMessageHandler(messageHandler);
		} catch (InterruptedException ex) {
			fail("Bean not reached");
		}
		return result;
	}

	/**
	 * Crée un message formé avec les argments en parametres
	 *
	 * @param cls
	 * @param operation
	 * @param params
	 * @return
	 */
	protected MessageFromClient getMessageFromClient(Class cls, String operation, String... params) {
		MessageFromClient messageFromClient = new MessageFromClient();
		messageFromClient.setId(UUID.randomUUID().toString());
		messageFromClient.setDataService(cls.getName());
		messageFromClient.setOperation(operation);
		if (params != null) {
			messageFromClient.getParameters().addAll(Arrays.asList(params));
		}
		return messageFromClient;
	}

	protected MessageFromClient getMessageFromClientWithParamNames(Class cls, String operation, String paramNames, String... params) {
		MessageFromClient messageFromClient = getMessageFromClient(cls, operation, params);
		messageFromClient.setParameterNames(Arrays.asList(paramNames.split(",")));
		return messageFromClient;
	}

	public void testCallMultiMethodsInClient(String testname, int nb, final Client client) {
		ExecutorService executor = Executors.newFixedThreadPool(nb);
		try {
			final List<Future<Object>> futures = new ArrayList<>();
			final Class clazz = RequestCdiDataService.class;
			final String methodName = "getValue";
			long t0 = System.currentTimeMillis();
			final CountDownLatch lock = new CountDownLatch(nb);
			for (int i = 0; i < nb; i++) {
				Callable callable = new Callable<Void>() {
					@Override
					public Void call() {
						if (client == null) {
							testRSCallWithoutResult(clazz, methodName);
						} else {
							testRSCallWithoutResult(client, clazz, methodName);
						}
						lock.countDown();
						return null;
					}
				};
				futures.add(executor.submit(callable));
			}
			int i = 0;
			for (Future<Object> fut : futures) {
				try {
					fut.get();
				} catch (InterruptedException | ExecutionException e) {
				}
			}
			boolean await = lock.await(10L * nb, TimeUnit.MILLISECONDS);
			long t1 = System.currentTimeMillis();
			assertThat(await).isTrue().as("Timeout. waiting %f ms. Remain %s/%s msgs", t1 - t0, lock.getCount(), nb);
			System.out.println(testname + " Timeout. waiting " + (t1 - t0) + " ms. Remain " + lock.getCount() + "/" + nb + " msgs");
		} catch (InterruptedException ie) {
			fail(ie.getMessage());
		} finally {
			executor.shutdown();
		}
	}

	protected void testResultRequestScope(final Class clazz) {
		int nb = 50;
		ExecutorService executor = Executors.newFixedThreadPool(nb);
		final List<Future<Object>> futures = new ArrayList<>();
		final Client client = getClient();
		for (int i = 0; i < nb; i++) {
			Callable callable = new Callable<Object>() {
				@Override
				public Object call() {
					return testRSCallWithoutResult(client, clazz, "getValue").getResponse();
				}
			};
			futures.add(executor.submit(callable));
		}
		final Object[] results = new Object[nb];
		int i = 0;
		for (Future<Object> fut : futures) {
			try {
				Object result = fut.get();
				results[i++] = result;
			} catch (InterruptedException | ExecutionException e) {
			}
		}
		assertThat(results).areAtLeastOne(new AreAtLeastOneDifferent(results));
		executor.shutdown();
	}

	/**
	 * do x call of clazz.operation(params)
	 *
	 * @param clazz
	 * @param nb
	 * @param operation
	 * @param params
	 * @return
	 */
	protected Object[] getResultsXTimesInOneSession(Class clazz, int nb, String operation, String... params) {
		Object[] results = new Object[nb];
		Client client = getClient();
		for (int i = 0; i < nb; i++) {
			results[i] = testRSCallWithoutResult(client, clazz, operation, params).getResponse();
		}
		return results;
	}

	/**
	 * Teste de la récupération d'un bean session, on le récupere deux fois et on check que le resultat soit identique pour une meme session, puis on crée une
	 * new session cela doit donner un resultat different
	 *
	 * @param clazz
	 */
	protected void testResultSessionScope(Class clazz) {
		// premiere requete 
		Object[] results = getResultsXTimesInOneSession(clazz, 2, "getValue");
		Object firstResult = results[0];
		Object secondResult = results[1];
		// controle : sur la meme session cela doit se comporter comme un singleton, donc meme resultat
		assertThat(secondResult).isEqualTo(firstResult);
		// troisieme appel sur une session differente
		Object thirdResult = null;
		try (Session wssession = createAndGetSession()) {
			thirdResult = getResultAfterSendInSession(wssession, clazz, "getValue");
		} catch (IOException exception) {
		}
		// controle : sur != session cela doit etre different
		assertThat(secondResult).isNotEqualTo(thirdResult);
	}

	/**
	 * Teste de la récupération d'un Singleton On excecute une methode via 2 session distincte sur le même bean. le resultat stocké  l'interieur du bean doit
	 * etre identique
	 *
	 * @param clazz
	 */
	public void testResultSingletonScope(Class clazz) {
		Client client = getClient();
		// premiere requete 
		Object firstResult = testRSCallWithoutResult(client, clazz, "getValue").getResponse();
		// deuxieme requete 
		Object secondResult = testRSCallWithoutResult(client, clazz, "getValue").getResponse();
		// controle : sur la meme session cela doit se comporter comme un singleton, donc meme resultat
		assertThat(secondResult).isEqualTo(firstResult);
		// troisieme appel sur une session differente
		Object thirdResult = testRSCallWithoutResult(client, clazz, "getValue").getResponse();
		// controle, doit etre identique
		assertThat(firstResult).isEqualTo(thirdResult);
	}

	/**
	 * Test call with result in given session
	 *
	 * @param wssession
	 * @param clazz
	 * @param methodName
	 * @param expected
	 * @param params
	 */
	protected void testCallWithResultInSession(Session wssession, Class clazz, String methodName, String expected, String... params) {
		System.out.println(clazz + "." + methodName);
		MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz, methodName, params);
		Object result = messageToClient.getResponse();
		assertThat(messageToClient.getType()).isEqualTo(MessageType.RESULT);
		assertThat(result).isEqualTo(expected);
	}

	/**
	 * Test call with result in new session
	 *
	 * @param clazz
	 * @param methodName
	 * @param expected
	 * @param params
	 */
	protected void testCallWithResult(Class clazz, String methodName, String expected, String... params) {
		try (Session wssession = createAndGetSession()) {
			testCallWithResultInSession(wssession, clazz, methodName, expected, params);
		} catch (IOException exception) {
		}
	}

	/**
	 * Test call with no result in given session
	 *
	 * @param wssession
	 * @param clazz
	 * @param methodName
	 * @param params
	 */
	protected void testCallWithoutResultInSession(Session wssession, Class clazz, String methodName, String... params) {
		System.out.println(clazz + "." + methodName);
		MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz, methodName, params);
		if (MessageType.FAULT.equals(messageToClient.getType())) {
			System.out.println("FAULT : " + messageToClient.getResponse());
		}
		assertThat(messageToClient.getType()).isEqualTo(MessageType.RESULT);
	}

	/**
	 * Test call with no result in new session
	 *
	 * @param clazz
	 * @param methodName
	 * @param params
	 */
	protected void testCallWithoutResult(Class clazz, String methodName, String... params) {
		try (Session wssession = createAndGetSession()) {
			testCallWithoutResultInSession(wssession, clazz, methodName, params);
		} catch (IOException exception) {
		}
	}

	/**
	 * Subcribe to topic in session
	 *
	 * @param wssession
	 * @param topic
	 */
	protected void subscribeToTopicInSession(Session wssession, String topic) {
		System.out.println("Subscribe to Topic '" + topic + "'");
		testCallWithoutResultInSession(wssession, OcelotServices.class, "subscribe", getJson(topic));
	}

	/**
	 * Subcribe to topic in new session
	 *
	 * @param topic
	 */
	protected void subscribeToTopic(String topic) {
		try (Session wssession = createAndGetSession()) {
			subscribeToTopicInSession(wssession, topic);
		} catch (IOException exception) {
		}
	}

	/**
	 * Test call throw exception in given session
	 *
	 * @param wssession
	 * @param clazz
	 * @param methodName
	 * @param expected
	 * @param params
	 */
	protected void testCallThrowExceptionInSession(Session wssession, Class clazz, String methodName, Class<? extends Exception> expected, String... params) {
		System.out.println(clazz + "." + methodName);
		MessageToClient messageToClient = getMessageToClientAfterSendInSession(wssession, clazz, methodName, params);
		assertThat(messageToClient.getType()).isEqualTo(MessageType.FAULT);
		Fault fault = (Fault) messageToClient.getResponse();
		assertThat(fault.getClassname()).isEqualTo(expected.getName());
	}

	/**
	 * Test call throw exception in new session
	 *
	 * @param clazz
	 * @param methodName
	 * @param expected
	 * @param params
	 */
	protected void testCallThrowException(Class clazz, String methodName, Class<? extends Exception> expected, String... params) {
		try (Session wssession = createAndGetSession()) {
			testCallThrowExceptionInSession(wssession, clazz, methodName, expected, params);
		} catch (IOException exception) {
		}
	}

	/**
	 * Test reception of X msg triggered by call Runnable
	 *
	 * @param wssession
	 * @param nbMsg
	 * @param topic
	 * @param trigger
	 */
	protected void testWaitXMessageToTopic(Session wssession, int nbMsg, String topic, Runnable trigger) {
		try {
			long t0 = System.currentTimeMillis();
			CountDownLatch lock = new CountDownLatch(nbMsg);
			CountDownMessageHandler messageHandler = new CountDownMessageHandler(topic, lock);
			wssession.addMessageHandler(messageHandler);
			trigger.run();
			boolean await = lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			long t1 = System.currentTimeMillis();
			assertThat(await).as("Timeout. waiting %d ms. Remain %d/%d msgs", t1 - t0, lock.getCount(), nbMsg).isTrue();
			wssession.removeMessageHandler(messageHandler);
		} catch (IllegalStateException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Handler de message de type result Si le handler compte un id, il decomptera le lock uniquement s'il arrive à  récuperer un message avec le bon id Sinon la
	 * récupération d'un message décompte le lock
	 */
	protected static class CountDownMessageHandler implements MessageHandler.Whole<String> {

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

	protected class TestThread implements Runnable {

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
	 * Check message after Send request in session
	 *
	 * @param session
	 * @param cls
	 * @param operation
	 * @return
	 */
	private void checkMessageAfterSendInSession(Session session, Class cls, String operation, String... params) {
		// contruction de l'objet command
		MessageFromClient messageFromClient = getMessageFromClient(cls, operation, params);
		// on crée un handler client de reception de la réponse
		// send
		session.getAsyncRemote().sendText(messageFromClient.toJson());
	}

}
