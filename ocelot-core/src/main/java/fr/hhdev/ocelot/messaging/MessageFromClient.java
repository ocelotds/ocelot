/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.ocelot.messaging;

import fr.hhdev.ocelot.Constants;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Message venant du client pour appeler un services quelconque.
 * C'est le topic de Command qui identifie quel resolver sera utilisé pour acquérir l'instance du service.
 * @author hhfrancois
 */
@Getter @Setter @EqualsAndHashCode(of = {"id"})
public class MessageFromClient {

	protected String id;
	protected String dataService;
	protected String operation;
	/**
	 * parametres de la requete au format json
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

	public static MessageFromClient createFromJson(String json) {
		try (JsonReader reader = Json.createReader(new StringReader(json))) {
			JsonObject root = reader.readObject();
			MessageFromClient message = new MessageFromClient();
			message.setId(root.getString(Constants.Message.ID));
			message.setDataService(root.getString(Constants.Message.DATASERVICE));
			message.setOperation(root.getString(Constants.Message.OPERATION));
			JsonArray argArray = root.getJsonArray(Constants.Message.ARGUMENTS);
			List<String> params = new ArrayList<>();
			message.setParameters(params);
			int idx = 0;
			while(idx<argArray.size()) {
				params.add(""+argArray.get(idx++));
			} 
			return message;
		}
	}
	
	public String toJson() {
		String args = "";
		Iterator<String> iterator = parameters.iterator();
		while(iterator.hasNext()) {
			args += iterator.next();
			if(iterator.hasNext()) {
				args += ",";
			}
		}
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":[%s]}",
			Constants.Message.ID, this.getId(),
			Constants.Message.DATASERVICE, this.getDataService(),
			Constants.Message.OPERATION, this.getOperation(),
			Constants.Message.ARGUMENTS, args);
		return json;
	}
}
