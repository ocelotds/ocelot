/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.frameworks;

import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ocelotds.processors.ProcessorConstants;

/**
 *
 * @author hhfrancois
 */
public abstract class AbstractFwkTest implements ProcessorConstants {

	public abstract FwkWriter getInstance();

	/**
	 * Test of writeHeaderService method, of class AngularFwk.
	 * @throws java.lang.Exception
	 */
	public void testWriteHeaderFooterService() throws Exception {
		System.out.println("writeHeaderService");
		Writer writer = WriterTest.getMockWriter();
		Map<String, Object> params = new HashMap<>();
		params.put("type", "factory");
		getInstance().writeHeaderService(writer, "servicename");
		getInstance().writeFooterService(writer);
		List<String> allValues = WriterTest.testBraces(writer);
	}
}