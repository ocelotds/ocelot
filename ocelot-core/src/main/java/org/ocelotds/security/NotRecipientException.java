/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
