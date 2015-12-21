/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

import java.io.File;
import java.net.URL;

/**
 *
 * @author hhfrancois
 */
public abstract class AbstractFileInitializer {
	/**
	 * Remove file on undeploy application
	 *
	 * @param filename
	 * @return 
	 */
	protected boolean deleteFile(String filename) {
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
}
