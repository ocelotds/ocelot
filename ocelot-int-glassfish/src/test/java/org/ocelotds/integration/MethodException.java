/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.integration;

/**
 *
 * @author hhfrancois
 */
public class MethodException extends Exception {

	public MethodException(String message) {
		super(message);
	}

	public MethodException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
