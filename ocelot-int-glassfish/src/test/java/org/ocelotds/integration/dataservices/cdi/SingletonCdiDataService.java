/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.integration.dataservices.cdi;

import org.ocelotds.integration.dataservices.*;
import org.ocelotds.Constants;
import org.ocelotds.annotations.DataService;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.CDI)
@ApplicationScoped
public class SingletonCdiDataService implements GetValue {
	
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
