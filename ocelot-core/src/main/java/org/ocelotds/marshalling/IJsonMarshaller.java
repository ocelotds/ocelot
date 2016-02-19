/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.marshalling;

import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;

/**
 * For change to runtime an object expose by a service to an other json type<br>
 Use @IJsonMarshaller annotation on method to specify the marshaller that implements IJsonMarshaller
 * @author hhfrancois
 * @param <T>
 */
public interface IJsonMarshaller<T> {
	String toJson(T obj) throws JsonMarshallingException;
	T toJava(String json) throws JsonUnmarshallingException;
}
