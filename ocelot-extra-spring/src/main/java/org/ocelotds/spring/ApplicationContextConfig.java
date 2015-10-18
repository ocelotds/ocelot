/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.spring;

import java.security.Principal;
import org.ocelotds.Constants;
import org.ocelotds.context.ThreadLocalContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author hhfrancois
 */
//@Configuration
public class ApplicationContextConfig {

	@Bean
	@Scope(value = "prototype")
	public Principal principal() {
		Principal p = (Principal) ThreadLocalContextHolder.get(Constants.PRINCIPAL);
		return p;
	}
}
