/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.dashboard.objects;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.TypeLiteral;

/**
 *
 * @author hhfrancois
 * @param <T>
 */
public class FakeCDI<T> extends CDI<T> implements Instance<T> {
	private final Collection<T> instances = new ArrayList<>();

	@Override
	public Instance<T> select(Annotation... qualifiers) {
		return this;
	}

	@Override
	public Instance<T> select(Class subtype, Annotation... qualifiers) {
		return this;
	}

	@Override
	public Instance<T> select(TypeLiteral subtype, Annotation... qualifiers) {
		return this;
	}

	@Override
	public boolean isUnsatisfied() {
		return instances.isEmpty();
	}

	@Override
	public boolean isAmbiguous() {
		return instances.size() > 1;
	}

	@Override
	public void destroy(T instance) {
	}

	@Override
	public Iterator<T> iterator() {
		return instances.iterator();
	}

	@Override
	public T get() {
		return instances.iterator().next();
	}

	public void add(T o) {
		instances.add(o);
	}

	public void clear() {
		instances.clear();
	}

	@Override
	public BeanManager getBeanManager() {
		return null;
	}
}
