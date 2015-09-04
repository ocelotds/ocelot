/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.resolvers;

import javax.enterprise.inject.Produces;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author hhfrancois
 */
public class ICProducer {
	
	@Produces
	private InitialContext getInitialContext() throws NamingException {
		return new InitialContext();
	}
	
}
