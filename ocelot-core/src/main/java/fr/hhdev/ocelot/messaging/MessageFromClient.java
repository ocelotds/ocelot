package fr.hhdev.ocelot.messaging;

import fr.hhdev.ocelot.Constants;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message from client for call any service.
 * the topic of command identify the resolver for get instance of service
 * 
 * @author hhfrancois
 */
public class MessageFromClient {

	private static Logger logger = LoggerFactory.getLogger(MessageFromClient.class);
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
				JsonValue arg = argArray.get(idx++);
				logger.debug("Récupération des arguments dans le message {} : '{}'", arg.getValueType().name(), arg.toString());
				params.add(arg.toString());
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

	@Override
	public String toString() {
		return toJson();
	}
}
