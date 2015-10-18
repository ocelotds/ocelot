/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.spring.extension;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import org.ocelotds.spring.OcelotSpringConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author hhfrancois
 */
public class CDIExtension implements Extension {
	private final static Logger logger = LoggerFactory.getLogger(CDIExtension.class);

	<T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {
		if (pat.getAnnotatedType().isAnnotationPresent(Configuration.class)) {
			final AnnotatedType<T> type = pat.getAnnotatedType();
			AnnotatedType<T> wrapped = new SpringConfigurationWrapper<>(type);
			pat.setAnnotatedType(wrapped);
			logger.debug("Configuration is added as CDIBean with Annotation: {} : {}", OcelotSpringConfiguration.class, wrapped);
		}
	}
}
