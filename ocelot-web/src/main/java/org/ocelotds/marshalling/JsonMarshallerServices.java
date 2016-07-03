/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.marshalling;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.marshalling.exceptions.JsonMarshallerException;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
public class JsonMarshallerServices {
	
	@Inject
	@OcelotLogger
	Logger logger;
	
	public IJsonMarshaller getIJsonMarshallerInstance(Class<? extends IJsonMarshaller> cls) throws JsonMarshallerException {
		if(logger.isDebugEnabled()) {
			logger.debug("Try to get {} by CDI.select Unsatisfied: {}", cls.getName(), CDI.current().select(cls).isUnsatisfied());
		}
		if(CDI.current().select(cls).isUnsatisfied()) {
			throw new JsonMarshallerException(cls.getName()+" is Unsatisfied");
		}
		return CDI.current().select(cls).get();
	}
}
