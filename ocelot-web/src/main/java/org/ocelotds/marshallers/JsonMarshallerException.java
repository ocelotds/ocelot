/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.marshallers;

/**
 *
 * @author hhfrancois
 */
public class JsonMarshallerException extends Exception {

	/**
	 * Constructs an instance of <code>JsonMarshallerException</code> with the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public JsonMarshallerException(String msg) {
		super(msg);
	}
}
