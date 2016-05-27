/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.messaging;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.BeforeClass;
import org.ocelotds.Constants;
import static org.ocelotds.messaging.MessageFromClient.getArgumentsFromMessage;

/**
 *
 * @author hhfrancois
 */
public class MessageFromClientTest {

	private static MessageFromClient msgWithArg;
	private static MessageFromClient msgWithoutArg;
	
	@BeforeClass
	public static void setUpClass() {
		List<String> args = new ArrayList<>();
		args.add("\"arg\"");
		List<String> argNames = new ArrayList<>();
		argNames.add("argName");
		msgWithArg = new MessageFromClient();
		msgWithArg.setDataService("DataServiceClassName");
		msgWithArg.setId(UUID.randomUUID().toString());
		msgWithArg.setOperation("methodName");
		msgWithArg.setParameterNames(argNames);
		msgWithArg.setParameters(args);

		msgWithoutArg = new MessageFromClient();
		msgWithoutArg.setDataService("DataServiceClassName");
		msgWithoutArg.setId(UUID.randomUUID().toString());
		msgWithoutArg.setOperation("methodName");
		msgWithoutArg.setParameterNames(new ArrayList<String>());
		msgWithoutArg.setParameters(new ArrayList<String>());
	}

	/**
	 * Test of getId method, of class MessageFromClient.
	 */
	@Test
	public void testGetSetId() {
		System.out.println("testGetSetId");
		MessageFromClient instance = new MessageFromClient();
		String expResult = UUID.randomUUID().toString();
		instance.setId(expResult);
		String result = instance.getId();
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getDataService method, of class MessageFromClient.
	 */
	@Test
	public void testGetSetDataService() {
		System.out.println("testGetSetDataService");
		MessageFromClient instance = new MessageFromClient();
		String expResult = "DataService";
		instance.setDataService(expResult);
		String result = instance.getDataService();
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getOperation method, of class MessageFromClient.
	 */
	@Test
	public void testGetSetOperation() {
		System.out.println("testGetSetOperation");
		MessageFromClient instance = new MessageFromClient();
		String expResult = "method";
		instance.setOperation(expResult);
		String result = instance.getOperation();
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getParameters method, of class MessageFromClient.
	 */
	@Test
	public void testGetSetParameters() {
		System.out.println("testGetSetParameters");
		MessageFromClient instance = new MessageFromClient();
		List<String> expResult = new ArrayList<>();
		expResult.add("arg");
		instance.setParameters(expResult);
		List<String> result = instance.getParameters();
		assertThat(result).isNotNull();
		assertThat(result).isNotEmpty();
	}

	/**
	 * Test of getParameterNames method, of class MessageFromClient.
	 */
	@Test
	public void testGetSetParameterNames() {
		System.out.println("testGetSetParameterNames");
		MessageFromClient instance = new MessageFromClient();
		List<String> expResult = new ArrayList<>();
		expResult.add("argName");
		instance.setParameterNames(expResult);
		List<String> result = instance.getParameterNames();
		assertThat(result).isNotNull();
		assertThat(result).isNotEmpty();
	}

	@Test
	public void testEquals() {
		System.out.println("testEquals");
		String expResult = UUID.randomUUID().toString();
		MessageFromClient msg = new MessageFromClient();
		msg.setId(expResult);
		MessageFromClient test = null;
		assertThat(msg.equals(test)).isFalse();
		assertThat(msg.equals("NotSameClass")).isFalse();
		test = new MessageFromClient();
		test.setId(UUID.randomUUID().toString());
		assertThat(msg.equals(test)).isFalse();
		test.setId(expResult);
		assertThat(msg.equals(test)).isTrue();
	}

	@Test
	public void testHashCode() {
		System.out.println("testHashCode");
		MessageFromClient a = createRandom();
		MessageFromClient b = createRandom();
		assertThat(a.hashCode()).isEqualTo(a.hashCode());
		assertThat(a.hashCode()).isNotEqualTo(b.hashCode());
		b.setId(a.getId());
		assertThat(a.hashCode()).isEqualTo(b.hashCode());
	}

	@Test
	public void createFromJsonTest() {
		System.out.println("createFromJson");
		String json = mfcToJson(msgWithArg);
		MessageFromClient mfc = MessageFromClient.createFromJson(json);
		assertThat(mfc).isEqualToComparingFieldByField(msgWithArg);
	}
	
	/**
	 * Test of getArgumentsFromMessage method, of class.
	 */
	@Test
	public void getArgumentsFromMessageTest() {
		System.out.println("getArgumentsFromMessage");
		try (JsonReader reader = Json.createReader(new StringReader("[]"))) {
			JsonArray array = reader.readArray();
			List<String> result = MessageFromClient.getArgumentsFromMessage(array);
			assertThat(result).isEmpty();
		}
		try (JsonReader reader = Json.createReader(new StringReader("[\"foo\"]"))) {
			JsonArray array = reader.readArray();
			List<String> result = MessageFromClient.getArgumentsFromMessage(array);
			assertThat(result).hasSize(1);
		}
		try (JsonReader reader = Json.createReader(new StringReader("[\"foo\", 5]"))) {
			JsonArray array = reader.readArray();
			List<String> result = MessageFromClient.getArgumentsFromMessage(array);
			assertThat(result).hasSize(2);
			assertThat(result.get(0)).isEqualTo("\"foo\"");
			assertThat(result.get(1)).isEqualTo("5");
		}
	}
	
	/**
	 * Test of getArgumentNamesFromMessage method, of class.
	 */
	@Test
	public void getArgumentNamesFromMessageTest() {
		System.out.println("getArgumentNamesFromMessage");
		try (JsonReader reader = Json.createReader(new StringReader("[]"))) {
			JsonArray array = reader.readArray();
			List<String> result = MessageFromClient.getArgumentNamesFromMessage(array);
			assertThat(result).isEmpty();
		}
		try (JsonReader reader = Json.createReader(new StringReader("[\"arg1\"]"))) {
			JsonArray array = reader.readArray();
			List<String> result = MessageFromClient.getArgumentsFromMessage(array);
			assertThat(result).hasSize(1);
		}
		try (JsonReader reader = Json.createReader(new StringReader("[\"arg1\", \"arg2\"]"))) {
			JsonArray array = reader.readArray();
			List<String> result = MessageFromClient.getArgumentsFromMessage(array);
			assertThat(result).hasSize(2);
			assertThat(result.get(0)).isEqualTo("\"arg1\"");
			assertThat(result.get(1)).isEqualTo("\"arg2\"");
		}
	}

	private String mfcToJson(MessageFromClient mfc) {
		StringBuilder jsonParamNames = new StringBuilder("[");
		boolean first = true;
		for (String parameterName : mfc.getParameterNames()) {
			if(!first) {
				jsonParamNames.append(",");
			}
			jsonParamNames.append(Constants.QUOTE).append(parameterName).append(Constants.QUOTE);
			first = false;
		}
		jsonParamNames.append("]");
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s,\"%s\":%s}",
				  Constants.Message.ID, mfc.getId(),
				  Constants.Message.DATASERVICE, mfc.getDataService(),
				  Constants.Message.OPERATION, mfc.getOperation(),
				  Constants.Message.ARGUMENTNAMES, jsonParamNames.toString(),
				  Constants.Message.ARGUMENTS, Arrays.toString(mfc.getParameters().toArray(new String[mfc.getParameters().size()])));
		return json;
	}

	private MessageFromClient createRandom() {
		MessageFromClient result = new MessageFromClient();
		result.setDataService(UUID.randomUUID().toString());
		result.setId(UUID.randomUUID().toString());
		result.setOperation(UUID.randomUUID().toString());
		result.setParameterNames(new ArrayList<String>());
		result.setParameters(new ArrayList<String>());
		return result;
	}
}
