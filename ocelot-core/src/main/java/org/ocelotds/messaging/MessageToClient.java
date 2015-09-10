/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ocelotds.Constants;
import java.io.IOException;
import java.io.StringReader;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

/**
 * Message to Client, for response after message from client. Server send this response asynchronous
 *
 * @author hhfrancois
 */
public class MessageToClient {
	private static final long serialVersionUID = -834697863344344124L;

	/**
	 * Type of message
	 */
	protected MessageType type = null;

	/**
	 * Id of request, compute from hash of packageName, classname, methodName, arguments
	 */
	protected String id;
	/**
	 * The result of request
	 */
	protected Object response = null;
	
	/**
	 * The result of request in json format
	 */
	protected String json = null;

	/**
	 * validity limit
	 */
	protected long deadline = 0L;

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Object getResponse() {
		return response;
	}

	private void setResponse(Object response, MessageType type) {
		this.type = type;
		this.response = response;
	}
	
	public void setResult(Object response) {
		setResponse(response, MessageType.RESULT);
	}

	public void setFault(Object response) {
		setResponse(response, MessageType.FAULT);
	}

	public void setResponse(Object response) {
		this.response = response;
	}
	
	public long getDeadline() {
		return deadline;
	}

	public void setDeadline(long deadline) {
		this.deadline = deadline;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 83 * hash + Objects.hashCode(this.id);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MessageToClient other = (MessageToClient) obj;
		return Objects.equals(this.id, other.id);
	}

	/**
	 * Becareful result/fault are not unmarshalled
	 *
	 * @param json
	 * @return
	 */
	public static MessageToClient createFromJson(String json) {
		try (JsonReader reader = Json.createReader(new StringReader(json))) {
			JsonObject root = reader.readObject();
			MessageToClient message = new MessageToClient();
			message.setId(root.getString(Constants.Message.ID));
			message.setType(MessageType.valueOf(root.getString(Constants.Message.TYPE)));
			message.setDeadline(root.getInt(Constants.Message.DEADLINE));
			if(MessageType.FAULT.equals(message.getType())) {
				JsonObject faultJs = root.getJsonObject(Constants.Message.RESPONSE);
				try {
					Fault f = Fault.createFromJson(faultJs.toString());
					message.setFault(f);
				} catch (IOException ex) {
				}
			} else {
				JsonValue result = root.get(Constants.Message.RESPONSE);
				message.setResponse("" + result, message.getType());
			}
			return message;
		}
	}

	public String toJson() {
		String res;
		String resultFormat = ",\"%s\":%s";
		ObjectMapper mapper = getObjectMapper();
		String jsonResponse;
		try {
			jsonResponse = this.json;
			if(null==this.json) {
				jsonResponse = mapper.writeValueAsString(this.getResponse());
			}
			res = String.format(resultFormat, Constants.Message.RESPONSE, jsonResponse);
		} catch (JsonProcessingException ex) {
			Fault f = new Fault(ex, 0);
			try {
				jsonResponse = mapper.writeValueAsString(f);
				res = String.format(resultFormat, Constants.Message.RESPONSE, jsonResponse);
			} catch (JsonProcessingException ex1) {
				res = String.format(resultFormat, Constants.Message.RESPONSE, "");
			}
		}
		return String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s%s}",
				  Constants.Message.TYPE, this.getType(), Constants.Message.ID, this.getId(), Constants.Message.DEADLINE, this.getDeadline(), res);
	}

	ObjectMapper getObjectMapper() {
		return new ObjectMapper();
	}
	
	@Override
	public String toString() {
		return toJson();
	}
}
