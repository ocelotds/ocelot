/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.processors;

/**
 *
 * @author hhfrancois
 */
public interface ProcessorConstants {

	String FUNCTION = "function";
	String SPACEOPTIONAL = " "; //" ";
	String SPACE = " "; //" ";
	String TAB = "\t"; //"\t";
	String TAB2 = TAB + TAB;
	String QUOTE = "\"";
	String CR = "\n"; //"\n";
	String TAB3 = TAB2 + TAB;
	String TAB4 = TAB3 + TAB;
	String OPENBRACE = "{";
	String CLOSEBRACE = "}";
	String PARENTHESIS = "()";
	String OPENPARENTHESIS = "(";
	String CLOSEPARENTHESIS = ")";
	String BRACKETS = "[]";
	String OPENBRACKET = "[";
	String CLOSEBRACKET = "]";
	String SEMICOLON = ";";
	String COLON = ":";
	String EQUALS = "=";
	String COMMA = ",";
	String DOT = ".";
	String ASTERISK = "*";
	String UNDERSCORE = "_";
	String PLUS = "+";
	String USESTRICT = "'use strict'";
	
	String DATASERVICE_AT = "org.ocelotds.annotations.DataService";
	String JSCACHERM_AT = "org.ocelotds.annotations.JsCacheRemove";
	String JSCACHERMS_AT = "org.ocelotds.annotations.JsCacheRemoves";
	String DIRECTORY = "jsdir";
	String FRAMEWORK = "jsfwk";
	String JSPROMISECREATORSCRIPT = "/js/promisefactory.js";
	String CORESCRIPT = "ocelot-core.js";
	String COREMINSCRIPT = "ocelot-core-min.js";
	String JSCORESCRIPT = "/js/ocelot-core.js";
	String JSCOREMINSCRIPT = "/js/ocelot-core-min.js";
	String JSOCELOTSERVICES = "/js/OcelotServices.js";
}
