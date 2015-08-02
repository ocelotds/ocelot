/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds;

/**
 * Constants Class
 *
 * @author hhfrancois
 */
public interface Constants {

	String UTF_8 = "UTF-8";
	String JS = ".js";
	String MIN = "-min";
	String SLASH = "/";
	String BACKSLASH_N = "\n";
	String LOCALE = "LOCALE";

	String OCELOT = "ocelot";
	String OCELOT_CORE = OCELOT+"-core";
	String OCELOT_MIN = OCELOT+MIN;
	String SLASH_OCELOT_JS = SLASH+OCELOT+JS;
	
	String MINIFY_PARAMETER = "minify";
	String JSTYPE = "text/javascript;charset=UTF-8";
	String FALSE = "false";
	String TRUE = "true";
	
	/**
	 * This string will be replaced by the contextPath in ocelot-core.js
	 */
	String CTXPATH = "%CTXPATH%";
	
	interface Options {

		String STACKTRACE = "ocelot.stacktrace.length";
	}

	interface Message {

		String ID = "id";
		String TYPE = "type";
		String DATASERVICE = "ds";
		String OPERATION = "op";
		String ARGUMENTS = "args";
		String ARGUMENTNAMES = "argNames";
		String DEADLINE = "deadline";
		String RESPONSE = "response";
		String SUBSCRIPTION = "subscription";
		String UNSUBSCRIPTION = "unsubscription";

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
		String ALL = "ALL";
		String USE_ALL_ARGUMENTS = "*";
		String ARGS_NOT_CONSIDERATED = "-";
	}
}
