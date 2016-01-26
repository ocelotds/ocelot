/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.integration.dataservices.ejb;

import org.ocelotds.integration.dataservices.*;
import org.ocelotds.Constants;
import org.ocelotds.annotations.DataService;
import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 *
 * @author hhfrancois
 */
@Stateless
@LocalBean
@DataService(resolver = Constants.Resolver.EJB)
public class RequestEjbDataService implements GetValue {
	
	private double d;

	@PostConstruct
	protected void init() {
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
