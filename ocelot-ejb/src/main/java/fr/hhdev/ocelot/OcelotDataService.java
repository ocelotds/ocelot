package fr.hhdev.ocelot;

import fr.hhdev.ocelot.annotations.DataService;
import fr.hhdev.ocelot.messaging.Fault;
import fr.hhdev.ocelot.messaging.MessageFromClient;
import fr.hhdev.ocelot.messaging.MessageToClient;
import fr.hhdev.ocelot.messaging.MessageEvent;
import fr.hhdev.ocelot.resolvers.DataServiceException;
import fr.hhdev.ocelot.resolvers.DataServiceResolver;
import fr.hhdev.ocelot.resolvers.DataServiceResolverIdLitteral;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Future;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.el.MethodNotFoundException;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
@Stateless
public class OcelotDataService extends AbstractOcelotDataService {

	private static final Logger logger = LoggerFactory.getLogger(OcelotDataService.class);

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
			Class cls = Class.forName(message.getDataService());
			if(cls.isAnnotationPresent(DataService.class)) {
				Object result = invoke(ds, message);
				messageToClient.setResult(result);
			} else {
				throw new DataServiceException(message.getDataService());
			}
		} catch (Exception ex) {
			Throwable cause = ex;
			if (InvocationTargetException.class.isInstance(ex)) {
				cause = ex.getCause();
			}
			messageToClient.setFault(new Fault(cause));
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
}
