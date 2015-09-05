/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import javax.inject.Inject;
import org.ocelotds.logger.OcelotLogger;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
public abstract class AbstractServiceProvider implements IServicesProvider {

	@Inject
	@OcelotLogger
	private Logger logger;

	protected abstract String getJsFilename();
	
	@Override
	public void streamJavascriptServices(Writer writer) {
		InputStream injs = this.getClass().getClassLoader().getResourceAsStream(getJsFilename());
		try (BufferedReader in = new BufferedReader(new InputStreamReader(injs, Constants.UTF_8))) {
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				writer.write(inputLine);
				writer.write(Constants.BACKSLASH_N);
			}
		} catch(IOException ex) {
			logger.error("Generation of '"+getJsFilename()+"' failed.", ex);
		}
	}
	
}
