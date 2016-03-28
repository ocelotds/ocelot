/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
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
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
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
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
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
import org.glassfish.jersey.jetty.connector.JettyConnectorProvider;
import org.ocelotds.messaging.ConstraintViolation;
import org.ocelotds.integration.objects.ResultMonitored;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.ocelotds.objects.Options;

/**
 *
 * @author hhfrancois
 */
public abstract class AbstractOcelotTest {

	protected final static long TIMEOUT = 1000;
	protected final static String PORT = "8282";

	protected final static String CTXPATH = "ocelot-test";

	@Resource(lookup = "java:comp/DefaultManagedExecutorService")
	ManagedExecutorService managedExecutor;

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
				  .addPackages(true, AbstractOcelotTest.class.getPackage())
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
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	protected HttpURLConnection getConnectionForResource(String resource) throws MalformedURLException, IOException {
		StringBuilder sb = new StringBuilder("http://localhost:");
		sb.append(PORT).append(Constants.SLASH).append(CTXPATH).append(Constants.SLASH).append(resource);
		URL url = new URL(sb.toString());
		HttpURLConnection uc = (HttpURLConnection) url.openConnection();
//		Authenticator.setDefault(new Authenticator() {
//			@Override
//			protected PasswordAuthentication getPasswordAuthentication() {
//				return new PasswordAuthentication("demo", "demo".toCharArray());
//			}
//		});
//    ou
//		String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary("demo:demo".getBytes());
//		System.out.println(basicAuth);
//		uc.setRequestProperty("Authorization", basicAuth);
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
	protected static String getJson(Object obj) {
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
	 * Becareful result is not unmarshalled
	 *
	 * @param json
	 * @return
	 */
	protected static MessageToClient mtcFromJson(String json) {
		try (JsonReader reader = Json.createReader(new StringReader(json))) {
			JsonObject root = reader.readObject();
			MessageToClient message = new MessageToClient();
			message.setId(root.getString(Constants.Message.ID));
			message.setTime(root.getInt(Constants.Message.TIME));
			message.setType(MessageType.valueOf(root.getString(Constants.Message.TYPE)));
			message.setDeadline(root.getInt(Constants.Message.DEADLINE));
			if (null != message.getType()) {
				switch (message.getType()) {
					case FAULT:
						JsonObject faultJs = root.getJsonObject(Constants.Message.RESPONSE);
						Fault f = Fault.createFromJson(faultJs.toString());
						message.setFault(f);
						break;
					case MESSAGE:
						message.setResult("" + root.get(Constants.Message.RESPONSE));
						message.setType(MessageType.MESSAGE);
						break;
					case CONSTRAINT:
						JsonArray result = root.getJsonArray(Constants.Message.RESPONSE);
						List<ConstraintViolation> list = new ArrayList<>();
						for (JsonValue jsonValue : result) {
							list.add(getJava(ConstraintViolation.class, ((JsonObject) jsonValue).toString()));
						}
						message.setConstraints(list.toArray(new ConstraintViolation[]{}));
						break;
					default:
						message.setResult("" + root.get(Constants.Message.RESPONSE));
						break;
				}
			}
			return message;
		}
	}

	protected static String mfcToJson(MessageFromClient mfc) {
		StringBuilder jsonParamNames = new StringBuilder("[");
		boolean first = true;
		for (String parameterName : mfc.getParameterNames()) {
			if (!first) {
				jsonParamNames.append(",");
			}
			jsonParamNames.append(Constants.QUOTE).append(parameterName).append(Constants.QUOTE);
			first = false;
		}
		jsonParamNames.append("]");
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s,\"%s\":%s}",
				  Constants.Message.ID, mfc.getId(),
				  Constants.Message.DATASERVICE, mfc.getDataService(),
				  Constants.Message.OPERATION, mfc.getOperation(),
				  Constants.Message.ARGUMENTNAMES, jsonParamNames.toString(),
				  Constants.Message.ARGUMENTS, Arrays.toString(mfc.getParameters().toArray(new String[mfc.getParameters().size()])));
		// Arrays.toString(mfc.getParameterNames().toArray(new String[mfc.getParameterNames().size()]))
		return json;
	}

	/**
	 * Transforme un json en object java
	 *
	 * @param <T>
	 * @param cls
	 * @param json
	 * @return
	 */
	protected static <T> T getJava(Class<T> cls, String json) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(json, cls);
		} catch (IOException ex) {
		}
		return null;
	}

	/**
	 * call 2 times the method on cls, first time with jsonok, second time with jsonfail first call have to be good, second throw a CONSTRAINT event
	 *
	 * @param cls
	 * @param methodname
	 * @param jsonok
	 * @param jsonfail
	 */
	protected void testUniqueConstraint(Class cls, String methodname, String jsonok, String jsonfail) {
		// test OK
		testRSCallWithoutResult(cls, methodname, jsonok);
		// test with constraint
		MessageFromClient mfc = getMessageFromClient(cls, methodname, jsonfail);
		mfc.setParameterNames(Arrays.asList("str0"));
		MessageToClient mtc = testRSCallWithoutResult(getClient(), mfc, MessageType.CONSTRAINT);
		ConstraintViolation[] cvs = (ConstraintViolation[]) mtc.getResponse();
		assertThat(cvs).isNotNull();
		assertThat(cvs).hasSize(1);
		ConstraintViolation cv = cvs[0];
		assertThat(cv.getIndex()).isEqualTo(0);
		assertThat(cv.getName()).isEqualTo("str0");
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
		WebTarget target = client.target("http://localhost:" + PORT).path(CTXPATH).path("ocelot").path("endpoint").queryParam("monitor", true);
		Response res = target.request(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON).post(Entity.form(new Form("mfc", mfcToJson(mfc))));
		String result = res.readEntity(String.class);
		MessageToClient mtc = null;
		try {
			mtc = mtcFromJson(result);
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
		return createAndGetSession(null, "user:user");
	}

	/**
	 * Create session
	 *
	 * @param userpwd
	 * @return
	 */
	protected Session createAndGetSession(String userpwd) {
		return createAndGetSession(null, userpwd);
	}

	/**
	 * Create session
	 *
	 * @param jsessionid
	 * @param userpwd
	 * @return
	 */
	protected Session createAndGetSession(String jsessionid, String userpwd) {
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		try {
			StringBuilder sb = new StringBuilder("ws://localhost:");
			sb.append(PORT).append(Constants.SLASH).append(CTXPATH).append(Constants.SLASH).append("ocelot-endpoint");
			URI uri = new URI(sb.toString());
			return container.connectToServer(new Endpoint() {
				@Override
				public void onOpen(Session session, EndpointConfig config) {
				}
			}, createClientEndpointConfigWithJsession(jsessionid, userpwd), uri);
		} catch (URISyntaxException | DeploymentException | IOException ex) {
			ex.getCause().printStackTrace();
			fail("CONNEXION FAILED " + ex.getMessage());
		}
		return null;
	}

	private ClientEndpointConfig createClientEndpointConfigWithJsession(final String jsession, final String userpwd) {
		ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {
			@Override
			public void beforeRequest(Map<String, List<String>> headers) {
				if (null != jsession) {
					headers.put("Cookie", Arrays.asList("JSESSIONID=" + jsession));
				} 
				headers.put("Authorization", Arrays.asList("Basic " + DatatypeConverter.printBase64Binary(userpwd.getBytes())));
			}
		};
		return ClientEndpointConfig.Builder.create().configurator(configurator).build();
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

	public void testCallMultiMethodsInClient(String testname, int nb, final Client client) {
		testCallMultiMethodsInClient(nb, client, Double.class, RequestCdiDataService.class, "getValue");
	}

	protected void testResultRequestScope(final Class clazz) {
		Client client = null;
		try {
			client = getClient();
			Collection<Double> results = testCallMultiMethodsInClient(2, client, Double.class, clazz, "getValueTempo");
			assertThat(results).areAtLeastOne(new AreAtLeastOneDifferent(results));
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	/**
	 *
	 * @param <T>
	 * @param nb
	 * @param client
	 * @param returnClass
	 * @param ds
	 * @param methodName
	 * @param params
	 * @return
	 */
	protected <T> Collection<T> testCallMultiMethodsInClient(int nb, final Client client, final Class<T> returnClass, final Class ds, final String methodName, final String... params) {
		ExecutorCompletionService<ResultMonitored<T>> executorCompletionService = new ExecutorCompletionService(managedExecutor);
		Collection<T> results = new ArrayList<>();
		long t0 = System.currentTimeMillis();
		for (int i = 0; i < nb; i++) {
			final int num = i;
			Callable<ResultMonitored<T>> task = new Callable() {
				@Override
				public ResultMonitored<T> call() {
					Client cl = client;
					if (cl == null) {
						cl = getClient();
					}
					long t0 = System.currentTimeMillis();
					T result = getJava(returnClass, (String) testRSCallWithoutResult(cl, ds, methodName, params).getResponse());
					ResultMonitored resultMonitored = new ResultMonitored(result, num);
					long t1 = System.currentTimeMillis();
					resultMonitored.setTime(t1 - t0);
					return resultMonitored;
				}
			};
			executorCompletionService.submit(task);
		}
		for (int i = 0; i < nb; i++) {
			try {
				Future<ResultMonitored<T>> fut = executorCompletionService.take();
				ResultMonitored<T> res = fut.get();
//				System.out.println("Time of execution of service " + res.getNum() + ": " + res.getTime() + " ms");
				results.add(res.getResult());
			} catch (InterruptedException | ExecutionException e) {
			}
		}
		long t1 = System.currentTimeMillis();
		System.out.println("Time of execution of all services : " + (t1 - t0) + " ms");
		assertThat(results).hasSize(nb);
		return results;
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
		Client client = null;
		try {
			client = getClient();
			for (int i = 0; i < nb; i++) {
				results[i] = testRSCallWithoutResult(client, clazz, operation, params).getResponse();
			}
			return results;
		} finally {
			if (client != null) {
				client.close();
			}
		}
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
		Object thirdResult = testRSCallWithoutResult(clazz, "getValue").getResponse();
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
		Client client = null;
		try {
			client = getClient();
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
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	/**
	 *
	 * @param client
	 * @return
	 */
	protected String getJsessionFromServer(Client client) {
		Options options = new Options();
		options.setMonitor(true);
		MessageFromClient mfc = getMessageFromClient(OcelotServices.class, "initCore", getJson(options));
		WebTarget target = client.target("http://localhost:" + PORT + "/" + CTXPATH).path("ocelot").path("endpoint");
		Response res = target.request(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON).post(Entity.form(new Form("mfc", mfcToJson(mfc))));
		res.readEntity(String.class);
		res = target.request(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON).post(Entity.form(new Form("mfc", mfcToJson(mfc))));
		NewCookie jsession = res.getCookies().get("JSESSIONID");
		String result = res.readEntity(String.class);
		try {
			MessageToClient mtc = mtcFromJson(result);
			assertThat(mtc.getType()).isEqualTo(MessageType.RESULT);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return jsession.getValue();
	}

	/**
	 * Subcribe to topic in session
	 *
	 * @param topic
	 * @param client
	 * @param messageType
	 */
	protected void subscribeToTopic(String topic, Client client, MessageType messageType) {
		System.out.println("Subscribe to Topic '" + topic + "'");
		testRSCallWithoutResult(client, OcelotServices.class, "subscribe", messageType, getJson(topic));
	}

	/**
	 * Subcribe to topic in session
	 *
	 * @param topic
	 * @param client
	 * @param messageType
	 */
	protected void unsubscribeToTopic(String topic, Client client, MessageType messageType) {
		System.out.println("Unsubscribe to Topic '" + topic + "'");
		testRSCallWithoutResult(client, OcelotServices.class, "unsubscribe", messageType, getJson(topic));
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
			boolean await = lock.await(TIMEOUT * nbMsg, TimeUnit.MILLISECONDS);
			long t1 = System.currentTimeMillis();
			assertThat(await).as("Timeout. waiting %d ms. Remain %d/%d msgs", t1 - t0, lock.getCount(), nbMsg).isTrue();
			wssession.removeMessageHandler(messageHandler);
		} catch (IllegalStateException | InterruptedException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Test reception of 0 msg triggered by call Runnable
	 *
	 * @param wssession
	 * @param topic
	 * @param trigger
	 */
	protected void testWait0MessageToTopic(Session wssession, String topic, Runnable trigger) {
		try {
			long t0 = System.currentTimeMillis();
			CountDownLatch lock = new CountDownLatch(1);
			CountDownMessageHandler messageHandler = new CountDownMessageHandler(topic, lock);
			wssession.addMessageHandler(messageHandler);
			trigger.run();
			boolean await = lock.await(TIMEOUT, TimeUnit.MILLISECONDS);
			long t1 = System.currentTimeMillis();
			assertThat(await).as("Timeout. waiting %d ms. Remain %d/%d msgs", t1 - t0, lock.getCount(), 1).isFalse();
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
			MessageToClient messageToClientIn = mtcFromJson(message);
			if (id == null || id.equals(messageToClientIn.getId())) {
				messageToClient = messageToClientIn;
				lock.countDown();
			}
		}

		public MessageToClient getMessageToClient() {
			return messageToClient;
		}
	}
	
	protected void testReceiveXMessagesToTopic(final int nbMsg, final String topic, final Class cls, final String methodname, String user, String pwd) {
		testReceiveXMessageToTopicWithParams(nbMsg, topic, cls, methodname, user, pwd, getJson(nbMsg));
	}
	protected void testReceive1MessageToTopic(final String topic, final Class cls, final String methodname, String user, String pwd, final String... params) {
		testReceiveXMessageToTopicWithParams(1, topic, cls, methodname, user, pwd);
	}
	protected void testReceive1MessagesToDynTopic(final String topic, final Class cls, final String methodname, String user, String pwd) {
		testReceiveXMessageToTopicWithParams(1, topic, cls, methodname, user, pwd, getJson(topic));
	}
	protected void testReceiveXMessagesToDynTopic(final int nbMsg, final String topic, final Class cls, final String methodname, String user, String pwd) {
		testReceiveXMessageToTopicWithParams(nbMsg, topic, cls, methodname, user, pwd, getJson(nbMsg), getJson(topic));
	}

	/**
	 * Test receive X messages to mytopic
	 */
	protected void testReceiveXMessageToTopicWithParams(final int nbMsg, final String topic, final Class cls, final String methodname, String user, String pwd, final String... params) {
		testReceiveXMessagesToTopicWithRunable(nbMsg, topic, methodname, user, pwd, new Runnable() {
			@Override
			public void run() {
				testRSCallWithoutResult(cls, methodname, params);
			}
		});
	}

	/**
	 * Test receive X messages to mytopic
	 */
	protected void testReceiveXMessageToTopicWithMfc(final int nbMsg, final String topic, final MessageFromClient mfc, final String methodname, final String user, final String pwd) {
		testReceiveXMessagesToTopicWithRunable(nbMsg, topic, methodname, user, pwd, new Runnable() {
			@Override
			public void run() {
				testRSCallWithoutResult(getClient(user, pwd), mfc, MessageType.RESULT);
			}
		});
	}

	/**
	 * Test receive X messages to mytopic
	 */
	protected void testReceiveXMessagesToTopicWithRunable(final int nbMsg, final String topic, final String methodname, String user, String pwd, Runnable runnable) {
		Client client = getClient(user, pwd);
		String jsession = getJsessionFromServer(client);
		try (Session wssession = createAndGetSession(jsession, user + ":" + pwd)) {
			subscribeToTopic(topic, client, MessageType.RESULT);
			testWaitXMessageToTopic(wssession, nbMsg, topic, runnable);
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		unsubscribeToTopic(topic, client, MessageType.RESULT);
	}
}
