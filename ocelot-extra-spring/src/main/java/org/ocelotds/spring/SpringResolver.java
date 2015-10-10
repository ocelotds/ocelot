/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.spring;

import org.ocelotds.spi.DataServiceException;
import org.ocelotds.spi.DataServiceResolver;
import org.ocelotds.spi.IDataServiceResolver;
import org.ocelotds.spi.Scope;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.ocelotds.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Resolver of SPRING
 *
 * @author hhfrancois
 */
@DataServiceResolver(Constants.Resolver.SPRING)
@ApplicationScoped
public class SpringResolver implements IDataServiceResolver{

	private static final Logger logger = LoggerFactory.getLogger(SpringResolver.class);
	
	@Inject
	@Any
	@OcelotSpringConfiguration		  
	Instance<Object> ocelotSpringConfigs;

	private AnnotationConfigApplicationContext applicationContext;
	
	public AnnotationConfigApplicationContext getApplicationContext() {
		logger.debug("Initialisation du context Spring");
		if(applicationContext == null) {
			applicationContext =  new AnnotationConfigApplicationContext();
			for (Object ocelotSpringConfig : ocelotSpringConfigs) {
				logger.debug("Find 1 context Spring that implements OcelotSpringConfig");
				applicationContext.register(ocelotSpringConfig.getClass());
			}
			applicationContext.refresh();
		}
		return applicationContext;
	}

	@Override
	public <T> T resolveDataService(Class<T> clazz) throws DataServiceException {
		Map<String, ?> beansOfType = getApplicationContext().getBeansOfType(clazz);
		if (beansOfType == null || beansOfType.isEmpty()) {
			throw new DataServiceException("Unable to find any Spring bean of type : " + clazz.getName());
		}
		if (beansOfType.size() > 1) {
			throw new DataServiceException("Multiple (" + beansOfType.size() + ") Spring beans of type : '" + clazz.getName() + "' founded. Unable to choose one.");
		}
		return clazz.cast(beansOfType.values().iterator().next());
	}

	@Override
	public Scope getScope(Class clazz) {
		AnnotationConfigApplicationContext applicationCtx = this.getApplicationContext();
		Map<String, ?> beansOfType = applicationCtx.getBeansOfType(clazz);
		if(beansOfType != null) {
			String next = beansOfType.keySet().iterator().next();
			if(next!=null) {
				if (!applicationCtx.isPrototype(next) && !applicationCtx.isSingleton(next)) {
					return Scope.SESSION;
				}
			}
		}
		return Scope.MANAGED;
	}
}
