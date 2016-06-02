/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.frameworks.angularjs;

import java.io.IOException;
import java.io.Writer;
import org.ocelotds.frameworks.FwkWriter;
import static org.ocelotds.frameworks.angularjs.AngularConstants.FACTORY;
import org.ocelotds.processors.ProcessorConstants;

/**
 *
 * @author hhfrancois
 */
public class AngularFwk implements FwkWriter, ProcessorConstants {
	
	@Override
	public void writeHeaderService(Writer writer, String servicename) throws IOException {
		ClosureWriter.writeOpen(writer);
		ModuleWriter.writeModule(writer);
		ModuleWriter.writeAddition(writer, FACTORY, servicename);
		FunctionWriter.writeInjectDependenciesOnObject(writer, FACTORY, "promiseFactory");
		FunctionWriter.writeOpenFunctionWithDependencies(writer, FACTORY, "promiseFactory");
	}

	@Override
	public void writeFooterService(Writer writer) throws IOException {
		FunctionWriter.writeCloseFunction(writer);
		ClosureWriter.writeClose(writer);
	}
}
