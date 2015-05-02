/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author hhfrancois
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

	@Override
	public String toString() {
		try {
			return toJson();
		} catch (IOException ex) {
		}
		return "Fault{" + "message=" + message + ", classname=" + classname + ", stacktrace=" + Arrays.deepToString(stacktrace) + '}';
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 29 * hash + Objects.hashCode(this.message);
		hash = 29 * hash + Objects.hashCode(this.classname);
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
		final Fault other = (Fault) obj;
		if (!Objects.equals(this.message, other.message)) {
			return false;
		}
		if (!Objects.equals(this.classname, other.classname)) {
			return false;
		}
		return true;
	}
	
	

	
	

}
