/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.marshallers;

import java.util.Locale;
import org.ocelotds.Constants;
import org.ocelotds.marshalling.JsonMarshaller;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;

/**
 *
 * @author hhfrancois
 */
public class LocaleMarshaller implements JsonMarshaller<Locale> {

	/**
	 * 
	 * @param obj
	 * @return
	 * @throws JsonMarshallingException 
	 */
	@Override
	public String toJson(Locale obj) throws JsonMarshallingException {
		if(null != obj) {
			return String.format("{\"%s\":\"%s\",\"%s\":\"%s\"}", Constants.Message.COUNTRY, obj.getCountry(), Constants.Message.LANGUAGE, obj.getLanguage());
		}
		return "null";
	}

}
