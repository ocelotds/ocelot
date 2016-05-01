/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.integration.marshallers;

import org.ocelotds.Constants;
import org.ocelotds.marshalling.IJsonMarshaller;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;

/**
 *
 * @author hhfrancois
 */
public class ClassMarshaller implements IJsonMarshaller<Class>{

	@Override
	public String toJson(Class obj) throws JsonMarshallingException {
		return Constants.QUOTE+obj.getName()+Constants.QUOTE;
	}

	@Override
	public Class toJava(String json) throws JsonUnmarshallingException {
		String clsname = json.replaceAll(Constants.QUOTE, "");
		try {
			return Class.forName(clsname);
		} catch (ClassNotFoundException ex) {
			throw new JsonUnmarshallingException(json);
		}
	}
	
}
