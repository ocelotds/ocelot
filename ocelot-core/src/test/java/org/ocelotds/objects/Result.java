/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.objects;

/**
 *
 * @author hhfrancois
 */
public class Result {
	
	public Result() {
	}

	public Result(int integer) {
		this.integer = integer;
	}

	private int integer;
	public int fieldOfInstance;
	private static int fieldOfClass;

	public int getInteger() {
		return integer;
	}

	public void setInteger(int integer) {
		this.integer = integer;
	}
	
	public static Result getMock() {
		Result result = new Result(5);
		return result;
	}
	
}
