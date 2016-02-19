/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.marshallers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.inject.Inject;
import org.ocelotds.marshalling.IJsonMarshaller;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;

/**
 *
 * @author hhfrancois
 */
public class TemplateMarshaller implements IJsonMarshaller<Object> {
	
	@Inject
	ObjectMapper objectMapper;

	@Override
	public String toJson(Object obj) throws JsonMarshallingException {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException ex) {
			throw new JsonMarshallingException(ex.getMessage());
		}
	}

	@Override
	public Object toJava(String json) throws JsonUnmarshallingException {
		return null;
	}
	
}
