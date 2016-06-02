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
public class ModuleWriter implements ProcessorConstants, AngularConstants {

	/**
	 * try { angular.module('ocelot.ds'); } catch (e) { angular.module('ocelot.ds', []); }
	 *
	 * @param writer
	 * @throws IOException
	 */
	public static void writeModule(Writer writer) throws IOException {
		writer.append(TAB).append("try").append(SPACEOPTIONAL).append(OPENBRACE).append(CR); // \ttry {\n
		writer.append(TAB2).append(ANGULAR_MODULE).append(OPENPARENTHESIS).append(MODULENAME).append(CLOSEPARENTHESIS)
				  .append(SEMICOLON).append(CR); // \tangular.module('ocelot.ds');\n
		writer.append(TAB).append(CLOSEBRACE).append(SPACEOPTIONAL).append("catch").append(SPACEOPTIONAL)
				  .append(OPENPARENTHESIS).append("e").append(CLOSEPARENTHESIS).append(SPACEOPTIONAL).append(OPENBRACE)
				  .append(CR); // \t} catch (e) {\n
		writer.append(TAB2).append(ANGULAR_MODULE).append(OPENPARENTHESIS).append(MODULENAME).append(COMMA)
				  .append(SPACEOPTIONAL).append(BRACKETS).append(CLOSEPARENTHESIS).append(SEMICOLON)
				  .append(CR); // \t\tangular.module('ocelot.ds', []);\n
		writer.append(TAB).append(CLOSEBRACE).append(CR); // \t}\n
	}

	/**
	 * angular.module('ocelot.ds').type(type);
	 * @param writer
	 * @param type
	 * @param name
	 * @throws IOException 
	 */
	public static void writeAddition(Writer writer, String type, String name) throws IOException {
		writer.append(TAB).append(ANGULAR_MODULE).append(OPENPARENTHESIS).append(MODULENAME).append(CLOSEPARENTHESIS)
				  .append(DOT).append(type).append(OPENPARENTHESIS);
		if(name!=null) {
			writer.append("'").append(name).append("'").append(COMMA).append(SPACEOPTIONAL);
		}
		writer.append(type).append(CLOSEPARENTHESIS).append(SEMICOLON).append(CR); //\tangular.module('ocelot.ds').type(type);
	}
}
