/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.ocelot.messaging;

import fr.hhdev.ocelot.Constants;
import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import lombok.Data;

/**
 *
 * @author hhfrancois
 */
@Data 
public class Command {

	String topic;
	String command;
	String message;

	public static Command createFromJson(String json) {
		try (JsonReader reader = Json.createReader(new StringReader(json))) {
			JsonObject root = reader.readObject();
			Command command = new Command();
			command.setTopic(root.getString(Constants.Command.TOPIC));
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
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\"%s}",
				  Constants.Command.TOPIC, this.getTopic(),
				  Constants.Command.COMMAND, this.getCommand(),
				  msg);
		return json;
	}
}
