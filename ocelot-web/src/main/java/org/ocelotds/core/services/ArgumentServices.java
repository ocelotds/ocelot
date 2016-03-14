/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.services;

import java.io.StringReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonValue;
import org.ocelotds.marshalling.IJsonMarshaller;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;

/**
 *
 * @author hhfrancois
 */
public class ArgumentServices {

	/**
	 *
	 * @param jm
	 * @param result
	 * @return
	 * @throws JsonMarshallingException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public String getJsonResultFromSpecificMarshaller(JsonMarshaller jm, Object result) throws JsonMarshallingException, InstantiationException, IllegalAccessException {
		Class<? extends IJsonMarshaller> marshallerCls = jm.value();
		IJsonMarshaller marshaller = marshallerCls.newInstance();
		if (jm.iterable()) {
			return getJsonResultFromSpecificMarshallerIterable((Iterable) result, marshaller);
		}
		return marshaller.toJson(result);
	}

	String getJsonResultFromSpecificMarshallerIterable(Iterable list, IJsonMarshaller marshaller) throws JsonMarshallingException {
		StringBuilder json = new StringBuilder();
		json.append("[");
		boolean first = true;
		for (Object object : list) {
			if (!first) {
				json.append(",");
			}
			json.append(marshaller.toJson(object));
			first = false;
		}
		json.append("]");
		return json.toString();
	}

	
	List getJavaResultFromSpecificUnmarshallerIterable(String json, IJsonMarshaller marshaller) throws JsonUnmarshallingException {
		try {
			List result = new ArrayList<>();
			JsonReader createReader = Json.createReader(new StringReader(json));
			JsonArray readArray = createReader.readArray();
			for (JsonValue jsonValue : readArray) {
				result.add(marshaller.toJava(jsonValue.toString()));
			}
			return result;
		} catch(Throwable t) {
			throw new JsonUnmarshallingException(t.getMessage());
		}
	}

	public void checkType(Object object, Type paramType) throws JsonUnmarshallingException {
		Class cls;
		if (ParameterizedType.class.isInstance(paramType)) {
			cls = (Class) ((ParameterizedType) paramType).getRawType();
		} else if (Class.class.isInstance(paramType)) {
			cls = (Class) paramType;
		} else { // GenericArrayType, TypeVariable<D>, WildcardType
			throw new JsonUnmarshallingException(object + " is not instance of " + paramType);
		}
		checkClass(object, cls);
	}

	void checkClass(Object object, Class cls) throws JsonUnmarshallingException {
		if (!cls.isInstance(object)) {
			throw new JsonUnmarshallingException(object + " is not instance of " + cls);
		}
	}
}
