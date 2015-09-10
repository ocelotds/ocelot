/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds;

import org.ocelotds.Constants;
import org.ocelotds.spi.DataServiceException;
import org.ocelotds.spi.DataServiceResolver;
import org.ocelotds.spi.IDataServiceResolver;
import org.ocelotds.spi.Scope;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * Resolver of SPRING
 *
 * @author hhfrancois
 */
@DataServiceResolver(Constants.Resolver.SPRING)
public class SpringResolver implements IDataServiceResolver {

	private static final Logger logger = LoggerFactory.getLogger(SpringResolver.class);

//	@Inject
	private ApplicationContext applicationContext = null;

	public SpringResolver() {
		if (Objects.isNull(applicationContext)) {
			applicationContext = ApplicationContextProvider.getApplicationContext();
		}
	}

	@Override
	public <T> T resolveDataService(Class<T> clazz) throws DataServiceException {

		System.out.println("Je passe la");

		Map<String, ?> beansOfType = applicationContext.getBeansOfType(clazz);
		if (Objects.isNull(beansOfType) || beansOfType.isEmpty()) {
			throw new DataServiceException("Unable to find any Spring bean of type : " + clazz.getName());
		}
		if (beansOfType.size() > 1) {
			throw new DataServiceException("Multiple (" + beansOfType.size() + ") Spring beans of type : '" + clazz.getName() + "' founded. Unable to choose one.");
		}
		return clazz.cast(beansOfType.values().iterator().next());
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public Scope getScope(Class clazz) {
		for (Annotation anno : clazz.getAnnotations()) {
			if (!anno.annotationType().getName().equals("org.ocelotds.annotations.DataService")) {
				if (anno.annotationType().equals(org.springframework.context.annotation.Scope.class)) {
					org.springframework.context.annotation.Scope springScopeAnno = (org.springframework.context.annotation.Scope) anno;
					if (springScopeAnno.value().contains("session")) {
						return Scope.SESSION;
					}
				}
			}
		}
		if (!this.applicationContext.isPrototype(clazz.getName()) && !this.applicationContext.isSingleton(clazz.getName())) {
			return Scope.SESSION;
		}
		return Scope.MANAGED;
	}
}
