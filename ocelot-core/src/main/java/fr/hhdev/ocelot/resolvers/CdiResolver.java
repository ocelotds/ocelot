/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot.resolvers;

import fr.hhdev.ocelot.spi.DataServiceException;
import fr.hhdev.ocelot.spi.DataServiceResolver;
import fr.hhdev.ocelot.spi.IDataServiceResolver;
import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.annotations.DataService;
import fr.hhdev.ocelot.spi.Scope;
import java.lang.annotation.Annotation;
import java.util.Set;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * Resolver of POJO
 *
 * @author hhfrancois
 */
@DataServiceResolver(Constants.Resolver.CDI)
public class CdiResolver implements IDataServiceResolver {

	@Inject
	BeanManager beanManager;

	@Override
	public <T> T resolveDataService(Class<T> clazz) throws DataServiceException {
		Set<Bean<?>> beans = beanManager.getBeans(clazz);
		for (Bean<?> b : beans) {
			final CreationalContext creationalContext = beanManager.createCreationalContext(b);
			return clazz.cast(beanManager.getReference(b, b.getBeanClass(), creationalContext));
		}
		throw new DataServiceException(clazz.getName());
	}

	@Override
	public Scope getScope(Class clazz) {
		for (Annotation anno : clazz.getAnnotations()) {
			if(!anno.annotationType().equals(DataService.class)) {
				String annoName =anno.annotationType().getName();
				switch (annoName) {
					case "javax.enterprise.context.SessionScoped":
						return Scope.SESSION;
					case "javax.enterprise.context.ConversationScoped":
						return Scope.SESSION;
					default:
				}
			}
		}
		return Scope.MANAGED;
	}
}
