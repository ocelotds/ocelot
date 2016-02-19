/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.services;

import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;

/**
 *
 * @author hhfrancois
 */
public class ArgumentServices {

	/**
	 * 
	 * @param jm
	 * @param result
	 * @return
	 * @throws JsonMarshallingException
	 * @throws InstantiationException
	 * @throws IllegalAccessException 
	 */
	public String getJsonResultFromSpecificMarshaller(JsonMarshaller jm, Object result) throws JsonMarshallingException, InstantiationException, IllegalAccessException {
		Class<? extends org.ocelotds.marshalling.IJsonMarshaller> marshallerCls = jm.value();
		org.ocelotds.marshalling.IJsonMarshaller marshaller = marshallerCls.newInstance();
		return marshaller.toJson(result);
	}
}
