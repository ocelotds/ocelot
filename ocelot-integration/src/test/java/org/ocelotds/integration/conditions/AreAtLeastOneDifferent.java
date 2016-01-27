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
 */
public class AreAtLeastOneDifferent extends Condition<Object> {

	final Object[] ref;

	public AreAtLeastOneDifferent(Object[] ref) {
		this.ref = ref;
	}

	@Override
	public boolean matches(Object t) {
		for (Object object : ref) {
			if (!object.equals(t)) {
				return true;
			}
		}
		return false;
	}
}
