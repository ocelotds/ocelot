/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 * Message en réponse à une solicitation cliente Le client émét une MessageFromClient, le serveur lui répond de facon asynchrone par un MessageToClient
 *
 * @author hhfrancois
 */
public class MessageToClient {

	private static final long serialVersionUID = -834697863344344124L;

	/**
	 * L'identifiant du message, cela peut être ou la clé émise par le client reliant cette réponse à une requete client ou le nom d'un topic.
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
				Fault f = new Fault(ex);
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
		String json = String.format("{\"%s\":\"%s\"%s}",
				  Constants.Message.ID, this.getId(),
				  res);
		return json;
	}
}
