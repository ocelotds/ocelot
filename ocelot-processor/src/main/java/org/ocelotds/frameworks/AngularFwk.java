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
public class AngularFwk implements FwkWriter, ProcessorConstants {
	@Override
	public void writeHeaderService(Writer writer, String servicename) throws IOException {
		writer.append(OPENPARENTHESIS).append(FUNCTION).append(PARENTHESIS).append(SPACEOPTIONAL)
				  .append(OPENBRACE).append(CR); // (function() {\n
		writer.append(TAB).append("'use strict'").append(SEMICOLON).append(CR); // \t'use strict';\n
		writer.append(TAB).append("try").append(SPACEOPTIONAL).append(OPENBRACE).append(CR); // \ttry {\n
		writer.append(TAB2).append("angular.module").append(OPENPARENTHESIS).append("'ocelotds'").append(CLOSEPARENTHESIS)
				  .append(SEMICOLON).append(CR); // \tangular.module('ocelotds');\n
		writer.append(TAB).append(CLOSEBRACE).append(SPACEOPTIONAL).append("catch").append(SPACEOPTIONAL)
				  .append(OPENPARENTHESIS).append("e").append(CLOSEPARENTHESIS).append(SPACEOPTIONAL).append(OPENBRACE)
				  .append(CR); // \t} catch (e) {\n
		writer.append(TAB2).append("angular.module").append(OPENPARENTHESIS).append("'ocelotds'").append(COMMA)
				  .append(SPACEOPTIONAL).append(BRACKETS).append(CLOSEPARENTHESIS).append(SEMICOLON)
				  .append(CR); // \t\tangular.module('ocelotds', []);\n
		writer.append(TAB).append(CLOSEBRACE).append(CR); // \t}\n
		writer.append(TAB).append("angular.module").append(OPENPARENTHESIS).append("'ocelotds'").append(CLOSEPARENTHESIS)
				  .append(DOT).append("factory").append(OPENPARENTHESIS).append("'").append(servicename).append("'")
				  .append(COMMA).append(SPACEOPTIONAL).append("service").append(CLOSEPARENTHESIS).append(SEMICOLON)
				  .append(CR); // \tangular.module('ocelotds').factory('servicename', service);
		writer.append(TAB).append(FUNCTION).append(SPACE).append("service").append(PARENTHESIS).append(SPACEOPTIONAL)
				  .append(OPENBRACE).append(CR); // \tfunction service() {\n
	}

	@Override
	public void writeFooterService(Writer writer) throws IOException {
		writer.append(TAB).append(CLOSEBRACE).append(CR); //\t}\n
		writer.append(CLOSEBRACE).append(CLOSEPARENTHESIS).append(PARENTHESIS).append(SEMICOLON); // })();
	}
}
