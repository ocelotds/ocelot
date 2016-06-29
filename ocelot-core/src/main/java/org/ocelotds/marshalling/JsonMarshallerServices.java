/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.marshalling;

import org.ocelotds.marshalling.exceptions.JsonMarshallerException;
import javax.enterprise.inject.spi.CDI;

/**
 *
 * @author hhfrancois
 */
public class JsonMarshallerServices {
	
	public IJsonMarshaller getIJsonMarshallerInstance(Class<? extends IJsonMarshaller> cls) throws JsonMarshallerException {
		return CDI.current().select(cls).get();
	}
}
