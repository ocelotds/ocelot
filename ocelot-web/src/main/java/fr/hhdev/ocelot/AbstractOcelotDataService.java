/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.SimpleType;
import fr.hhdev.ocelot.annotations.DataService;
import fr.hhdev.ocelot.annotations.JsCacheRemove;
import fr.hhdev.ocelot.annotations.JsCacheRemoveAll;
import fr.hhdev.ocelot.annotations.JsCacheRemoves;
import fr.hhdev.ocelot.annotations.JsCacheResult;
import fr.hhdev.ocelot.messaging.Fault;
import fr.hhdev.ocelot.messaging.MessageEvent;
import fr.hhdev.ocelot.messaging.MessageFromClient;
import fr.hhdev.ocelot.messaging.MessageToClient;
import fr.hhdev.ocelot.resolvers.DataServiceResolverIdLitteral;
import fr.hhdev.ocelot.spi.DataServiceException;
import fr.hhdev.ocelot.spi.IDataServiceResolver;
import fr.hhdev.ocelot.spi.Scope;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.el.MethodNotFoundException;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.websocket.EncodeException;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class of OcelotDataService
 *
 * @author hhfrancois
 */
public abstract class AbstractOcelotDataService {

	private static final Logger logger = LoggerFactory.getLogger(AbstractOcelotDataService.class);

	@Inject
	@MessageEvent
	Event<MessageToClient> wsEvent;

	@Inject
	@Any
	private Instance<IDataServiceResolver> resolvers;

	@Inject
	private OcelotConfiguration configuration;


	protected IDataServiceResolver getResolver(String type) {
		return resolvers.select(new DataServiceResolverIdLitteral(type)).get();
	}

	/**
	 * Retourne la methode adéquate et peuple la liste des arguments en les déserialisant du message
	 *
	 * @param dataService
	 * @param message
	 * @param arguments
	 * @return
	 */
	protected Method getMethodFromDataService(final Object dataService, final MessageFromClient message, Object[] arguments) throws MethodNotFoundException {
		logger.trace("Try to find method {} on class {}", message.getOperation(), dataService);
		List<String> parameters = message.getParameters();
		for (Method method : dataService.getClass().getMethods()) {
			if (method.getName().equals(message.getOperation()) && method.getParameterCount() == parameters.size()) {
				logger.trace("Process method {}", method.getName());
				try {
					Type[] params = method.getGenericParameterTypes();
					int idx = 0;
					for (Type param : params) {
						String arg = cleanArg(parameters.get(idx));
						logger.trace("Get argument ({}) {} : {}.", new Object[]{idx, param.getTypeName(), arg});
						arguments[idx++] = convertArgument(arg, param);
					}
					logger.trace("Method {}.{} with good signature found.", dataService.getClass(), message.getOperation());
					return method;
				} catch (IllegalArgumentException iae) {
					logger.trace("Method {}.{} not found. Arguments didn't match. {}.", new Object[]{dataService.getClass(), message.getOperation(), iae.getMessage()});
				}
			}
		}
		throw new MethodNotFoundException(dataService.getClass() + "." + message.getOperation());
	}

	private Object convertArgument(String arg, Type param) throws IllegalArgumentException {
		Object result = null;
		logger.trace("Try to convert {} : param = {} : {}", new Object[]{arg, param, param.getClass()});
		try {
			ObjectMapper mapper = new ObjectMapper();
			if(ParameterizedType.class.isInstance(param)) {
				JavaType javaType = getJavaType(param);
				logger.trace("Try to convert '{}' to JavaType : '{}'", arg, param);
				result = mapper.readValue(arg, javaType);
				logger.trace("Conversion of '{}' to '{}' : OK", arg, param);
			} else if(Class.class.isInstance(param)) {
				Class cls = (Class) param;
				logger.trace("Try to convert '{}' to Class '{}'", arg, param);
				if (cls.equals(String.class) && (!arg.startsWith("\"") || !arg.endsWith("\""))) { // on cherche une string
					throw new IOException();
				}
				if (!cls.equals(String.class) && arg.startsWith("\"") && arg.endsWith("\"")) { // on a une string
					throw new IOException();
				}
				result = mapper.readValue(arg, cls);
				logger.trace("Conversion of '{}' to '{}' : OK", arg, param);
			}
		} catch (IOException ex) {
			logger.trace("Conversion of '{}' to '{}' failed", arg, param);
			throw new IllegalArgumentException(param.getTypeName());
		}
		return result;
	}

