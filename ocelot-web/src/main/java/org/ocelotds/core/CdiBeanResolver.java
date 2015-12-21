/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core;

import java.lang.annotation.Annotation;
import java.util.Set;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.ocelotds.Constants;

/**
 *
 * @author hhfrancois
 */
public class CdiBeanResolver {

	private static volatile BeanManager beanManager = null;

	/**
	 * Becareful, don't work if static
	 */
	private InitialContext initialContext = null;

	public <T> T getBean(Class<T> cls) {
		BeanManager bm = getBeanManager();
		Set<Bean<?>> beans = bm.getBeans(cls, DEFAULT_AT);
		Bean<?> b = beans.iterator().next();
		final CreationalContext context = bm.createCreationalContext(b);
		return cls.cast(bm.getReference(b, b.getBeanClass(), context));
	}

	BeanManager getBeanManager() {
		try {
			InitialContext ic = getInitialContext();
			if (null == beanManager) {
				try {
					synchronized(this) {
                    if (beanManager == null) {
								beanManager = (BeanManager) ic.lookup(Constants.BeanManager.BEANMANAGER_JEE); // standart implementation
						  }
					}
				} catch (NamingException e) {
					synchronized(this) {
                    if (beanManager == null) {
								beanManager = (BeanManager) ic.lookup(Constants.BeanManager.BEANMANAGER_ALT); // Tomcat implementation
						  }
					}
				}
			}
		} catch (NamingException e) {
		}
		return beanManager;
	}

	InitialContext getInitialContext() throws NamingException {
		if (null == initialContext) {
			initialContext = new InitialContext();
		}
		return initialContext;
	}

	private static final Annotation DEFAULT_AT = new AnnotationLiteral<Default>() {
		private static final long serialVersionUID = 1L;
	};
	
	static void raz() {
		beanManager = null;
	}

}
