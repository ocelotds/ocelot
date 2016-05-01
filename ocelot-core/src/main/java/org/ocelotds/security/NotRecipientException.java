/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

/**
 *
 * @author hhfrancois
 */
public class NotRecipientException extends Exception {

	/**
	 * Constructs an instance of <code>NotRecipientException</code> with the specified detail message.
	 *
	 * @param principalName the user
	 */
	public NotRecipientException(String principalName) {
		super(principalName);
	}
}
