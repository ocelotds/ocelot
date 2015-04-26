/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.ocelot.resolvers;

import fr.hhdev.ocelot.spi.DataServiceException;
import fr.hhdev.ocelot.spi.DataServiceResolverId;
import fr.hhdev.ocelot.spi.DataServiceResolver;
import fr.hhdev.ocelot.Constants;

/**
 *
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
