/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.marshalling.exceptions;

/**
 *
 * @author hhfrancois
 */
public class JsonUnmarshallingException extends Exception {

	/**
	 * Constructs an instance of <code>JsonUnmarshallingException</code> with the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public JsonUnmarshallingException(String msg) {
		super(msg);
	}
}
