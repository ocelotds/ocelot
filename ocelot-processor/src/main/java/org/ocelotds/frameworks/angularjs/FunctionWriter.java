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
public class FunctionWriter implements ProcessorConstants, AngularConstants {
	/**
	 * \tobject.$inject = ['dep1', 'dep2'];
	 *
	 * @param writer
	 * @param object
	 * @param dependencies
	 * @throws IOException
	 */
	public void writeInjectDependenciesOnObject(Writer writer, String object, String... dependencies) throws IOException {
		if (dependencies != null && dependencies.length > 0) {
			writer.append(TAB).append(object).append(DOT).append("$inject").append(SPACEOPTIONAL).append(EQUALS).append(SPACEOPTIONAL).append(OPENBRACKET);
			writeDependencies(writer, "'", dependencies);
			writer.append(CLOSEBRACKET).append(SEMICOLON).append(CR);
		}
	}

	/**
	 * dep1, dep2 or if deco = "'" 'dep1', 'dep2'
	 *
	 * @param writer
	 * @param deco
	 * @param dependencies
	 * @throws IOException
	 */
	void writeDependencies(Writer writer, String deco, String... dependencies) throws IOException {
		boolean first = true;
		for (String dependency : dependencies) {
			if (!first) {
				writer.append(COMMA).append(SPACEOPTIONAL);
			}
			writer.append(deco).append(dependency).append(deco);
			first = false;
		}
	}

	/**
	 *  \tfunction object(dep1, dep2) {\n
	 *
	 * @param writer
	 * @param object
	 * @param dependencies
	 * @throws IOException
	 */
	public void writeOpenFunctionWithDependencies(Writer writer, String object, String... dependencies) throws IOException {
		writer.append(TAB).append("/* @ngInject */").append(CR);
		writer.append(TAB).append(FUNCTION).append(SPACE).append(object).append(OPENPARENTHESIS);
		writeDependencies(writer, "", dependencies);
		writer.append(CLOSEPARENTHESIS).append(SPACEOPTIONAL).append(OPENBRACE).append(CR); //\tfunction config(dep1, dep2) {\n

	}

	/**
	 * \t}
	 * @param writer
	 * @throws IOException 
	 */
	public void writeCloseFunction(Writer writer) throws IOException {
		writer.append(TAB).append(CLOSEBRACE).append(CR);
	}
	
}
