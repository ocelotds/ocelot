/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.integration.objects;

import javax.validation.constraints.NotNull;

/**
 *
 * @author hhfrancois
 */
public class WithConstraint {
	@NotNull
	String name = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
