/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.objects;

import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;

/**
 *
 * @author hhfrancois
 */
public class BadMarshaller3 implements org.ocelotds.marshalling.IJsonMarshaller {

	public BadMarshaller3() {
	}

	@Override
	public String toJson(Object obj) throws JsonMarshallingException {
		throw new JsonMarshallingException("");
	}

	@Override
	public Object toJava(String json) throws JsonUnmarshallingException {
		throw new JsonUnmarshallingException("");
	}
}
