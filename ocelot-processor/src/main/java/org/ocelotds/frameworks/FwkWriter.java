/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.frameworks;

import java.io.Writer;
import java.io.IOException;
/**
 *
 * @author hhfrancois
 */
public interface FwkWriter {
	void writeHeaderService(Writer writer, String servicename) throws IOException;
	void writeFooterService(Writer writer) throws IOException;
	
}
