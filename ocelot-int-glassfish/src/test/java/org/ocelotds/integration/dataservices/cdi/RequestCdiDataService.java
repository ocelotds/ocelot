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

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.CDI)
public class RequestCdiDataService implements GetValue {

	private double d;

	@PostConstruct
	private void init() {
		d = Math.random();
	}

	@Override
	public double getValue() {
		try {
			Thread.sleep(200L);
		} catch (InterruptedException ex) {
		}
		return d;
	}

	@Override
	public void setValue(double d) {
		this.d = d;
	}

	public double getValueTempo() {
		try {
			Thread.sleep(200L);
		} catch (InterruptedException ex) {
		}
		return d;
	}
}