	private JavaType getJavaType(Type type) {
		Class clazz;
		logger.trace("Computing type of {} - {}", type.getClass(), type.getTypeName());
		if (type instanceof ParameterizedType) {
			clazz = (Class) ((ParameterizedType) type).getRawType();
		} else {
			clazz = (Class) type;
		}
		JavaType javaType;
		Type actualType;
		if (Collection.class.isAssignableFrom(clazz)) {
			ParameterizedType pt = (ParameterizedType) type;
			actualType = pt.getActualTypeArguments()[0];
			JavaType t1 = getJavaType(actualType);
			javaType = CollectionType.construct(Collection.class, t1);
		} else if (clazz.isArray()) {
			Class t = clazz.getComponentType();
			JavaType t1 = getJavaType(t);
			javaType = ArrayType.construct(t1, null, null);
		} else if (Map.class.isAssignableFrom(clazz)) {
			ParameterizedType pt = (ParameterizedType) type;
			actualType = pt.getActualTypeArguments()[0];
			JavaType t1 = getJavaType(actualType);
			actualType = pt.getActualTypeArguments()[1];
			JavaType t2 = getJavaType(actualType);
			javaType = MapType.construct(Map.class, t1, t2);
		} else {
			javaType = SimpleType.construct(clazz);
		}
		return javaType;
	}

	/**
	 * Methode permettant de nettoyer les arguments des attributs ajoutés par les framework web, 
	 * par exemple angularjs rajoute des variables commencant par $$
	 * à remplacer : ,"$$hashKey":"object:\d"
	 * TODO externaliser ca via SPI pour supporter n'importe quel framework
	 *
	 * @param arg
	 * @return
	 */
	private String cleanArg(String arg) {
		String angularvar = "(,\"\\$\\$\\w+\":\".*\")";
		return arg.replaceAll(angularvar, "");
	}

	/**
	 * Get Dataservice, maybe in session if session scope and stored J'aimerais bien faire cela avec un interceptor/decorator, mais comment passer la session à celui ci ?
	 *
	 * @param client
	 * @param cls
	 * @return
	 * @throws DataServiceException
	 */
	protected Object getDataService(Session client, Class cls) throws DataServiceException {
		String dataServiceClassName = cls.getName();
		logger.trace("Looking for dataservice : {}", dataServiceClassName);
		if (cls.isAnnotationPresent(DataService.class)) {
			DataService dataServiceAnno = (DataService) cls.getAnnotation(DataService.class);
			IDataServiceResolver resolver = getResolver(dataServiceAnno.resolver());
			Scope scope = resolver.getScope(cls);
			Object dataService = null;
			if (scope.equals(Scope.SESSION)) {
				dataService = client.getUserProperties().get(dataServiceClassName);
			}
			if (dataService == null) {
				dataService = resolver.resolveDataService(cls);
				if (scope.equals(Scope.SESSION)) {
					client.getUserProperties().put(dataServiceClassName, dataService);
				}
			}
			return dataService;
		} else {
			throw new DataServiceException(dataServiceClassName);
		}
	}

