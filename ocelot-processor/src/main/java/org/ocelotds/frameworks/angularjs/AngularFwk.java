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
	
	ClosureWriter closureWriter = null;
	FunctionWriter functionWriter = null;
	ModuleWriter moduleWriter = null;
	
	@Override
	public void writeHeaderService(Writer writer, String servicename) throws IOException {
		getClosureWriter().writeOpen(writer);
		getModuleWriter().writeModule(writer);
		getModuleWriter().writeAddition(writer, FACTORY, servicename);
		getFunctionWriter().writeInjectDependenciesOnObject(writer, FACTORY, "promiseFactory");
		getFunctionWriter().writeOpenFunctionWithDependencies(writer, FACTORY, "promiseFactory");
	}

	@Override
	public void writeFooterService(Writer writer) throws IOException {
		functionWriter.writeCloseFunction(writer);
		getClosureWriter().writeClose(writer);
	}

	public ClosureWriter getClosureWriter() {
		if(closureWriter==null) {
			closureWriter = new ClosureWriter();
		}
		return closureWriter;
	}

	public FunctionWriter getFunctionWriter() {
		if(functionWriter==null) {
			functionWriter = new FunctionWriter();
		}
		return functionWriter;
	}

	public ModuleWriter getModuleWriter() {
		if(moduleWriter==null) {
			moduleWriter = new ModuleWriter();
		}
		return moduleWriter;
	}

 
}
