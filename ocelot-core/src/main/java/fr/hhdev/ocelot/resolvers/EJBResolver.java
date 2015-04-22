/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.ocelot.resolvers;

import fr.hhdev.ocelot.Constants;
import java.util.HashMap;
import java.util.Map;
import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author hhfrancois
 */
@Slf4j
@DataServiceResolverId(Constants.Resolver.EJB)
public class EJBResolver implements DataServiceResolver {

	private static final Map<String, String> jndiMap = new HashMap<>();
	private String jndiPath = "";
	private InitialContext initialContext = null;


	public void EJBResolver() {
		getInitialContext();		
	}
	@Override
	public Object resolveDataService(String name) throws DataServiceException {
		Object obj;
		InitialContext ic = getInitialContext();
		if(jndiMap.containsKey(name)) {
			String jndi = jndiMap.get(name);
			try {
				obj = ic.lookup(jndi);
			} catch (NamingException ex) {
				throw new DataServiceException(name, ex);
			}
		} else {
			obj = findEJB(jndiPath, name);
		}
		if(null == obj){
			throw new DataServiceException(name);
		}
		return obj;
	}

	private Object findEJB(String jndi, String name) {
		Object result;
		InitialContext ic = getInitialContext();
		try {
			NamingEnumeration<Binding> list = ic.listBindings(jndi);
			while (list != null && list.hasMore()) {
				try {
					Binding item = list.next();
					String itemName = item.getName();
					if (itemName.endsWith(name)) {
						try {
							result =  ic.lookup(jndi + JndiConstant.PATH_SEPARATOR + itemName);
							if(result!=null) {
								jndiMap.put(name, jndi + JndiConstant.PATH_SEPARATOR + itemName);
							}
							return result;
						} catch (NamingException e) {
							logger.debug("{} list.next() {} : {}", new Object[]{e.getClass().getSimpleName(), name, e.getMessage()});
						}
					}
					result = findEJB(jndi + JndiConstant.PATH_SEPARATOR + itemName, name);
					if(result!=null) {
						return result;
					}
				} catch (NamingException e) {
					logger.debug("{} list.next() {} : {}", new Object[]{e.getClass().getSimpleName(), name, e.getMessage()});
				}
			}
		} catch (Throwable e) {
			logger.debug("{} Context {} invalide : {}", new Object[]{e.getClass().getSimpleName(), name, e.getMessage()});
		}
		return null;
	}

	private interface JndiConstant {
		String PREFIX = "java:global/";
		String APP_NAME = "java:app/AppName";
		String PATH_SEPARATOR = "/";
	}
	
	private InitialContext getInitialContext() {
		try {
			if (null == initialContext) {
				logger.info("Initializing context ...");
				initialContext = new InitialContext();
				if (jndiPath.isEmpty()) {
					jndiPath += JndiConstant.PREFIX + (String) initialContext.lookup(JndiConstant.APP_NAME);
				}
			}
		} catch (NamingException ex) {
			logger.error("Echec d'initialisation de l'initialContext", ex);
		}
		return initialContext;
	}
}
