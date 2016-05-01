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
public class BadMarshaller1 implements org.ocelotds.marshalling.IJsonMarshaller {

	public BadMarshaller1(String s) {
	}

	@Override
	public String toJson(Object obj) throws JsonMarshallingException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Object toJava(String json) throws JsonUnmarshallingException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
