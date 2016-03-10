/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
