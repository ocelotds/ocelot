/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.integration.conditions;

import org.assertj.core.api.Condition;

/**
 *
 * @author hhfrancois
 * @param <T>
 */
public class AreAtLeastOneDifferent<T>  extends Condition<T>  {

	final Iterable<T> ref;

	public AreAtLeastOneDifferent(Iterable<T> ref) {
		this.ref = ref;
	}

	@Override
	public boolean matches(T t) {
		for (T object : ref) {
			if (!object.equals(t)) {
				return true;
			}
		}
		return false;
	}
}
