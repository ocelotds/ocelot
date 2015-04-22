/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.ocelot.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.hhdev.ocelot.Constants;
import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Message en réponse à une solicitation cliente Le client émét une MessageFromClient, le serveur lui répond de facon asynchrone par un MessageToClient
 *
 * @author hhfrancois
 */
@Getter @Setter @EqualsAndHashCode(of = {"id"})
public class MessageToClient {

	private static final long serialVersionUID = -834697863344344124L;

	/**
	 * L'identifiant du message, cela peut être ou la clé émise par le client reliant cette réponse à une requete client
	 * ou le nom d'un topic.
	 */
	protected String id;
	/**
	 * Le résultat de la requete
	 */
	protected Object result = null;
	/**
	 * L'erreur de la requete
	 */
	protected Fault fault = null;

	/**
	 * Attention le result/fault n'est pas désérialisé
	 *
	 * @param json
	 * @return
	 */
	public static MessageToClient createFromJson(String json) {
		try (JsonReader reader = Json.createReader(new StringReader(json))) {
			JsonObject root = reader.readObject();
			MessageToClient message = new MessageToClient();
			message.setId(root.getString(Constants.Message.ID));
			if (root.containsKey(Constants.Message.RESULT)) {
				JsonValue result = root.get(Constants.Message.RESULT);
				message.setResult("" + result);
			} else {
				if (root.containsKey(Constants.Message.FAULT)) {
					JsonObject faultJs = root.getJsonObject(Constants.Message.FAULT);
					Fault f = new Fault();
					f.setMessage(faultJs.getString(Constants.Fault.MSG));
					f.setClassname(faultJs.getString(Constants.Fault.CLASSNAME));
					f.setStacktrace(faultJs.getString(Constants.Fault.STACKTRACE));
					message.setFault(f);
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
				res = String.format(resultFormat, Constants.Message.FAULT, "\"" + JsonProcessingException.class.getName() + "\"");
			}
		}
		if (null != this.getFault()) {
			String faultFormat = ",\"%s\":{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\"}";
			res = String.format(faultFormat, Constants.Message.FAULT,
					  Constants.Fault.MSG, this.getFault().getMessage(),
					  Constants.Fault.CLASSNAME, this.getFault().getClassname(),
					  Constants.Fault.STACKTRACE, this.getFault().getStacktrace());
		}
		String json = String.format("{\"%s\":\"%s\"%s}",
				  Constants.Message.ID, this.getId(),
				  res);
		return json;
	}
}
