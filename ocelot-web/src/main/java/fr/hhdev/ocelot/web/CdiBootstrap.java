/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.web;

import java.lang.annotation.Annotation;
import java.util.Set;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author hhfrancois
 */
public abstract class CdiBootstrap {
	private static BeanManager beanManager = null;

	public CdiBootstrap() {
		try {
			InitialContext initialContext = new InitialContext();
			beanManager = (BeanManager) initialContext.lookup("java:comp/env/BeanManager");
		} catch (NamingException e) {
		}
	}
	
	public <T> T getBean(Class<T> cls) {
		Set<Bean<?>> beans = beanManager.getBeans(cls, DEFAULT_AT);
		Bean<?> b = beans.iterator().next();
		final CreationalContext context = beanManager.createCreationalContext(b);
		return cls.cast(beanManager.getReference(b, b.getBeanClass(), context));
	}

	private static final Annotation DEFAULT_AT = new AnnotationLiteral<Default>() {
		private static final long serialVersionUID = 1L;
	};
	
}
