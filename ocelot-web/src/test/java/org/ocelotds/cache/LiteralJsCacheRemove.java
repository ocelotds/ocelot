/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.cache;

import javax.enterprise.util.AnnotationLiteral;
import org.ocelotds.annotations.JsCacheRemove;

/**
 *
 * @author hhfrancois
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class LiteralJsCacheRemove  extends AnnotationLiteral<JsCacheRemove> implements JsCacheRemove {
	final Class cls;
	final String methodName;
	final String[] keys;
	final boolean userScope;

	public LiteralJsCacheRemove(Class cls, String methodName, String[] keys, boolean userScope) {
		this.cls = cls;
		this.methodName = methodName;
		this.keys = keys;
		this.userScope = userScope;
	}
	
	
	@Override
	public Class cls() {
		return cls;
	}

	@Override
	public String methodName() {
		return methodName;
	}

	@Override
	public String[] keys() {
		return keys;
	}

	@Override
	public boolean userScope() {
		return userScope;
	}
}
