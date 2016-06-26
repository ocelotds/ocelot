/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.services;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import org.ocelotds.marshalling.exceptions.JsonMarshallerException;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;

/**
 *
 * @author hhfrancois
 */
public interface IArgumentConvertor {
	Object convertJsonToJava(String jsonArg, Type paramType, Annotation[] parameterAnnotations) throws JsonUnmarshallingException, JsonMarshallerException;
}