	/**
	 * Construction et envoi des messages response suite à un appel de type call
	 *
	 * @param client
	 * @param message
	 */
	protected void sendMessageToClients(Session client, MessageFromClient message) {
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(message.getId());
		try {
			Class cls = Class.forName(message.getDataService());
			Object dataService = getDataService(client, cls);
			logger.trace("Process message {}", message);
			logger.trace("Invocation of : {}", message.getOperation());
			Object[] arguments = new Object[message.getParameters().size()];
			Method method = getMethodFromDataService(dataService, message, arguments);
			logger.trace("Process method {}.", method.getName());
			Object result = method.invoke(dataService, arguments);
			messageToClient.setResult(result);
			try {
				Method nonProxiedMethod = getNonProxiedMethod(cls, method.getName(), method.getParameterTypes());
				messageToClient.setDeadline(getJsCacheResultDeadline(nonProxiedMethod));
				processCleanCacheAnnotations(nonProxiedMethod, message.getParameterNames(), message.getParameters());
			} catch (NoSuchMethodException ex) {
				logger.error("Fail to process extra annotations (JsCacheResult, JsCacheRemove) for method : " + method.getName(), ex);
			}
		} catch (MethodNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException | DataServiceException ex) {
			int stacktracelength =configuration.getStacktracelength();
			Throwable cause = ex;
			if (InvocationTargetException.class.isInstance(ex)) {
				cause = ex.getCause();
			}
			if(stacktracelength==0) {
				logger.error("Invocation failed", ex);
			}
			messageToClient.setFault(new Fault(cause, stacktracelength));
		}
		try {
			client.getBasicRemote().sendObject(messageToClient);
		} catch (IOException | EncodeException ex) {
			logger.error("Fail to send : " + messageToClient.toJson(), ex);
		}
	}

	/**
	 * Récupere la date d'expiration du service via l'annotation sur la methode directement sur la classe, car sinon cela peut être un proxy
	 * Eventuellement traite les annotation spécifiant que l'execution de la méthode doit supprimer un cache
	 *
	 * @param nonProxiedMethod
	 * @return
	 */
	protected long getJsCacheResultDeadline(Method nonProxiedMethod) {
		boolean cached = nonProxiedMethod.isAnnotationPresent(JsCacheResult.class);
		if (cached) { // Ce service doit être mis en cache sur le client
			logger.debug("The result of the method {} will be cached on client side.", nonProxiedMethod.getName());
			JsCacheResult jcr = nonProxiedMethod.getAnnotation(JsCacheResult.class);
			Calendar deadline = Calendar.getInstance();
			deadline.add(Calendar.YEAR, jcr.year());
			deadline.add(Calendar.MONTH, jcr.month());
			deadline.add(Calendar.DATE, jcr.day());
			deadline.add(Calendar.HOUR, jcr.hour());
			deadline.add(Calendar.MINUTE, jcr.minute());
			deadline.add(Calendar.SECOND, jcr.second());
			deadline.add(Calendar.MILLISECOND, jcr.millisecond());
			return deadline.getTime().getTime();
		}
		return 0;
	}
	
	/**
	 * Traite les annotations JsCacheRemove et JsCacheRemoves
	 * 
	 * @param nonProxiedMethod
	 * @param paramNames
	 * @param jsonArgs 
	 */
	protected void processCleanCacheAnnotations(Method nonProxiedMethod, List<String> paramNames, List<String> jsonArgs) {
		boolean cleanAllCache = nonProxiedMethod.isAnnotationPresent(JsCacheRemoveAll.class);
		if(cleanAllCache) {
			JsCacheRemoveAll jcra = nonProxiedMethod.getAnnotation(JsCacheRemoveAll.class);
			processJsCacheRemoveAll(jcra);
		}
		boolean simpleCleancache = nonProxiedMethod.isAnnotationPresent(JsCacheRemove.class);
		if(simpleCleancache) {
			JsCacheRemove jcr = nonProxiedMethod.getAnnotation(JsCacheRemove.class);
			processJsCacheRemove(jcr, paramNames, jsonArgs);
		}
		boolean multiCleancache = nonProxiedMethod.isAnnotationPresent(JsCacheRemoves.class);
		if(multiCleancache) {
			JsCacheRemoves jcrs = nonProxiedMethod.getAnnotation(JsCacheRemoves.class);
			for (JsCacheRemove jcr : jcrs.value()) {
				processJsCacheRemove(jcr, paramNames, jsonArgs);
			}
		}
		if(simpleCleancache || multiCleancache) {
			logger.debug("The method {} will remove cache{} entr{} on clients side.", nonProxiedMethod.getName(), multiCleancache?"s":"", multiCleancache?"ies":"y");
		}
	}

