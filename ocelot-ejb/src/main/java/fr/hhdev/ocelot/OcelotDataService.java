package fr.hhdev.ocelot;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.SimpleType;
import fr.hhdev.ocelot.messaging.Fault;
import fr.hhdev.ocelot.messaging.MessageFromClient;
import fr.hhdev.ocelot.messaging.MessageToClient;
import fr.hhdev.ocelot.messaging.MessageEvent;
import fr.hhdev.ocelot.resolvers.DataServiceException;
import fr.hhdev.ocelot.resolvers.DataServiceResolver;
import fr.hhdev.ocelot.resolvers.DataServiceResolverIdLitteral;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.el.MethodNotFoundException;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author hhfrancois
 */
@Slf4j
@Stateless
public class OcelotDataService {

	@Inject
	@MessageEvent
	Event<MessageToClient> wsEvent;

	@Inject
	@Any
	private Instance<DataServiceResolver> resolvers;

	private DataServiceResolver getResolver(String type) {
		return resolvers.select(new DataServiceResolverIdLitteral(type)).get();
	}

	@Asynchronous
	@SuppressWarnings("UseSpecificCatch")
	public Future<Void> excecute(String resolverId, MessageFromClient message) {
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(message.getId());
		try {
			Object ds = getResolver(resolverId).resolveDataService(message.getDataService());
			if(ds.getClass().isAnnotationPresent(DataService.class)) {
				Object result = invoke(ds, message);
				messageToClient.setResult(result);
			} else {
				throw new DataServiceUnknownException(message.getDataService());
			}
		} catch (Exception ex) {
			Throwable cause = ex;
			if (InvocationTargetException.class.isInstance(ex)) {
				cause = ex.getCause();
			}
			Fault f = new Fault();
			f.setMessage(cause.getMessage());
			f.setClassname(cause.getClass().getName());
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			cause.printStackTrace(pw);
			f.setStacktrace(pw.toString());
			messageToClient.setFault(f);
		}
		wsEvent.fire(messageToClient);
		return new AsyncResult<>(null);
	}

	private Object invoke(Object dataService, MessageFromClient message) throws MethodNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, DataServiceException, IOException {
		Object[] arguments = new Object[message.getParameters().size()];
		logger.debug("Réceptacle de {} argument(s) typé(s).", arguments.length);
		Method method = getMethodFromDataService(dataService, message, arguments);
		logger.debug("Récupération de la méthode {} et récupération des arguments.", method.getName());
		Object result = method.invoke(dataService, arguments);
		return result;
	}

	/**
	 * Retourne la methode adéquate et peuple la liste des arguments en les déserialisant du message
	 *
	 * @param dataService
	 * @param message
	 * @param arguments
	 * @return
	 */
	Method getMethodFromDataService(final Object dataService, final MessageFromClient message, Object[] arguments) throws MethodNotFoundException {
		List<String> parameters = message.getParameters();
		for (Method method : dataService.getClass().getMethods()) {
			if (method.getName().equals(message.getOperation()) && method.getParameterCount() == parameters.size()) {
				logger.debug("Traitement de la methode " + method.getName());
				try {
					Parameter[] params = method.getParameters();
					logger.debug("On a trouvé une methode avec le bon nombre d'arguments, on essaye de les unmarshaller.");
					int idx = 0;
					for (Parameter param : params) {
						String arg = parameters.get(idx);
						logger.debug("Récupération de l'argument ({}) {} : {}.", new Object[]{idx, param.getName(), arg});
						arguments[idx++] = convertArgument(arg, param);
					}
					logger.debug("Méthode {}.{} avec tous les arguments du même type trouvé.", dataService.getClass(), message.getOperation());
					return method;
				} catch (IllegalArgumentException iae) {
					logger.debug("Méthode {}.{} non retenue car {} ne colle pas, mauvais type.", new Object[]{dataService.getClass(), message.getOperation(), iae.getMessage()});
				}
			}
		}
		throw new MethodNotFoundException(dataService.getClass() + "." + message.getOperation());
	}

	private Object convertArgument(String arg, Parameter param) throws IllegalArgumentException {
		Object result = null;
		logger.debug("Tentative de conversion de {}", arg);
		try {
			ObjectMapper mapper = new ObjectMapper();
			if (Collection.class.isAssignableFrom(param.getType()) || Map.class.isAssignableFrom(param.getType())) {
				ParameterizedType pt = (ParameterizedType) param.getParameterizedType();
				JavaType javaType = getJavaType(pt);
				logger.debug("Tentative de conversion de {} vers JavaType : {}", arg, javaType);
				result = mapper.readValue(arg, javaType);
				logger.debug("Conversion de {} vers {} : OK", arg, param.getType());
			} else {
				logger.debug("Tentative de conversion de {} vers {}", arg, param.getType());
				result = mapper.readValue(arg, param.getType());
				logger.debug("Conversion de conversion de {} vers {} : OK", arg, param.getType());
			}
		} catch (IOException ex) {
			logger.debug("Echec de tentative de conversion de {} vers {}", arg, param.getType());
			throw new IllegalArgumentException(param.getName());
		}
		return result;
	}

	private JavaType getJavaType(Type type) {
		Class clazz;
		logger.debug("Détermination du type {} - {}", type.getClass(), type.getTypeName());
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
}
