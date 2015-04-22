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

/**
 *
 * @author francois
 */
public class Fault {

	private String message;
	private String classname;
	private String stacktrace;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public String getStacktrace() {
		return stacktrace;
	}

	public void setStacktrace(String stacktrace) {
		this.stacktrace = stacktrace;
	}
	
	public static Fault createFromJson(String json) {
		try (JsonReader reader = Json.createReader(new StringReader(json))) {
			JsonObject faultJs = reader.readObject();
			Fault f = new Fault();
			f.setMessage(faultJs.getString(Constants.Fault.MSG));
			f.setClassname(faultJs.getString(Constants.Fault.CLASSNAME));
			f.setStacktrace(faultJs.getString(Constants.Fault.STACKTRACE));
			return f;
		}
	}
	
	public String toJson() {
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\"}",
				  Constants.Fault.MSG, message, Constants.Fault.CLASSNAME, classname, Constants.Fault.STACKTRACE, stacktrace);
		return json;
	}
	
}
