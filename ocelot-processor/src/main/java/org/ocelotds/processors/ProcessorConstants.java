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
	String SPACEOPTIONAL = ""; //" ";
	String SPACE = " "; //" ";
	String TAB = ""; //"\t";
	String TAB2 = TAB + TAB;
	String QUOTE = "\"";
	String CR = ""; //"\n";
	String TAB3 = TAB2 + TAB;
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
	String DIRECTORY = "jsdir";
	String FRAMEWORK = "jsfwk";
	String PROMISECREATORSCRIPT = "/js/main.js";
}
