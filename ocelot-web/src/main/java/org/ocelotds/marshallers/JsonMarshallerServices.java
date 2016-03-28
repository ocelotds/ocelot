/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.marshallers;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.ocelotds.marshalling.IJsonMarshaller;

/**
 *
 * @author hhfrancois
 */
public class JsonMarshallerServices {
	@Inject 
	@Any
	Instance<IJsonMarshaller> iJsonMarshallers;

	public IJsonMarshaller getIJsonMarshallerInstance(Class<? extends IJsonMarshaller> cls) throws JsonMarshallerException {
		if(iJsonMarshallers.select(cls).isUnsatisfied()) {
			throw new JsonMarshallerException(cls.getSimpleName()+" is Unsatesfed");
		}
		return iJsonMarshallers.select(cls).get();
	}
}
