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

	String QUOTE = "\"";
	String ALGORITHM = "MD5";
	String JS = ".js";
	String SLASH = "/";
	String BACKSLASH_N = "\n";
	String LOCALE = "LOCALE";
	String SESSION = "SESSION"; // to remove
	String HTTPSESSION = "HTTPSESSION";
	String HTTPREQUEST = "HTTPREQUEST";
	String HANDSHAKEREQUEST = "HANDSHAKEREQUEST";
	String SESSION_BEANS = "SESSIONBEANS";
	String PRINCIPAL = "PRINCIPAL";
	String ANONYMOUS = "ANONYMOUS";

	String OCELOT = "ocelot";
	String OCELOT_CORE = OCELOT + "-core";
	String OCELOT_CORE_MIN = OCELOT_CORE + "-min";

	String JSTYPE = "text/javascript;charset=UTF-8";
	String FALSE = "false";
	String TRUE = "true";

	int DEFAULT_BUFFER_SIZE = 1024 * 4;

	interface Options {
		String STACKTRACE_LENGTH = "ocelot.stacktrace.length";
		String DASHBOARD_ROLES = "ocelot.dashboard.roles";
		String OPTIONS = "options";
		String SEPARATOR = ",";
	}

	interface Topic {

		String SUBSCRIBERS = "subscribers";
		String COLON = ":";
		String ALL = "ALL";
	}

	interface Message {

		String ID = "id";
		String TYPE = "type";
		String DATASERVICE = "ds";
		String TIME = "t";
		String OPERATION = "op";
		String ARGUMENTS = "args";
		String ARGUMENTNAMES = "argNames";
		String DEADLINE = "deadline";
		String RESPONSE = "response";
		String LANGUAGE = "language";
		String COUNTRY = "country";
		String MFC = "mfc";

		interface Fault {

			String MESSAGE = "message";
			String CLASSNAME = "classname";
			String STACKTRACE = "stacktrace";

		}
	}

	interface Resolver {
		String CDI = "cdi";
		String EJB = "cdi"; // same resolver
		String SPRING = "spring";
	}

	interface Cache {

		String CLEANCACHE_TOPIC = "ocelot-cleancache";
		String ALL = "ALL";
		String USE_ALL_ARGUMENTS = "*";
	}
	
	interface BeanManager {
		String BEANMANAGER_JEE = "java:comp/BeanManager";
		String BEANMANAGER_ALT = "java:comp/env/BeanManager";
	}
}
