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
import org.ocelotds.annotations.OcelotLogger;
import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Resolver of SPRING
 *
 * @author hhfrancois
 */
@DataServiceResolver(Constants.Resolver.SPRING)
@ApplicationScoped
public class SpringResolver implements IDataServiceResolver {

	@Inject
	@OcelotLogger
	Logger logger;

	@Inject
	@Any
	@OcelotSpringConfiguration
	Instance<Object> ocelotSpringConfigs;

	AnnotationConfigApplicationContext applicationContext;

	public AnnotationConfigApplicationContext getApplicationContext() {
		logger.debug("Init Spring context");
		if (applicationContext == null) {
			applicationContext = new AnnotationConfigApplicationContext();
			ClientScope clientScope = new ClientScope();
			applicationContext.getBeanFactory().registerScope(clientScope.getConversationId(), clientScope);
			for (Object ocelotSpringConfig : ocelotSpringConfigs) {
				logger.info("Find context Spring {}", ocelotSpringConfig.getClass());
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
		logger.debug("Try to get scope of class {}", clazz);
		AnnotationConfigApplicationContext applicationCtx = this.getApplicationContext();
		Map<String, ?> beansOfType = applicationCtx.getBeansOfType(clazz);
		if (beansOfType != null) {
			logger.debug("Try to get scope of class {} from beans", clazz);
			String bean = beansOfType.keySet().iterator().next();
			logger.debug("Try to get scope of class {} from bean {}", clazz, bean);
			if (bean != null) {
				boolean prototype = applicationCtx.isPrototype(bean);
				boolean singleton = applicationCtx.isSingleton(bean);
				logger.debug("Try to get scope of class {} prototype : {}, singleton : {}", clazz, prototype, singleton);
				if (!prototype && !singleton) {
					return Scope.SESSION;
				}
			}
		}
		return Scope.MANAGED;
	}
}
