/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	public void streamJavascriptServices(OutputStream out) {
		String jsname = getJsFilename();
		try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(jsname)) {
			if(null != in) {
				byte[] buffer = new byte[Constants.DEFAULT_BUFFER_SIZE];
				int n = 0;
				while (-1 != (n = in.read(buffer))) {
					out.write(buffer, 0, n);
				}
			} else {
				logger.warn("Generation of '{}' failed. File not found", jsname);
			}
		} catch(IOException ex) {
			logger.error("Generation of '"+jsname+"' failed.", ex);
		}
	}
	
}
