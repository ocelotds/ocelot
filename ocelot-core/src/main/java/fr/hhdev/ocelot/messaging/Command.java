/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot.messaging;

import fr.hhdev.ocelot.Constants;
import java.io.StringReader;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object command for communication between Client to Server
 * Command types : SUBSCRIBE, UNSUBSCRIBE, CALL
 * @author hhfrancois
 */
public class Command {
	private final static Logger logger = LoggerFactory.getLogger(Command.class);

	String command;
	String message;

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * 
	 * @param json : {"cmd":"call","msg":{"id":"7b65b992407acdada143e66e77f2dfe8_7b65b992407acdada143e66e77f2dfe9","ds":"demo.TestEJBService","op":"getMessage","argNames":["i"],"args":[1]}}
	 * @return 
	 */
	public static Command createFromJson(String json) {
		logger.debug("Convert json '{}' to object", json);
		try (JsonReader reader = Json.createReader(new StringReader(json))) {
			JsonObject root = reader.readObject();
			Command command = new Command();
			command.setCommand(root.getString(Constants.Command.COMMAND));
			if(root.containsKey(Constants.Command.MESSAGE)) {
				JsonValue msg = root.get(Constants.Command.MESSAGE);
				command.setMessage(""+msg);
			}
			return command;
		}
	}

	public String toJson() {
		String msg = "";
		if(null != this.getMessage()) {
			String msgFormat = ",\"%s\":%s";
			msg = String.format(msgFormat, Constants.Command.MESSAGE, this.getMessage());
		}
		String json = String.format("{\"%s\":\"%s\"%s}", Constants.Command.COMMAND, this.getCommand(), msg);
		return json;
	}
	
	@Override
	public String toString() {
		return toJson();
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 11 * hash + Objects.hashCode(this.command);
		hash = 11 * hash + Objects.hashCode(this.message);
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
		final Command other = (Command) obj;
		return Objects.equals(this.command, other.command) && Objects.equals(this.message, other.message);
	}
	
	
}
