/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.ocelot.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/**
 *
 * @author francois
 */
public class Fault {

	public Fault() {
	}

	public Fault(Throwable t) {
		this.message = t.getMessage();
		this.classname = t.getClass().getName();
		String[] stackTraceElements = new String[Math.min(t.getStackTrace().length, 50)];
		for (int i = 0; i < stackTraceElements.length; i++) {
			stackTraceElements[i] = t.getStackTrace()[i].toString();
		}
		this.stacktrace = stackTraceElements;
	}

	private String message;
	private String classname;
	private String[] stacktrace;

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

	public String[] getStacktrace() {
		return stacktrace;
	}

	public void setStacktrace(String[] stacktrace) {
		this.stacktrace = stacktrace;
	}

	public static Fault createFromJson(String json) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(json, Fault.class);
	}

	public String toJson() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}

}
