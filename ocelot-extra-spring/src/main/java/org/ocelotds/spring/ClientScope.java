/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.spring;

import org.springframework.beans.factory.ObjectFactory;
import  org.springframework.beans.factory.config.Scope;

/**
 *
 * @author hhfrancois
 */
public class ClientScope implements Scope {

	@Override
	public String getConversationId() {
		return "client";
	}

	@Override
	public Object get(String string, ObjectFactory<?> of) {
		return of.getObject();
	}

	@Override
	public Object remove(String string) {
		return null;
	}

	@Override
	public void registerDestructionCallback(String string, Runnable r) {
	}

	@Override
	public Object resolveContextualObject(String string) {
		return null;
	}

}
