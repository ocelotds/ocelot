/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.BeforeClass;

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
		argNames.add("\"argName\"");
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
	public void testJsonWithArguments() {
		System.out.println("testJsonWithArguments");
		String json = msgWithArg.toJson();
		MessageFromClient result = MessageFromClient.createFromJson(json);
		assertThat(result).isEqualToComparingFieldByField(msgWithArg);
	}

	@Test
	public void testJsonWithNoArguments() {
		System.out.println("testJsonWithNoArguments");
		String json = msgWithoutArg.toJson();
		MessageFromClient result = MessageFromClient.createFromJson(json);
		assertThat(result).isEqualToComparingFieldByField(msgWithoutArg);
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
	public void testToString() {
		System.out.println("testToString");
		assertThat(msgWithArg.toJson()).isEqualTo(msgWithArg.toString());
		assertThat(msgWithoutArg.toJson()).isEqualTo(msgWithoutArg.toString());
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
