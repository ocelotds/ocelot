/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.marshallers;

import java.io.StringReader;
import java.util.Locale;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.ocelotds.Constants;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;
import org.ocelotds.marshalling.IJsonMarshaller;

/**
 *
 * @author hhfrancois
 */
public class LocaleMarshaller implements IJsonMarshaller<Locale> {

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

	/**
	 * 
	 * @param json
	 * @return
	 * @throws JsonUnmarshallingException 
	 */
	@Override
	public Locale toJava(String json) throws JsonUnmarshallingException {
		Locale locale = null;
		if(null != json) {
			try (JsonReader reader = Json.createReader(new StringReader(json))) {
				JsonObject root = reader.readObject();
				locale = new Locale(root.getString(Constants.Message.LANGUAGE), root.getString(Constants.Message.COUNTRY));
			} catch(Throwable t) {
				throw new JsonUnmarshallingException(json);
			}
		}
		return locale;
	}

}
