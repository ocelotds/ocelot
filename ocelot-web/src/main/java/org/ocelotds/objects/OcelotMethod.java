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
public class OcelotMethod {
	final String name;
	final String returntype;
	final List<String> argtypes = new ArrayList<>();
	final List<String> argnames = new ArrayList<>();
	final List<String> argtemplates = new ArrayList<>();

	public OcelotMethod(String name, String returntype) {
		this.name = name;
		this.returntype = returntype;
	}
	
	public String getName() {
		return name;
	}

	public String getReturntype() {
		return returntype;
	}

	public List<String> getArgtypes() {
		return argtypes;
	}

	public List<String> getArgnames() {
		return argnames;
	}

	public List<String> getArgtemplates() {
		return argtemplates;
	}
}
