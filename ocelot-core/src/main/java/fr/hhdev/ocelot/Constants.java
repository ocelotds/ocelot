package fr.hhdev.ocelot;

/**
 * Constants Class
 *
 * @author hhfrancois
 */
public interface Constants {

	interface Message {

		String ID = "id";
		String DATASERVICE = "ds";
		String OPERATION = "op";
		String ARGUMENTS = "args";
		String RESULT = "result";
		String FAULT = "fault";
	}

	interface Resolver {

		String SPRING = "spring";
		String POJO = "pojo";
		String EJB = "ejb";
	}

	interface Command {

		String TOPIC = "topic";
		String COMMAND = "cmd";
		String MESSAGE = "msg";

		interface Value {

			String SUBSCRIBE = "subscribe";
			String UNSUBSCRIBE = "unsubscribe";
			String CALL = "call";
		}
	}
}
