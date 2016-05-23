/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.integration.dataservices.marshalling;

import java.util.List;
import java.util.Map;
import org.ocelotds.Constants;
import org.ocelotds.annotations.DataService;
import org.ocelotds.integration.marshallers.ClassMarshaller;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.marshalling.annotations.JsonMarshallerType;
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
	
	@JsonMarshaller(value=ClassMarshaller.class, type = JsonMarshallerType.LIST)
	public List<Class> getClasses(@JsonUnmarshaller(value=ClassMarshaller.class, type = JsonMarshallerType.LIST) List<Class> clss) {
		return clss;
	}

	@JsonMarshaller(value=ClassMarshaller.class, type = JsonMarshallerType.MAP)
	public Map<String, Class> getMapClass(@JsonUnmarshaller(value=ClassMarshaller.class, type = JsonMarshallerType.MAP) Map<String, Class> mcls) {
		return mcls;
	}
}
