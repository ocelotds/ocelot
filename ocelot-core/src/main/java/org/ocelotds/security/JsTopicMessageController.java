/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

/**
 *
 * @author hhfrancois
 * @param <T>
 */
public interface JsTopicMessageController<T> {
	public void checkRight(UserContext ctx, String topic, T payload) throws NotRecipientException;
}
