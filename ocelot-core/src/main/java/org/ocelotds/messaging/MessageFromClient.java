/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.messaging;

import org.ocelotds.Constants;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message from client for call any service. the topic of command identify the resolver for get instance of service
 *
 * @author hhfrancois
 */
public class MessageFromClient {

	private static final Logger logger = LoggerFactory.getLogger(MessageFromClient.class);
	protected String id;
	protected String dataService;
	protected String operation;
	/**
	 * parameters json format
	 */
	protected List<String> parameters = new ArrayList<>();

	public String getId() {
		return id;
	}

	public void setId(String messageId) {
		this.id = messageId;
	}

	public String getDataService() {
		return dataService;
	}

	public void setDataService(String dataService) {
		this.dataService = dataService;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 79 * hash + Objects.hashCode(this.id);
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
		final MessageFromClient other = (MessageFromClient) obj;
		return Objects.equals(this.id, other.id);
	}

	public static MessageFromClient createFromJson(String json) {
		MessageFromClient message = new MessageFromClient();
		try (JsonReader reader = Json.createReader(new StringReader(json))) {
			JsonObject root = reader.readObject();
			message.setId(root.getString(Constants.Message.ID));
			message.setDataService(root.getString(Constants.Message.DATASERVICE));
			message.setOperation(root.getString(Constants.Message.OPERATION));
			logger.debug("Get arguments from message '{}'", json);
			message.setParameters(getArgumentsFromMessage(root.getJsonArray(Constants.Message.ARGUMENTS)));
		}
		return message;
	}

	static List<String> getArgumentsFromMessage(JsonArray argArray) {
		List<String> params = new ArrayList<>();
		for (JsonValue arg : argArray) {
			if (logger.isDebugEnabled()) {
				logger.debug("Get argument Type : '{}'. Value : '{}'", arg.getValueType().name(), arg);
			}
			params.add(arg.toString());
		}
		return params;
	}
}
