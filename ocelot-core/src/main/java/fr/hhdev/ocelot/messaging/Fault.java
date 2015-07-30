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

	private String[] getStacktrace() {
		if (throwable != null && stacktracelength > 0) {
			StackTraceElement[] stackTraces = throwable.getStackTrace();
			int nb = Math.min(stackTraces.length, stacktracelength);
			String[] result = new String[nb];
			for (int i = 0; i < nb; i++) {
				result[i] = stackTraces[i].toString();
			}
			return result;
		}
		return new String[]{};
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
		String[] stacktraceElt = getStacktrace();
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s}",
				  Constants.Message.Fault.CLASSNAME, this.classname,
				  Constants.Message.Fault.MESSAGE, this.message,
				  Constants.Message.Fault.STACKTRACE, String.format(Arrays.toString(Collections.nCopies(stacktraceElt.length, "\"%s\"").toArray()), (Object[]) stacktraceElt));
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
		return Objects.equals(this.classname, other.classname);
	}

}
