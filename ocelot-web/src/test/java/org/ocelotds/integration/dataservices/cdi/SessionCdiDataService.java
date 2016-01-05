/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.integration.dataservices.cdi;

import org.ocelotds.integration.dataservices.*;
import org.ocelotds.Constants;
import org.ocelotds.annotations.DataService;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

/**
 *
 * @author hhfrancois
 */
@Dependent
@DataService(resolver = Constants.Resolver.CDI)
public class SessionCdiDataService implements GetValue {
	private double d;
	
	@PostConstruct
	private void init() {
		d = Math.random();
	}

	@Override
	public double getValue() {
		return d;
	}

	@Override
	public void setValue(double d) {
		this.d = d;
	}
}
