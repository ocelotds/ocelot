/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package fr.hhdev.ocelot.messaging;

import fr.hhdev.ocelot.Constants;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author hhfrancois
 */
public class Fault {

	private final Throwable throwable;
	final private int stacktracelength;
	protected String message = null;
	protected String classname = null;

	public Fault(Throwable t, int stacktracelength) {
		this.throwable = t;
		if (t != null) {
			this.message = t.getMessage();
			this.classname = t.getClass().getName();
		}
		this.stacktracelength = stacktracelength;
	}

	public String getMessage() {
		return message;
	}

	public String getClassname() {
		return classname;
	}

	public StackTraceElement[] getStacktrace() {
		if (throwable != null && stacktracelength > 0) {
			StackTraceElement[] stackTraces = throwable.getStackTrace();
			return Arrays.copyOf(stackTraces, Math.min(stackTraces.length, stacktracelength));
		}
		return new StackTraceElement[]{};
	}

	public Throwable getThrowable() {
		return throwable;
	}

	/**
	 * Fault json format : {"message":"","classname":"","stacktrace":["","",""]}
	 *
	 * @param json
	 * @return
	 * @throws IOException
	 */
	public static Fault createFromJson(String json) throws IOException {
		Fault fault = new Fault(null, 0);
		try (JsonReader reader = Json.createReader(new StringReader(json))) {
			JsonObject root = reader.readObject();
			fault.message = root.getString(Constants.Message.Fault.MESSAGE);
			fault.classname = root.getString(Constants.Message.Fault.CLASSNAME);
		}
		return fault;
	}

	public String toJson() {
		StackTraceElement[] stacktraceElt = getStacktrace();
		String stacktrace = String.format(String.join(",", Collections.nCopies(stacktraceElt.length, "\"%s\"")), (Object[]) stacktraceElt);
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":[%s]}",
				  Constants.Message.Fault.CLASSNAME, this.classname,
				  Constants.Message.Fault.MESSAGE, this.message,
				  Constants.Message.Fault.STACKTRACE, stacktrace);
		return json;
	}

	@Override
	public String toString() {
		return toJson();
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
