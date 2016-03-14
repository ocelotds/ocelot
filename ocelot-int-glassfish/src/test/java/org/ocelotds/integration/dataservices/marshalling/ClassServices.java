/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.integration.dataservices.marshalling;

import java.util.List;
import org.ocelotds.Constants;
import org.ocelotds.annotations.DataService;
import org.ocelotds.marshallers.ClassMarshaller;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.marshalling.annotations.JsonUnmarshaller;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.CDI)
public class ClassServices {

	@JsonMarshaller(ClassMarshaller.class)
	public Class getCls(@JsonUnmarshaller(ClassMarshaller.class) Class cls) {
		return cls;
	}
	
	@JsonMarshaller(value=ClassMarshaller.class, iterable = true)
	public List<Class> getClasses(@JsonUnmarshaller(value=ClassMarshaller.class, iterable = true) List<Class> clss) {
		return clss;
	}
}
