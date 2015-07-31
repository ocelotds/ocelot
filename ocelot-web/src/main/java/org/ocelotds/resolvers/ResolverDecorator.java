/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.resolvers;

import org.ocelotds.annotations.DataService;
import org.ocelotds.spi.DataServiceException;
import org.ocelotds.spi.IDataServiceResolver;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

/**
 *
 * @author hhfrancois
 */
@Decorator
public abstract class ResolverDecorator implements IDataServiceResolver {

	@Inject
	@Delegate
	@Any
	IDataServiceResolver resolver;

	@Override
	public <T> T resolveDataService(Class<T> clazz) throws DataServiceException {
		checkDataService(clazz);
		return resolver.resolveDataService(clazz);
	}

	/**
	 * Check if class is DataService
	 *
	 * @param cls
	 * @throws DataServiceException
	 */
	protected void checkDataService(Class cls) throws DataServiceException {
		if (!cls.isAnnotationPresent(DataService.class)) {
			throw new DataServiceException("Unreachable DataService : "+cls.getSimpleName());
		}
	}
}
