/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot.resolvers;

import fr.hhdev.ocelot.spi.DataServiceException;
import fr.hhdev.ocelot.spi.DataServiceResolverId;
import fr.hhdev.ocelot.spi.DataServiceResolver;
import fr.hhdev.ocelot.Constants;

/**
 * Resolver of POJO
 * @author hhfrancois
 */
@DataServiceResolverId(Constants.Resolver.POJO)
public class PojoResolver implements DataServiceResolver {

	@Override
	public Object resolveDataService(String dataService) throws DataServiceException {
		try {
			return Class.forName(dataService).newInstance();
		} catch (ClassNotFoundException |InstantiationException | IllegalAccessException ex) {
			throw new DataServiceException(dataService, ex);
		}
	}
}
