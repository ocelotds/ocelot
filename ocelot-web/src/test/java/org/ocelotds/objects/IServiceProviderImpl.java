/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.objects;

import org.ocelotds.AbstractServiceProvider;

/**
 *
 * @author hhfrancois
 */
public class IServiceProviderImpl extends AbstractServiceProvider {

	public final static String FILENAME = "test.js";

	@Override
	public String getJsFilename() {
		return FILENAME;
	}

}
