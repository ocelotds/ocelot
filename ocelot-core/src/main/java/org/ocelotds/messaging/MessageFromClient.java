/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.messaging;

import org.ocelotds.Constants;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
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
	/**
	 * parameter names
	 */
	protected List<String> parameterNames = new ArrayList<>();

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

	public List<String> getParameterNames() {
		return parameterNames;
	}

	public void setParameterNames(List<String> parameterNames) {
		this.parameterNames = parameterNames;
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
			JsonArray argArray = root.getJsonArray(Constants.Message.ARGUMENTS);
			List<String> params = new ArrayList<>();
			message.setParameters(params);
			int idx = 0;
			logger.debug("Get arguments and argumentNames from message '{}'", json);
			if (argArray.isEmpty()) {
				logger.debug("No arguments from message");
			} else {
				while (idx < argArray.size()) {
					JsonValue arg = argArray.get(idx++);
					logger.debug("Get argument Type : '{}'. Value : '{}'", arg.getValueType().name(), arg.toString());
					params.add(arg.toString());
				}
			}
			argArray = root.getJsonArray(Constants.Message.ARGUMENTNAMES);
			params = new ArrayList<>();
			message.setParameterNames(params);
			idx = 0;
			if (argArray.isEmpty()) {
				logger.debug("No argumentName from message");
			} else {
				while (idx < argArray.size()) {
					JsonString arg = argArray.getJsonString(idx++);
					logger.debug("Get argumentName : '{}'.", arg.toString());
					params.add(arg.toString());
				}
			}
		}
		return message;
	}

	public String toJson() {
//		StringBuilder args = new StringBuilder("");
//		Iterator<String> iterator = parameters.iterator();
//		while (iterator.hasNext()) {
//			args.append(iterator.next());
//			if (iterator.hasNext()) {
//				args.append(args);
//			}
//		}
//		StringBuilder argnames = new StringBuilder("");
//		iterator = parameterNames.iterator();
//		while (iterator.hasNext()) {
//			argnames.append(iterator.next());
//			if (iterator.hasNext()) {
//				argnames.append(",");
//			}
//		}
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s,\"%s\":%s}",
				  Constants.Message.ID, this.getId(),
				  Constants.Message.DATASERVICE, this.getDataService(),
				  Constants.Message.OPERATION, this.getOperation(),
				  Constants.Message.ARGUMENTNAMES, Arrays.toString(parameterNames.toArray(new String[parameterNames.size()])),
				  Constants.Message.ARGUMENTS, Arrays.toString(parameters.toArray(new String[parameters.size()])));
		return json;
	}

	@Override
	public String toString() {
		return toJson();
	}
}
