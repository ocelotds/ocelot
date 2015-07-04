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

	String JS = ".js";
	String MIN = "-min";
	String SLASH = "/";
	String BACKSLASH_N = "\n";
	String LOCALE = "LOCALE";

	String OCELOT_CORE = "ocelot-core";
	String OCELOT_SERVICES = "ocelot-services";

	String OCELOT_CORE_MIN = OCELOT_CORE+MIN;
	String OCELOT_SERVICES_MIN = OCELOT_SERVICES+MIN;

	String SLASH_OCELOT_CORE_JS = SLASH+OCELOT_CORE+JS;
	String SLASH_OCELOT_SERVICES_JS = SLASH+OCELOT_SERVICES+JS;
	
	String MINIFY_PARAMETER = "minify";
	String JSTYPE = "text/javascript;charset=UTF-8";
	String FALSE = "false";
	String TRUE = "true";
	
	interface Options {

		String STACKTRACE = "ocelot.stacktrace.length";
	}

	interface Message {

		String ID = "id";
		String DATASERVICE = "ds";
		String OPERATION = "op";
		String ARGUMENTS = "args";
		String ARGUMENTNAMES = "argNames";
		String DEADLINE = "deadline";
		String STORE = "store";
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
