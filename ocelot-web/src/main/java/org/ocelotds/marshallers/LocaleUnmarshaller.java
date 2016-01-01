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
import org.ocelotds.marshalling.JsonUnmarshaller;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;

/**
 * For change to runtime an object received from clientto an other java type<br>
 * Use @JsonUnmarshaller annotation on argument to specify the unmarshaller that implements JsonUnmarshaller
 * @author hhfrancois
 */
public class LocaleUnmarshaller implements JsonUnmarshaller<Locale> {

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
			} catch(Throwable t) {}
		}
		return locale;
	}
	
}
