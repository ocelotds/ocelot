/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import javax.inject.Inject;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotLogger;
import static org.ocelotds.configuration.JsFileInitializer.OCELOT_CORE_RESOURCE;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
public abstract class AbstractFileInitializer {

	@Inject
	@OcelotLogger
	private Logger logger;

	/**
	 * Remove file on undeploy application
	 *
	 * @param filename
	 * @return
	 */
	boolean deleteFile(String filename) {
		if (null != filename && !filename.isEmpty()) {
			File file = new File(filename);
			if (file.exists()) {
				return file.delete();
			}
		}
		return false;
	}

	URL getContentURL(String resourcename) {
		return AbstractFileInitializer.class.getResource(resourcename);
	}

	boolean writeStreamToOutputStream(InputStream input, OutputStream out) {
		if (null != input && null != out) {
			try (InputStream in = input) {
				byte[] buffer = new byte[Constants.DEFAULT_BUFFER_SIZE];
				int n = 0;
				while (-1 != (n = in.read(buffer))) {
					out.write(buffer, 0, n);
				}
				return true;
			} catch (IOException ex) {
				logger.error("Generation of '" + OCELOT_CORE_RESOURCE + "' failed.", ex);
			}
		} else {
			logger.warn("Generation of '{}' failed. File not found", OCELOT_CORE_RESOURCE);
		}
		return false;
	}
}
