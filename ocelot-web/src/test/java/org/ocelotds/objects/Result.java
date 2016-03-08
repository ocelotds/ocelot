/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
