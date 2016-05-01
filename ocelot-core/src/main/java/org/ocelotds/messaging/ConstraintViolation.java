/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.messaging;

/**
 *
 * @author hhfrancois
 */
public class ConstraintViolation {

	String message = null;
	int index = 0;
	String name = null;
	String prop = null;

	public ConstraintViolation(String message) {
		this.message = message;
	}

	public ConstraintViolation() {

	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProp() {
		return prop;
	}

	public void setProp(String prop) {
		this.prop = prop;
	}

	@Override
	public String toString() {
		if (prop != null) {
			return "ConstraintViolation{" + name + "." + prop + " " + message + '}';
		}
		return "ConstraintViolation{" + name + " " + message + '}';
	}
}
