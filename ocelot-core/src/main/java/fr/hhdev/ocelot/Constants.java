/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
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
		String CDI = "cdi";
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
