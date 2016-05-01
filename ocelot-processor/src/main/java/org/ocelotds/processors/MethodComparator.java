/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.processors;

import java.util.Comparator;
import javax.lang.model.element.ExecutableElement;

/**
 * Sort method by name and number of arguments, more arguments before.
 *
 * @author hhfrancois
 */
public class MethodComparator implements Comparator<ExecutableElement> {

	@Override
	public int compare(ExecutableElement o1, ExecutableElement o2) {
		int name = o1.getSimpleName().toString().compareTo(o2.getSimpleName().toString());
		if (name == 0) {
			return compareByArgument(o1, o2);
		}
		return name;
	}

	int compareByArgument(ExecutableElement o1, ExecutableElement o2) {
		return o2.getParameters().size() - o1.getParameters().size();
	}
}
