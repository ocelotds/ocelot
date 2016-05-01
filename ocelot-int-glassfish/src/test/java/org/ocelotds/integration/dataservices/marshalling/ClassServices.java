/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.integration.dataservices.marshalling;

import java.util.List;
import org.ocelotds.Constants;
import org.ocelotds.annotations.DataService;
import org.ocelotds.integration.marshallers.ClassMarshaller;
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
