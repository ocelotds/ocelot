/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.topic;

import javax.inject.Inject;
import org.ocelotds.annotations.JsTopicControls;
import org.ocelotds.core.UnProxyClassServices;
import org.ocelotds.security.JsTopicMessageController;

/**
 *
 * @author hhfrancois
 */
public class JsTopicControlsTools {
	
	@Inject
	private UnProxyClassServices unProxyClassServices;

	/**
	 * get JsTopicControls from JsTopicAccessController instance
	 * @param proxy
	 * @return 
	 */
	public JsTopicControls getJsTopicControlsFromProxyClass(Class<?> proxy) {
		Class<?> realClass = unProxyClassServices.getRealClass(proxy);
		return realClass.getAnnotation(JsTopicControls.class);
	}

}
