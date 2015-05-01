/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot.spring;

import fr.hhdev.ocelot.spi.DataServiceException;
import fr.hhdev.ocelot.spi.DataServiceResolver;
import fr.hhdev.ocelot.spi.IDataServiceResolver;
import fr.hhdev.ocelot.spi.Scope;
import java.lang.annotation.Annotation;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * Resolver of SPRING
 *
 * @author hhfrancois
 */
@DataServiceResolver("SPRING")
public class SpringResolver implements IDataServiceResolver {

	private static final Logger logger = LoggerFactory.getLogger(SpringResolver.class);

	private ApplicationContext applicationContext;

	@Override
	public <T> T resolveDataService(Class<T> clazz) throws DataServiceException {
		Map<String, ?> beansOfType = applicationContext.getBeansOfType(clazz);
		if (beansOfType == null || beansOfType.isEmpty()) {
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
			if (!anno.annotationType().getName().equals("fr.hhdev.ocelot.annotations.DataService")) {
				if (anno.annotationType().equals(org.springframework.context.annotation.Scope.class)) {
					org.springframework.context.annotation.Scope springScopeAnno = (org.springframework.context.annotation.Scope) anno;
					if (springScopeAnno.value().contains("session")) {
						return Scope.SESSION;
					}
				}
			}
		}
		if(!this.applicationContext.isPrototype(clazz.getName()) && !this.applicationContext.isSingleton(clazz.getName())) {
			return Scope.SESSION;
		}
		return Scope.MANAGED;
	}
}
