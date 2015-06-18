/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.hhdev.ocelot.Constants;
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
	 * Id of request, compute from hash of packageName, classname, methodName, arguments
	 */
	protected String id;
	/**
	 * The result of request
	 */
	protected Object result = null;
	/**
	 * The request failed, fault
	 */
	protected Fault fault = null;
	/**
	 * validity limit
	 */
	protected long deadline = 0L;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Fault getFault() {
		return fault;
	}

	public void setFault(Fault fault) {
		this.fault = fault;
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
			message.setDeadline(root.getInt(Constants.Message.DEADLINE));
			if (root.containsKey(Constants.Message.RESULT)) {
				JsonValue result = root.get(Constants.Message.RESULT);
				message.setResult("" + result);
			} else {
				if (root.containsKey(Constants.Message.FAULT)) {
					JsonObject faultJs = root.getJsonObject(Constants.Message.FAULT);
					try {
						Fault f = Fault.createFromJson(faultJs.toString());
						message.setFault(f);
					} catch (IOException ex) {
					}
				}
			}
			return message;
		}
	}

	public String toJson() {
		String res = "";
		if (null != this.getResult()) {
			String resultFormat = ",\"%s\":%s";
			ObjectMapper mapper = new ObjectMapper();
			String jsonResult;
			try {
				jsonResult = mapper.writeValueAsString(this.getResult());
				res = String.format(resultFormat, Constants.Message.RESULT, jsonResult);
			} catch (JsonProcessingException ex) {
				Fault f = new Fault(ex, 0);
				try {
					res = String.format(resultFormat, Constants.Message.FAULT, f.toJson());
				} catch (IOException ex1) {
				}
			}
		}
		if (null != this.getFault()) {
			String faultFormat = ",\"%s\":%s";
			try {
				res = String.format(faultFormat, Constants.Message.FAULT, this.getFault().toJson());
			} catch (IOException ex) {
			}
		}
		String json = String.format("{\"%s\":\"%s\",\"%s\":%s%s}",
				  Constants.Message.ID, this.getId(), Constants.Message.DEADLINE, this.getDeadline(),
				  res);
		return json;
	}

	@Override
	public String toString() {
		return toJson();
	}
}
