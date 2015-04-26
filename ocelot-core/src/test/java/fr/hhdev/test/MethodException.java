/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.test;

/**
 *
 * @author hhfrancois
 */
class MethodException extends Exception {

	public MethodException(String message) {
		super(message);
	}

	public MethodException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
