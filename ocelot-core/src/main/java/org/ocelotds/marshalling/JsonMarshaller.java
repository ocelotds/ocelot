/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.marshalling;

import org.ocelotds.marshalling.exceptions.JsonMarshallingException;

/**
 * For change to runtime an object expose by a service to an other json type<br>
 * Use @JsonMarshaller annotation on method to specify the marshaller that implements JsonMarshaller
 * @author hhfrancois
 * @param <T>
 */
public interface JsonMarshaller<T> {
	String toJson(T obj) throws JsonMarshallingException;
}
