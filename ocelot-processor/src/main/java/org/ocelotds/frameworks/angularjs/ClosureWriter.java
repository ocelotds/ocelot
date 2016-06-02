/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.frameworks.angularjs;

import java.io.IOException;
import java.io.Writer;
import org.ocelotds.processors.ProcessorConstants;
/**
 *
 * @author hhfrancois
 */
public class ClosureWriter implements ProcessorConstants, AngularConstants {

	/**
	 * (function () { 
	 *		'use strict';
	 * @param writer
	 * @throws IOException 
	 */
	public static void writeOpen(Writer writer) throws IOException {
		writer.append(OPENPARENTHESIS).append(FUNCTION).append(PARENTHESIS).append(SPACEOPTIONAL)
				  .append(OPENBRACE).append(CR); // (function() {\n
		writer.append(TAB).append(USESTRICT).append(SEMICOLON).append(CR); // 'use strict';
	}

	/**
	 * })();
	 * @param writer
	 * @throws IOException 
	 */
	public static void writeClose(Writer writer) throws IOException {
		writer.append(CLOSEBRACE).append(CLOSEPARENTHESIS).append(PARENTHESIS).append(SEMICOLON); // })();
	}
}
