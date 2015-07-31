/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import java.lang.annotation.Annotation;
import java.util.Set;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
public abstract class CdiBootstrap {

	private final static Logger logger = LoggerFactory.getLogger(CdiBootstrap.class);

	private static BeanManager beanManager = null;

	public BeanManager getBeanManager() {
		if (null == beanManager) {
			try {
				InitialContext initialContext = new InitialContext();
				beanManager = (BeanManager) initialContext.lookup("java:comp/env/BeanManager");
			} catch (NamingException e) {
			}
		}
		return beanManager;
	}

	public <T> T getBean(Class<T> cls) {
		BeanManager bm = getBeanManager();
		logger.info("Generate bean from {}, cause native injection doesn't work.");
		Set<Bean<?>> beans = bm.getBeans(cls, DEFAULT_AT);
		Bean<?> b = beans.iterator().next();
		final CreationalContext context = bm.createCreationalContext(b);
		return cls.cast(bm.getReference(b, b.getBeanClass(), context));
	}

	private static final Annotation DEFAULT_AT = new AnnotationLiteral<Default>() {
		private static final long serialVersionUID = 1L;
	};

}
