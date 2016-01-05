/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.assertj.core.api.Condition;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.container.ResourceContainer;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.ocelotds.Constants;
import org.ocelotds.OcelotServices;
import org.ocelotds.literals.JsonMarshallerLiteral;
import org.ocelotds.literals.JsonUnmarshallerLiteral;
import org.ocelotds.messaging.Fault;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.messaging.MessageType;
import org.ocelotds.objects.Result;

/**
 *
 * @author hhfrancois
 */
public abstract class AbstractOcelotTest {

	protected final static long TIMEOUT = 1000;
	protected final static String PORT = "8282";

	protected final static String CTXPATH = "ocelot-test";

//	protected final static ExecutorService executor = Executors.newFixedThreadPool(100);
	@BeforeClass
	public static void setUpClass() {
		System.out.println("===============================================================================================================");
	}

	@AfterClass
	public static void tearDownClass() {
		System.out.println("===============================================================================================================");
//		executor.shutdown();		// Check that at least one time results are diferent
	}

	/**
	 * For test api in JEE container, create war
	 *
	 * @return
	 */
	public static WebArchive createWarArchive() {
		File logback = new File("src/test/resources/logback.xml");
		File localeFr = new File("src/test/resources/test_fr_FR.properties");
		File localeUs = new File("src/test/resources/test_en_US.properties");
		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, CTXPATH + ".war")
				  .addPackages(true, OcelotTest.class.getPackage())
				  .addClass(Result.class).addClass(JsonMarshallerLiteral.class).addClass(JsonUnmarshallerLiteral.class)
				  .addAsResource(logback).addAsResource(localeUs).addAsResource(localeFr)
				  .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
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
		String version = "2.6.5-SNAPSHOT";
		File[] imports = Maven.resolver().resolve("org.ocelotds:ocelot-web:" + version, "org.ocelotds:ocelot-core:" + version).withTransitivity().asFile();
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
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	protected HttpURLConnection getConnectionForResource(String resource, boolean min) throws MalformedURLException, IOException {
		StringBuilder sb = new StringBuilder("http://localhost:");
		sb.append(PORT).append(Constants.SLASH).append(CTXPATH).append(Constants.SLASH).append(resource);
		if (!min) {
			sb.append("?").append(Constants.MINIFY_PARAMETER).append("=false");
		}
		URL url = new URL(sb.toString());
		HttpURLConnection uc = (HttpURLConnection) url.openConnection();
		System.out.println("Content-type: " + uc.getContentType());
		System.out.println("Content-encoding: " + uc.getContentEncoding());
		System.out.println("Date: " + new Date(uc.getDate()));
		System.out.println("Last modified: " + new Date(uc.getLastModified()));
		System.out.println("Expiration date: " + new Date(uc.getExpiration()));
		System.out.println("Content-length: " + uc.getContentLength());
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
	 * Create session
	 *
	 * @return
	 */
	protected Session createAndGetSession() {
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		try {
			StringBuilder sb = new StringBuilder("ws://localhost:");
			sb.append(PORT).append(Constants.SLASH).append(CTXPATH).append(Constants.SLASH).append("ocelot-endpoint");
			sb.append("?option=monitor&option=debug");
			URI uri = new URI(sb.toString());
			return container.connectToServer(OcelotClientEnpoint.class, uri);
		} catch (URISyntaxException | DeploymentException | IOException ex) {
			fail("CONNEXION FAILED " + ex.getMessage());
		}
		return null;
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

	protected void testResultRequestScope(final Class clazz) {
		int nb = 50;
		ExecutorService executor = Executors.newFixedThreadPool(nb);
		final List<Future<Object>> futures = new ArrayList<>();
		for (int i = 0; i < nb; i++) {
			Callable callable = new Callable<Object>() {
				@Override
				public Object call() {
					try (final Session wssession = createAndGetSession()) {
						return getResultAfterSendInSession(wssession, clazz, "getValue");
					} catch (IOException exception) {
					}
					return null;
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

	class AreAtLeastOneDifferent extends Condition<Object> {

		final Object[] ref;

		public AreAtLeastOneDifferent(Object[] ref) {
			this.ref = ref;
		}

		@Override
		public boolean matches(Object t) {
			for (Object object : ref) {
				if (!object.equals(t)) {
					return true;
				}
			}
			return false;
		}
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
		try (Session wssession = createAndGetSession()) {
			for (int i = 0; i < nb; i++) {
				results[i] = getResultAfterSendInSession(wssession, clazz, operation, params);
			}
		} catch (IOException exception) {
		}
		return results;
	}

	/**
	 * Teste de la récupération d'un bean session, on le récupere deux fois et on check que le resultat soit identique pour une meme session, puis on crée une new session cela doit donner un resultat
	 * different
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
	 * Teste de la récupération d'un Singleton On excecute une methode via 2 session distincte sur le même bean. le resultat stocké  l'interieur du bean doit etre identique
	 *
	 * @param clazz
	 */
	public void testResultSingletonScope(Class clazz) {
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
		assertThat(secondResult).isEqualTo(firstResult);
		// troisieme appel sur une session differente
		Object thirdResult = null;
		try (Session wssession = createAndGetSession()) {
			thirdResult = getResultAfterSendInSession(wssession, clazz, "getValue");
		} catch (IOException exception) {
		}
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
		assertThat(messageToClient.getType()).isEqualTo(MessageType.RESULT);
		Object result = messageToClient.getResponse();
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
	 * Handler de message de type result Si le handler compte un id, il decomptera le lock uniquement s'il arrive à  récuperer un message avec le bon id Sinon la récupération d'un message décompte le
	 * lock
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
