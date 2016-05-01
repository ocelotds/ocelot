/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.services;

import java.lang.reflect.Method;
import java.util.Comparator;

/**
 * sort method by less argument
 * @author hhfrancois
 */
public class MethodComparator implements Comparator<Method> {

	@Override
	public int compare(Method o1, Method o2) {
		int res = o1.getParameterTypes().length - o2.getParameterTypes().length;
		if (res == 0) {
			return -1;
		}
		return res;
	}

}
