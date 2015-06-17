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
	
	String OCELOT_SERVICES_JS = "ocelot-services.js";
	String OCELOT_CORE_JS = "ocelot-core.js";
	String OCELOT_SERVICES = "ocelot-services";
	String JS = ".js";

	interface Options {
		String MINIFYJS = "ocelot.minifyjs";
		String STACKTRACE = "ocelot.stacktrace.length";
	}
	interface Message {

		String ID = "id";
		String DATASERVICE = "ds";
		String OPERATION = "op";
		String ARGUMENTS = "args";
		String ARGUMENTNAMES = "argNames";
		String DEADLINE = "deadline";
		String RESULT = "result";
		String FAULT = "fault";
		interface Fault {
			String MESSAGE = "message";
			String CLASSNAME = "classname";
			String STACKTRACE = "stacktrace";

		}
	}

	interface Resolver {

		String POJO = "pojo";
		String CDI = "cdi";
		String EJB = "ejb";
		String SPRING = "spring";
	}

	interface Cache {
		String CLEANCACHE_TOPIC = "ocelot-cleancache";
		String ALL = "all";
	}
	
	interface Command {

		// Attention si on modifie ces deux varaibles, elles sont aussi dans ocelot-core.js en dur
		String COMMAND = "cmd";
		String MESSAGE = "msg";

		interface Value {

			String SUBSCRIBE = "subscribe";
			String UNSUBSCRIBE = "unsubscribe";
			String CALL = "call";
		}
	}
}
