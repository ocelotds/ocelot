/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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

	public double getValueTempo() {
		try {
			Thread.sleep(200L);
		} catch (InterruptedException ex) {
		}
		return d;
	}
}
