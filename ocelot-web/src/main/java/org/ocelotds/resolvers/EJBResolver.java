/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.resolvers;

import org.ocelotds.spi.DataServiceException;
import org.ocelotds.spi.DataServiceResolver;
import org.ocelotds.spi.IDataServiceResolver;
import org.ocelotds.Constants;
import org.ocelotds.annotations.DataService;
import org.ocelotds.spi.Scope;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolver of EJB
 *
 * @author hhfrancois
 */
@DataServiceResolver(Constants.Resolver.EJB)
public class EJBResolver implements IDataServiceResolver {

	private static final Logger logger = LoggerFactory.getLogger(EJBResolver.class);

	private static final Map<String, String> jndiMap = new HashMap<>();

	private String jndiPath = "";

	public String getJndiPath() {
		return jndiPath;
	}
	
	@Inject
	private InitialContext initialContext;

	@PostConstruct
	void initJNDIPath() {
		logger.debug("Initializing context ...");
		try {
			jndiPath = JndiConstant.PREFIX + (String) initialContext.lookup(JndiConstant.APP_NAME);
		} catch (NamingException ex) {
			logger.error("InitialContext initialisation Failed ", ex);
		}
	}

	@Override
	public <T> T resolveDataService(Class<T> clazz) throws DataServiceException {
		return clazz.cast(resolveDataService(clazz.getName()));
	}

	private Object resolveDataService(String name) throws DataServiceException {
		Object obj;
		if (jndiMap.containsKey(name)) {
			String jndi = jndiMap.get(name);
			try {
				obj = initialContext.lookup(jndi);
			} catch (NamingException ex) {
				throw new DataServiceException(name, ex);
			}
		} else {
			obj = findEJB(jndiPath, name);
		}
		if (null == obj) {
			throw new DataServiceException(name);
		}
		return obj;
	}

	private Object findEJB(String jndi, String name) {
		Object result;
		try {
			NamingEnumeration<Binding> list = initialContext.listBindings(jndi);
			while (list != null && list.hasMore()) {
				try {
					Binding item = list.next();
					String itemName = item.getName();
					if (itemName.endsWith(name)) {
						try {
							result = initialContext.lookup(jndi + JndiConstant.PATH_SEPARATOR + itemName);
							if (result != null) {
								jndiMap.put(name, jndi + JndiConstant.PATH_SEPARATOR + itemName);
							}
							return result;
						} catch (NamingException e) {
							logger.debug("{} list.next() {} : {}", new Object[]{e.getClass().getSimpleName(), name, e.getMessage()});
						}
					}
					result = findEJB(jndi + JndiConstant.PATH_SEPARATOR + itemName, name);
					if (result != null) {
						return result;
					}
				} catch (NamingException e) {
					logger.debug("{} list.next() {} : {}", new Object[]{e.getClass().getSimpleName(), name, e.getMessage()});
				}
			}
		} catch (Throwable e) {
		}
		return null;
	}

	@Override
	public Scope getScope(Class clazz) {
		for (Annotation anno : clazz.getAnnotations()) {
			if (!anno.annotationType().equals(DataService.class)) {
				String annoName = anno.annotationType().getName();
				switch (annoName) {
					case "javax.ejb.Stateful":
						return Scope.SESSION;
					default:
				}
			}
		}
		return Scope.MANAGED;
	}

	private interface JndiConstant {

		String PREFIX = "java:global/";
		String APP_NAME = "java:app/AppName";
		String PATH_SEPARATOR = "/";
	}
}
