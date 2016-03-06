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
	String UTF_8 = "UTF-8";
	String JS = ".js";
	String HTML = ".htm";
	String SLASH = "/";
	String BACKSLASH_N = "\n";
	String LOCALE = "LOCALE";
	String SESSION = "SESSION";
	String HTTPSESSION = "HTTPSESSION";
	String HTTPREQUEST = "HTTPREQUEST";
	String HANDSHAKEREQUEST = "HANDSHAKEREQUEST";
	String SESSION_BEANS = "SESSIONBEANS";
	String PRINCIPAL = "PRINCIPAL";
	String ANONYMOUS = "ANONYMOUS";

	String CONTENT = "content";
	String OCELOT = "ocelot";
	String OCELOT_CORE = OCELOT + "-core";
	String OCELOT_HTML = OCELOT + "-html";
	String OCELOT_MIN = OCELOT + "-min";
	String SLASH_OCELOT = SLASH + OCELOT;
	String SLASH_OCELOT_JS = SLASH_OCELOT + JS;
	String SLASH_OCELOT_HTML = SLASH_OCELOT + HTML;

	String MINIFY_PARAMETER = "minify";
	String JSTYPE = "text/javascript;charset=UTF-8";
	String HTMLTYPE = "text/html;charset=UTF-8";
	String FALSE = "false";
	String TRUE = "true";

	String WSS = "wss";
	String WS = "ws";

	/**
	 * This string will be replaced by the contextPath in ocelot-core.js
	 */
	String CTXPATH = "%CTXPATH%";
	String PROTOCOL = "%WSS%";

	int DEFAULT_BUFFER_SIZE = 1024 * 4;

	interface Options {
		String STACKTRACE_LENGTH = "ocelot.stacktrace.length";
		String MONITOR = "monitor";
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
		String MTC = "mtc";

		interface Fault {

			String MESSAGE = "message";
			String CLASSNAME = "classname";
			String STACKTRACE = "stacktrace";

		}
	}

	interface Resolver {
		String CDI = "cdi";
		String EJB = "cdi";
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
