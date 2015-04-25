package fr.hhdev.ocelot;

import fr.hhdev.ocelot.annotations.DataService;
import fr.hhdev.ocelot.messaging.Fault;
import fr.hhdev.ocelot.messaging.MessageFromClient;
import fr.hhdev.ocelot.messaging.MessageToClient;
import fr.hhdev.ocelot.resolvers.DataServiceException;
import fr.hhdev.ocelot.resolvers.DataServiceResolver;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.el.MethodNotFoundException;
import javax.enterprise.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
public class OcelotDataService extends AbstractOcelotDataService implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(OcelotDataService.class);

	private final DataServiceResolver resolver;
	private final Event<MessageToClient> wsEvent;
	private final MessageFromClient message;

	public OcelotDataService(DataServiceResolver resolver, Event<MessageToClient> wsEvent, MessageFromClient message) {
		this.resolver = resolver;
		this.wsEvent = wsEvent;
		this.message = message;
	}
	
	@Override
	public void run() {
		logger.debug("Excecution de la methode pour le message {}", message);
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(message.getId());
		try {
			logger.debug("Resolver : {}", resolver);
			Object ds = resolver.resolveDataService(message.getDataService());
			logger.debug("Dataservice : {}", ds);
			Class cls = Class.forName(message.getDataService());
			if(cls.isAnnotationPresent(DataService.class)) {
				logger.debug("Invocation de  : {}", message.getOperation());
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
