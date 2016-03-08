/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.literals;

import javax.enterprise.util.AnnotationLiteral;
import org.ocelotds.marshalling.annotations.JsonMarshaller;

/**
 *
 * @author hhfrancois
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class JsonMarshallerLiteral extends AnnotationLiteral<JsonMarshaller> implements JsonMarshaller {

	private final Class<? extends org.ocelotds.marshalling.IJsonMarshaller> value;

	public JsonMarshallerLiteral(Class<? extends org.ocelotds.marshalling.IJsonMarshaller> cls) {
		value = cls;
	}

	@Override
	public Class<? extends org.ocelotds.marshalling.IJsonMarshaller> value() {
		return value;
	}
}