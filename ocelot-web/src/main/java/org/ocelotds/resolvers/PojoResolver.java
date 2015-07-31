/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.resolvers;

import org.ocelotds.spi.DataServiceException;
import org.ocelotds.spi.DataServiceResolver;
import org.ocelotds.spi.IDataServiceResolver;
import org.ocelotds.Constants;
import org.ocelotds.spi.Scope;

/**
 * Resolver of POJO
 * @author hhfrancois
 */
@DataServiceResolver(Constants.Resolver.POJO)
public class PojoResolver implements IDataServiceResolver {

	@Override
	public <T> T resolveDataService(Class<T> clazz) throws DataServiceException {
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException ex) {
			throw new DataServiceException(clazz.getName(), ex);
		}
	}

	@Override
	public Scope getScope(Class clazz) {
		return Scope.MANAGED;
	}
}
