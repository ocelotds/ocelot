/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.spi;

/**
 * Implemente this class to do a resolver
 * @author hhfrancois
 */
public interface IDataServiceResolver {

	/**
	 * Return a instance for class by contextual resolver
	 * @param <T>
	 * @param clazz
	 * @return a dataservice
	 * @throws DataServiceException 
	 */
	<T> T resolveDataService(Class<T> clazz) throws DataServiceException;
	
	/**
	 * Return scope for bean in resolver context
	 * @param clazz
	 * @return 
	 */
	Scope getScope(Class clazz);
}
