/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

import java.lang.reflect.Method;

/**
 *
 * @author hhfrancois
 */
public class InvocationContext {
	
	final Method method;
	final Object[] parameters;

	public InvocationContext(Method method, Object[] parameters) {
		this.method = method;
		this.parameters = parameters;
	}

	public Method getMethod() {
		return method;
	}

	public Object[] getParameters() {
		return parameters;
	}
	
	@Override
	public String toString() {
		if(null != this.method) {
			return this.method.getDeclaringClass().getName()+"."+this.method.getName();
		}
		return null;
	}
	
	
	
}
