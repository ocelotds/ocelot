package fr.hhdev.ocelot;

import fr.hhdev.ocelot.annotations.DataService;
import fr.hhdev.ocelot.messaging.Fault;
import fr.hhdev.ocelot.messaging.MessageFromClient;
import fr.hhdev.ocelot.messaging.MessageToClient;
import fr.hhdev.ocelot.spi.DataServiceException;
import fr.hhdev.ocelot.spi.DataServiceResolver;
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

	private final Object service;
	private final Event<MessageToClient> wsEvent;
	private final MessageFromClient message;

	public OcelotDataService(Object dataservice, Event<MessageToClient> wsEvent, MessageFromClient message) {
		this.service = dataservice;
		this.wsEvent = wsEvent;
		this.message = message;
	}
	
	@Override
	public void run() {
		logger.debug("Excecution de la methode pour le message {}", message);
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(message.getId());
		try {
			logger.debug("Dataservice : {}", this.service);
			Class cls = Class.forName(this.message.getDataService());
			if(cls.isAnnotationPresent(DataService.class)) {
				logger.debug("Invocation de  : {}", this.message.getOperation());
				Object result = invoke(this.service, this.message);
				messageToClient.setResult(result);
			} else {
				throw new DataServiceException(this.message.getDataService());
			}
		} catch (Exception ex) {
			Throwable cause = ex;
			if (InvocationTargetException.class.isInstance(ex)) {
				cause = ex.getCause();
			}
			messageToClient.setFault(new Fault(cause));
		}
		this.wsEvent.fire(messageToClient);
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
