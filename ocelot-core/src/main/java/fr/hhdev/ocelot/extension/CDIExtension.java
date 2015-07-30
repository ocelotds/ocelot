/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.extension;

import java.util.List;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
public class CDIExtension implements Extension {

	private final static Logger logger = LoggerFactory.getLogger(CDIExtension.class);

	<T> void processAnnotatedType(@Observes AfterTypeDiscovery afd, BeanManager beanManager) {
		List<Class<?>> interceptors = afd.getInterceptors();
		interceptors.add(JsTopicInterceptor.class);
		logger.debug("CDI : Add Interceptor {}", JsTopicInterceptor.class);
	}

}
