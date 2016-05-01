/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.marshallers;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.ocelotds.marshalling.IJsonMarshaller;

/**
 *
 * @author hhfrancois
 */
public class JsonMarshallerServices {
	@Inject 
	@Any
	Instance<IJsonMarshaller> iJsonMarshallers;

	public IJsonMarshaller getIJsonMarshallerInstance(Class<? extends IJsonMarshaller> cls) throws JsonMarshallerException {
		if(iJsonMarshallers.select(cls).isUnsatisfied()) {
			throw new JsonMarshallerException(cls.getSimpleName()+" is Unsatisfied");
		}
		return iJsonMarshallers.select(cls).get();
	}
}
