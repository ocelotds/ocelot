/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.objects;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hhfrancois
 */
public class OcelotService {
	final String name;
	final List<OcelotMethod> methods = new ArrayList<>();

	public OcelotService(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public List<OcelotMethod> getMethods() {
		return methods;
	}
}