	/**
	 * Traite l'annotation JsCacheRemoveAll et envoi un message de suppression de tous le cache
	 * @param jcra
	 */
	protected void processJsCacheRemoveAll(JsCacheRemoveAll jcra) {
		logger.trace("Process JsCacheRemoveAll annotation : {}", jcra);
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(Constants.Cache.CLEANCACHE_TOPIC);
		messageToClient.setResult(Constants.Cache.ALL);
		wsEvent.fire(messageToClient);
	}
	
	/**
	 * Process an annotation JsCacheRemove and send a removeCache message to all clients connected
	 * @param jcr : l'annotation
	 * @param paramNames
	 * @param jsonArgs : les arguments de la methode au format json
	 */
	protected void processJsCacheRemove(JsCacheRemove jcr, List<String> paramNames, List<String> jsonArgs) {
		logger.debug("Process JsCacheRemove annotation : {}", jcr);
		StringBuilder sb = new StringBuilder(jcr.cls().getName()).append(".").append(jcr.methodName());
		sb.append("([");
		MessageToClient messageToClient = new MessageToClient();
		String[] args = new String[jsonArgs.size()];
		logger.debug("CLASSNAME : {} - METHODNAME : {} - KEYS : {}", jcr.cls().getName(), jcr.methodName(), jcr.keys());
		logger.debug("JSONARGS : {}", Arrays.deepToString(jsonArgs.toArray(new String[]{})));
		logger.debug("PARAMNAMES : {}", Arrays.deepToString(paramNames.toArray(new String[]{})));
		String[] keys = jcr.keys();
		for (int idKey = 0; idKey < keys.length; idKey++) {
			String key = keys[idKey];
			if("*".equals(key)) {
				sb.append(String.join(",", jsonArgs));
				break;
			} else {
				logger.debug("Process {} : ", key);
				String[] path = key.split("\\.");
				logger.debug("Process '{}' : token nb '{}'", key, path.length);
				String paramName = path[0];
				logger.debug("Looking for index of param '{}'", paramName);
				int idx = paramNames.indexOf("\""+paramName+"\"");
				logger.debug("Index of param '{}' : '{}'", paramName, idx);
				String jsonArg = jsonArgs.get(idx);
				logger.debug("Param '{}' : '{}'", paramName, jsonArg);
				if(path.length>1) {
					try (JsonReader reader = Json.createReader(new StringReader(jsonArg))) {
						JsonValue jsonObject = reader.readObject();
						for (int i = 1; i < path.length; i++) {
							String p = path[i];
							if(!(jsonObject instanceof JsonObject)) {
								logger.error("Impossible to get "+p+" on "+jsonObject.toString()+". It's not an json objet.");
							}
							logger.debug("Access to '{}' for '{}'", p, jsonObject.toString());
							jsonObject = ((JsonObject)jsonObject).get(p);
						}
						jsonArg = jsonObject.toString();
					}
				}
				logger.debug("Add value for '{}' : '{}' to builder cache key", key, jsonArg);
				sb.append(jsonArg);
				if(idKey+1 < keys.length) {
					sb.append(",");
				}
			}
		}
		sb.append("])");
		logger.debug("KEYS FROM ARGS : {}", sb.toString());
		messageToClient.setId(Constants.Cache.CLEANCACHE_TOPIC);
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] bytes = sb.toString().getBytes();
			md.update(bytes, 0, bytes.length);
			String md5 = new BigInteger(1, md.digest()).toString(16);
			messageToClient.setResult(md5);
			wsEvent.fire(messageToClient);
		} catch (NoSuchAlgorithmException ex) {
		}
	}

	/**
	 * Récupere la methode sur la classe d'origine en ignorant les eventuels proxies
	 * @param cls
	 * @param methodName
	 * @param parameterTypes
	 * @throws NoSuchMethodException
	 * @return 
	 */
	private Method getNonProxiedMethod(Class cls, String methodName,  Class<?>[] parameterTypes) throws NoSuchMethodException {
		try {
			return cls.getMethod(methodName, parameterTypes);
		} catch (SecurityException ex) {
		}
		throw new NoSuchMethodException(methodName);
	}
}
