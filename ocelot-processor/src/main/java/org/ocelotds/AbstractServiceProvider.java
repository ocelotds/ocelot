/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
public abstract class AbstractServiceProvider implements IServicesProvider {

	private final Logger logger = LoggerFactory.getLogger(AbstractServiceProvider.class);

	protected abstract String getFilename();
	
	@Override
	public void streamJavascriptServices(OutputStream out) {
		String jsname = getFilename();
		try (InputStream in = getJsStream(jsname)) {
			if(null != in) {
				byte[] buffer = new byte[Constants.DEFAULT_BUFFER_SIZE];
				int n = 0;
				while (-1 != (n = in.read(buffer))) {
					out.write(buffer, 0, n);
				}
			} else {
				getLogger().warn("Generation of '{}' failed. File not found", jsname);
			}
		} catch(IOException ex) {
			getLogger().error("Generation of '"+jsname+"' failed.", ex);
		}
	}

	Logger getLogger() {
		return logger;
	}
	
	InputStream getJsStream(String jsname) {
		ClassLoader classLoader = getClassLoader();
		if(null != classLoader) {
			return classLoader.getResourceAsStream(jsname);
		}
		return null;
	}
	
	ClassLoader getClassLoader() {
		return this.getClass().getClassLoader();
	}
}
