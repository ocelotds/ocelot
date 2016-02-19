/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import java.util.Locale;
import org.ocelotds.Constants;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;

/**
 *
 * @author hhfrancois
 */
public class JsMarshaller implements org.ocelotds.marshalling.IJsonMarshaller<String> {

	@Override
	public String toJson(String obj) throws JsonMarshallingException {
		return Constants.QUOTE+obj.toLowerCase(Locale.US)+Constants.QUOTE;
	}

	@Override
	public String toJava(String json) throws JsonUnmarshallingException {
		return null;
	}
}
