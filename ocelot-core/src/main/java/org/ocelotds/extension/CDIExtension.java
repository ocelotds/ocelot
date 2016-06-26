/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.extension;

import java.util.List;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import org.ocelotds.cache.JsCacheRemoveAllInterceptor;
import org.ocelotds.cache.JsCacheRemoveInterceptor;
import org.ocelotds.cache.JsCacheRemovesInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This extension add JsTopicInterceptor at runtime without configuration in beans.xml
 * @author hhfrancois
 */
public class CDIExtension implements Extension {

	private final static Logger logger = LoggerFactory.getLogger(CDIExtension.class);

	void afterTypeDiscovery(@Observes AfterTypeDiscovery afd, BeanManager beanManager) {
		List<Class<?>> interceptors = afd.getInterceptors();
		interceptors.add(JsTopicInterceptor.class);
		interceptors.add(SecureInterceptor.class);
		interceptors.add(JsCacheRemoveInterceptor.class);
		interceptors.add(JsCacheRemovesInterceptor.class);
		interceptors.add(JsCacheRemoveAllInterceptor.class);
		logger.debug("CDI : Add Interceptor {}, {}", JsTopicInterceptor.class, SecureInterceptor.class);
	}

}
