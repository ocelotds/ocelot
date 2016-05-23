/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core;

/**
 *
 * @author hhfrancois
 */
public class UnProxyClassServices {
	/**
	 * Get class without proxy CDI based on $ separator
	 *
	 * @param proxy
	 * @return
	 */
	public Class<?> getRealClass(Class proxy) {
		try {
			return Class.forName(getRealClassname(proxy.getName()));
		} catch (ClassNotFoundException ex) {
		}
		return proxy;
	}
	
	/**
	 * Get class without proxy CDI based on $ separator
	 *
	 * @param proxyname
	 * @return
	 * @throws java.lang.ClassNotFoundException
	 */
	String getRealClassname(String proxyname) throws ClassNotFoundException {
		int index = proxyname.indexOf('$');
		if (index != -1) {
			return proxyname.substring(0, index);
		} else {
			throw new ClassNotFoundException();
		}
	}
}
