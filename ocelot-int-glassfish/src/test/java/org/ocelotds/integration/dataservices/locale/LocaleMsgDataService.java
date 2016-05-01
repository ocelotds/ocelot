/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.integration.dataservices.locale;

import org.ocelotds.Constants;
import org.ocelotds.OcelotI18nServices;
import org.ocelotds.annotations.DataService;
import javax.inject.Inject;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.CDI)
public class LocaleMsgDataService {
	
	@Inject
	private OcelotI18nServices ocelotServices;

	public String getLocaleHello(String who) {
		return ocelotServices.getLocalizedMessage("test", "HELLOGUY", new Object[]{who});
	}
}
