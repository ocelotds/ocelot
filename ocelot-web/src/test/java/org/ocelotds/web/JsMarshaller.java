/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import org.ocelotds.marshalling.exceptions.JsonMarshallingException;

/**
 *
 * @author hhfrancois
 */
public class JsMarshaller implements org.ocelotds.marshalling.JsonMarshaller<String> {

	@Override
	public String toJson(String obj) throws JsonMarshallingException {
		return "\""+obj.toLowerCase()+"\"";
	}
}
