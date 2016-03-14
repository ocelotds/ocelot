/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.marshallers;

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
