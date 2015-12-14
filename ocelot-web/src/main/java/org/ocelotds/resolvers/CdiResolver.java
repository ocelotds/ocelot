/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.resolvers;

import org.ocelotds.spi.DataServiceException;
import org.ocelotds.spi.DataServiceResolver;
import org.ocelotds.spi.IDataServiceResolver;
import org.ocelotds.Constants;
import org.ocelotds.annotations.DataService;
import org.ocelotds.spi.Scope;
import java.lang.annotation.Annotation;
import java.util.Set;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * Resolver of CDI
 *
 * @author hhfrancois
 */
@DataServiceResolver(Constants.Resolver.CDI)
public class CdiResolver implements IDataServiceResolver {

	@Inject
	BeanManager beanManager;
	
	@Override
	public <T> T resolveDataService(Class<T> clazz) throws DataServiceException {
//		return CDI.current().select(clazz).get(); // equivalent, but no testable
		Set<Bean<?>> beans = beanManager.getBeans(clazz, new DataserviceLiteral());
		for (Bean<?> b : beans) {
			final CreationalContext context = beanManager.createCreationalContext(b);
			return clazz.cast(beanManager.getReference(b, b.getBeanClass(), context));
		}
		throw new DataServiceException(clazz.getName());
	}

	@Override
	public Scope getScope(Class clazz) {
		for (Annotation anno : clazz.getAnnotations()) {
			if (!anno.annotationType().equals(DataService.class)) {
				String annoName = anno.annotationType().getName();
				switch (annoName) {
					case "javax.enterprise.context.Dependent":
						return Scope.SESSION;
					case "javax.ejb.Stateful":
						return Scope.SESSION;
					default:
				}
			}
		}
		return Scope.MANAGED;
	}
}
