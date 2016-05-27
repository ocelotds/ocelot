/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.frameworks;

import java.io.IOException;
import java.io.Writer;
import org.ocelotds.processors.ProcessorConstants;

/**
 *
 * @author hhfrancois
 */
public class NoFwk implements FwkWriter, ProcessorConstants {

	@Override
	public void writeHeaderService(Writer writer, String instanceName) throws IOException {
		writer.append("var").append(SPACE).append(instanceName).append(SPACEOPTIONAL).append(EQUALS).append(SPACEOPTIONAL)
				  .append(OPENPARENTHESIS).append(FUNCTION).append(SPACEOPTIONAL).append(PARENTHESIS).append(SPACEOPTIONAL)
				  .append(OPENBRACE).append(CR); //var instanceName = (function () {\n
		writer.append(TAB).append(USESTRICT).append(SEMICOLON).append(CR); //\t'use strict';\n
	}

	@Override
	public void writeFooterService(Writer writer) throws IOException {
		writer.append(CLOSEBRACE).append(CLOSEPARENTHESIS).append(PARENTHESIS).append(SEMICOLON); //})();
	}
	
}
