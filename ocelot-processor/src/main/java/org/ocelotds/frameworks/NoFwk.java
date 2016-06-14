/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.frameworks;

import java.io.IOException;
import java.io.Writer;
import org.ocelotds.frameworks.angularjs.ClosureWriter;
import org.ocelotds.processors.ProcessorConstants;

/**
 *
 * @author hhfrancois
 */
public class NoFwk implements FwkWriter, ProcessorConstants {

	ClosureWriter closureWriter = null;

	@Override
	public void writeHeaderService(Writer writer, String instanceName) throws IOException {
		getClosureWriter().writeOpen(writer);
		writer.append("window.").append(instanceName).append(SPACEOPTIONAL).append(EQUALS).append(SPACEOPTIONAL);
		getClosureWriter().writeOpen(writer);
	}

	@Override
	public void writeFooterService(Writer writer) throws IOException {
		getClosureWriter().writeClose(writer);
		getClosureWriter().writeClose(writer);
	}
	
	public ClosureWriter getClosureWriter() {
		if(closureWriter==null) {
			closureWriter = new ClosureWriter();
		}
		return closureWriter;
	}
}
