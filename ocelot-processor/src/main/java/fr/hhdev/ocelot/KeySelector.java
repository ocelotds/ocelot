/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * Classe permettant de retourner le l'argument et son selecteur Le constructeur prends en argument la valeurs keys des annotations JSCacheRemove et
 * JsCacheResult
 *
 * @author hhfrancois
 */
public class KeySelector {

	private final Iterator<String> keys;
	private String lastKey = "**";

	public KeySelector(String keys) {
		if (keys == null) {
			keys = "**";
		}
		this.keys = Arrays.asList(keys.split(",")).iterator();
		if (!this.keys.hasNext()) {
			lastKey = "";
		}
	}

	/**
	 * A partir du nom de l'argument js, retourne la signature adéquate en fonction du pattern fournit au constructeur
	 * Soit l'argument : 'i'<br>
	 * si currentPattern = '*' ou '**' alors return 'i'<br>
	 * si currentPattern = '-' return 'null'<br>
	 * si currentPattern = 'id' return (i)?i.id:null<br>
	 * si currentPattern = 'name' return (i)?i.name:null<br>
	 *
	 * @param arg
	 * @return
	 */
	public String nextJSSignature(String arg) {
		String current;
		if (keys.hasNext()) {
			current = keys.next().trim();
			lastKey = current;
		} else {
			if (!lastKey.equals("**")) {
				return "null";
			} else {
				return arg;
			}
		}
		switch (current) {
			case "**":
			case "*":
				return arg;
			case "-":
				return "null";
			default:
				return "(" + arg + ")?" + arg + "." + current + ":null";
		}
	}

	/**
	 * A partir de l'argument js, retourne la valeur adéquate en fonction du pattern fournit au constructeur<br>
	 * Soit l'argument : {"id":5,"name":"foo"}<br>
	 * si currentPattern = '*' ou '**' alors return '{"id":5,"name":"foo"}'<br>
	 * si currentPattern = '-' return 'null'<br>
	 * si currentPattern = 'id' return 5<br>
	 * si currentPattern = 'name' return 'foo'<br>
	 *
	 * @param jsonarg
	 * @return
	 */
	public String nextJSValue(String jsonarg) {
		String current;
		if (keys.hasNext()) {
			current = keys.next().trim();
			lastKey = current;
		} else {
			if (!lastKey.equals("**")) {
				return "null";
			} else {
				return jsonarg;
			}
		}
		switch (current) {
			case "**":
			case "*":
				return jsonarg;
			case "-":
				return "null";
			default:
				JsonReader reader = Json.createReader(new StringReader(jsonarg));
				JsonObject root = reader.readObject();
				return root.get(current).toString();
		}
	}
}
