/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.frameworks.angularjs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import org.ocelotds.processors.ProcessorConstants;

/**
 *
 * @author hhfrancois
 */
public class BodyWriter implements ProcessorConstants, AngularConstants {
	
	
	 public void write(Writer writer, InputStream body) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(body))) {
			String line;
			while ((line = reader.readLine()) != null) {
				writer.write(line);
				writer.write("\n");
			}
		}
	}
	
	public InputStream getInputStream(String resource) {
		return BodyWriter.class.getResourceAsStream(resource);
	}
	
}
