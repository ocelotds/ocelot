/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
