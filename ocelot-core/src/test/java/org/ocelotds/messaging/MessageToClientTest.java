/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ocelotds.Constants;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 *
 * @author hhfrancois
 */
public class MessageToClientTest {
	
	public MessageToClientTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}

	/**
	 * Test of getType method, of class MessageToClient.
	 */
	@Test
	public void testGetSetType() {
		System.out.println("testGetSetType");
		MessageToClient instance = new MessageToClient();
		MessageType expResult = MessageType.FAULT;
		instance.setType(expResult);
		MessageType result = instance.getType();
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getId method, of class MessageToClient.
	 */
	@Test
	public void testGetSetId() {
		System.out.println("testGetSetId");
		MessageToClient instance = new MessageToClient();
		String expResult = UUID.randomUUID().toString();
		instance.setId(expResult);
		String result = instance.getId();
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getResponse method, of class MessageToClient.
	 */
	@Test
	public void testGetSetResponse() {
		System.out.println("getSetResponse");
		MessageToClient instance = new MessageToClient();
		Object expResult = "ObjectResult";
		instance.setResponse(expResult);
		Object result = instance.getResponse();
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of setResult method, of class MessageToClient.
	 */
	@Test
	public void testSetResult() {
		System.out.println("setResult");
		MessageToClient instance = new MessageToClient();
		Object expResult = "ObjectResult";
		instance.setResult(expResult);
		Object result = instance.getResponse();
		assertThat(result).isEqualTo(expResult);
		assertThat(instance.getType()).isEqualTo(MessageType.RESULT);
	}

	/**
	 * Test of setFault method, of class MessageToClient.
	 */
	@Test
	public void testSetFault() {
		System.out.println("setFault");
		MessageToClient instance = new MessageToClient();
		Fault expResult = new Fault(null, 5);
		instance.setFault(expResult);
		Object result = instance.getResponse();
		assertThat(result).isEqualTo(expResult);
		assertThat(instance.getType()).isEqualTo(MessageType.FAULT);
	}

	/**
	 * Test of getDeadline method, of class MessageToClient.
	 */
	@Test
	public void testGetSetDeadline() {
		System.out.println("getSetDeadline");
		MessageToClient instance = new MessageToClient();
		long expResult = new Random().nextInt(200);
		instance.setDeadline(expResult);
		long result = instance.getDeadline();
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of createFromJson method, of class MessageToClient.
	 */
	@Test
	public void testCreateFromResultJson() throws JsonProcessingException {
		System.out.println("testCreateFromResultJson");
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s,\"%s\":%s}",
				  Constants.Message.TYPE, MessageType.RESULT, Constants.Message.ID, "111", 
				  Constants.Message.DEADLINE, 0, Constants.Message.RESPONSE, "\"Result\"");
		MessageToClient msg = MessageToClient.createFromJson(json);
		assertThat(msg.getId()).isEqualTo("111");
		assertThat(msg.getDeadline()).isEqualTo(0);
		assertThat(msg.getType()).isEqualTo(MessageType.RESULT);
		assertThat(msg.getResponse()).isEqualTo("\"Result\"");
	}

	/**
	 * Test of createFromJson method, of class MessageToClient.
	 */
	@Test
	public void testCreateFromFaultJson() throws JsonProcessingException {
		System.out.println("testCreateFromResultJson");
		Fault fault = null;
		try {
			throw new Exception("ErrorMessage");
		} catch (Exception e) {
			fault = new Fault(e, 0);
		}
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s,\"%s\":%s}",
				  Constants.Message.TYPE, MessageType.FAULT, Constants.Message.ID, "111", 
				  Constants.Message.DEADLINE, 0, Constants.Message.RESPONSE, fault.toJson());
		MessageToClient msg = MessageToClient.createFromJson(json);
		assertThat(msg.getId()).isEqualTo("111");
		assertThat(msg.getDeadline()).isEqualTo(0);
		assertThat(msg.getType()).isEqualTo(MessageType.FAULT);
		String expResult = "{\"classname\":\"java.lang.Exception\",\"message\":\"ErrorMessage\",\"stacktrace\":[]}";
		assertThat(""+msg.getResponse()).isEqualTo(expResult);
	}
	/**
	 * Test of createFromJson method, of class MessageToClient.
	 */
	@Test
	public void testJsonAndToString() throws JsonProcessingException {
		System.out.println("testJsonAndToString");
		MessageToClient msg = spy(MessageToClient.class);
		long deadline = new Random().nextInt(200);
		msg.setDeadline(deadline);
		Fault fault = null;
		try {
			throw new Exception("ErrorMessage");
		} catch (Exception e) {
			fault = new Fault(e, 5);
		}
		msg.setFault(fault);
		String id = UUID.randomUUID().toString();
		msg.setId(id);
		ObjectMapper mapper = new ObjectMapper();
		String jsonResponse = mapper.writeValueAsString(fault);
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s,\"%s\":%s}",
				  Constants.Message.TYPE, msg.getType(), Constants.Message.ID, msg.getId(), 
				  Constants.Message.DEADLINE, msg.getDeadline(), Constants.Message.RESPONSE, jsonResponse);
		assertThat(msg.toJson()).isEqualTo(json);
		assertThat(msg.toString()).isEqualTo(json);

		msg.setFault(null);
		msg.setResult("Result");

		jsonResponse = mapper.writeValueAsString(msg.getResponse());
		json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s,\"%s\":%s}",
				  Constants.Message.TYPE, msg.getType(), Constants.Message.ID, msg.getId(), 
				  Constants.Message.DEADLINE, msg.getDeadline(), Constants.Message.RESPONSE, jsonResponse);
		assertThat(msg.toJson()).isEqualTo(json);
		assertThat(msg.toString()).isEqualTo(json);

		ObjectMapper mapperThrowException = mock(ObjectMapper.class);
		when(mapperThrowException.writeValueAsString(anyString())).thenThrow(JsonProcessingException.class);
		when(msg.getObjectMapper()).thenReturn(mapperThrowException);
		json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s,\"%s\":%s}",
				  Constants.Message.TYPE, msg.getType(), Constants.Message.ID, msg.getId(), 
				  Constants.Message.DEADLINE, msg.getDeadline(), Constants.Message.RESPONSE, "");
		assertThat(msg.toJson()).isEqualTo(json);
	}
	
	@Test
	public void testEquals() {
		System.out.println("testEquals");
		String expResult = UUID.randomUUID().toString();
		MessageToClient msg = new MessageToClient();
		msg.setId(expResult);
		MessageToClient test = null;
		assertThat(msg.equals(test)).isFalse();
		assertThat(msg.equals("NotSameClass")).isFalse();
		test = new MessageToClient();
		test.setId(UUID.randomUUID().toString());
		assertThat(msg.equals(test)).isFalse();
		test.setId(expResult);
		assertThat(msg.equals(test)).isTrue();
	}

	@Test
	public void testHashCode() {
		System.out.println("testHashCode");
		MessageToClient a = createRandom();
		MessageToClient b = createRandom();
		assertThat(a.hashCode()).isEqualTo(a.hashCode());
		assertThat(a.hashCode()).isNotEqualTo(b.hashCode());
		b.setId(a.getId());
		assertThat(a.hashCode()).isEqualTo(b.hashCode());
	}

	private MessageToClient createRandom() {
		MessageToClient result = new MessageToClient();
		result.setDeadline(UUID.randomUUID().hashCode());
		result.setId(UUID.randomUUID().toString());
		result.setResponse(new ArrayList<String>());
		return result;
	}


}
