/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.marshalling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.StringReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;

/**
 *
 * @author hhfrancois
 */
public class ArgumentServices {
	
	@Inject
	JsonMarshallerServices jsonMarshallerServices;
	
	@Inject
	ObjectMapper objectMapper;

	public List<String> getJsonParameters(Object[] parameters) throws JsonMarshallingException, JsonMarshallerException, JsonProcessingException {
		List<String> jsonArgs = new ArrayList<>();
		for (Object arg : parameters) {
			if(arg.getClass().isAnnotationPresent(JsonMarshaller.class)) {
				jsonArgs.add(getJsonResultFromSpecificMarshaller(arg.getClass().getAnnotation(JsonMarshaller.class), arg));
			} else {
				jsonArgs.add(objectMapper.writeValueAsString(arg));
			}
		}
		return jsonArgs;
	}
	
	/**
	 *
	 * @param jm
	 * @param result
	 * @return
	 * @throws JsonMarshallingException
	 * @throws org.ocelotds.marshalling.JsonMarshallerException
	 */
	public String getJsonResultFromSpecificMarshaller(JsonMarshaller jm, Object result) throws JsonMarshallingException, JsonMarshallerException {
		IJsonMarshaller marshaller = jsonMarshallerServices.getIJsonMarshallerInstance(jm.value());
		String res;
		switch (jm.type()) {
			case LIST:
				res = getJsonResultFromSpecificMarshallerIterable((Iterable) result, marshaller);
				break;
			case MAP:
				res = getJsonResultFromSpecificMarshallerMap((Map) result, marshaller);
				break;
			default:
				res = marshaller.toJson(result);
		}
		return res;
	}

	public String getJsonResultFromSpecificMarshallerIterable(Iterable list, IJsonMarshaller marshaller) throws JsonMarshallingException {
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

	String getJsonResultFromSpecificMarshallerMap(Map map, IJsonMarshaller marshaller) throws JsonMarshallingException {
		StringBuilder json = new StringBuilder();
		json.append("{");
		boolean first = true;
		for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
			if (!first) {
				json.append(",");
			}
			json.append("\"").append(entry.getKey()).append("\"").append(":");
			json.append(marshaller.toJson(entry.getValue()));
			first = false;
		}
		json.append("}");
		return json.toString();
	}
	
	public List getJavaResultFromSpecificUnmarshallerIterable(String json, IJsonMarshaller marshaller) throws JsonUnmarshallingException {
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

	public Map getJavaResultFromSpecificUnmarshallerMap(String json, IJsonMarshaller marshaller) throws JsonUnmarshallingException {
		try {
			Map<String, Object> result = new HashMap();
			JsonReader createReader = Json.createReader(new StringReader(json));
			JsonObject readObject = createReader.readObject();
			for (Map.Entry<String, JsonValue> entry : readObject.entrySet()) {
				String key = entry.getKey();
				JsonValue jsonValue = entry.getValue();
				result.put(key, marshaller.toJava(jsonValue.toString()));
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
