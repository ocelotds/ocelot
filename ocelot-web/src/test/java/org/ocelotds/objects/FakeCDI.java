/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.objects;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

/**
 *
 * @author hhfrancois
 */
public class FakeCDI implements Instance {
	private final Collection instances = new ArrayList<>();

	@Override
	public Instance select(Annotation... qualifiers) {
		return null;
	}

	@Override
	public Instance select(Class subtype, Annotation... qualifiers) {
		return null;
	}

	@Override
	public Instance select(TypeLiteral subtype, Annotation... qualifiers) {
		return null;
	}

	@Override
	public boolean isUnsatisfied() {
		return false;
	}

	@Override
	public boolean isAmbiguous() {
		return false;
	}

	@Override
	public void destroy(Object instance) {
	}

	@Override
	public Iterator iterator() {
		return instances.iterator();
	}

	@Override
	public Object get() {
		return null;
	}

	public void add(Object o) {
		instances.add(o);
	}
	
}
