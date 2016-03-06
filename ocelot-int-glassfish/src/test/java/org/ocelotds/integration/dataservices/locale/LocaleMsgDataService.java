/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
