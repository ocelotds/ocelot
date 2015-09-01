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
import org.ocelotds.security.JsTopicAccessController;

/**
 *
 * @author hhfrancois
 */
public class FakeCDI implements Instance<JsTopicAccessController> {
	private final Collection<JsTopicAccessController> instances = new ArrayList<>();

	@Override
	public Instance<JsTopicAccessController> select(Annotation... qualifiers) {
		return null;
	}

	@Override
	public Instance<JsTopicAccessController> select(Class subtype, Annotation... qualifiers) {
		return null;
	}

	@Override
	public Instance<JsTopicAccessController> select(TypeLiteral subtype, Annotation... qualifiers) {
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
	public void destroy(JsTopicAccessController instance) {
	}

	@Override
	public Iterator iterator() {
		return instances.iterator();
	}

	@Override
	public JsTopicAccessController get() {
		return instances.iterator().next();
	}

	public void add(JsTopicAccessController o) {
		instances.add(o);
	}
	
}
