/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package resolver;

import fr.hhdev.ocelot.spi.DataServiceException;
import fr.hhdev.ocelot.spi.DataServiceResolverId;
import fr.hhdev.ocelot.spi.DataServiceResolver;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Resolver of POJO
 * @author hhfrancois
 */
@DataServiceResolverId("SINGLETON")
public class VerySimpleResolver implements DataServiceResolver {

	@Override
	public Object resolveDataService(String dataService) throws DataServiceException {
		try {
			return resolveDataService(Class.forName(dataService));
		} catch (ClassNotFoundException ex) {
			throw new DataServiceException(dataService, ex);
		}
	}

	@Override
	public <T> T resolveDataService(Class<T> clazz) throws DataServiceException {
		try {
			Method method = clazz.getDeclaredMethod("getInstance");
			return clazz.cast(method.invoke(null));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
			throw new DataServiceException(clazz.getName(), ex);
		}
	}
}
