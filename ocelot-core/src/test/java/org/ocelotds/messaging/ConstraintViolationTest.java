/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ocelotds.messaging;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class ConstraintViolationTest {

	@InjectMocks
	@Spy
	ConstraintViolation instance;

	/**
	 * Test of ConstraintViolation constructor, of class ConstraintViolation.
	 */
	@Test
	public void testEmptyConstructor() {
		ConstraintViolation cv = new ConstraintViolation();
		String result = cv.getMessage();
		assertThat(result).isEqualTo(null);
	}

	/**
	 * Test of ConstraintViolation constructor, of class ConstraintViolation.
	 */
	@Test
	public void testConstructor() {
		ConstraintViolation cv = new ConstraintViolation("message");
		String result = cv.getMessage();
		assertThat(result).isEqualTo("message");
	}

	/**
	 * Test of getIndex and setIndex method, of class ConstraintViolation.
	 */
	@Test
	public void testSetGetIndex() {
		System.out.println("getIndex");
		System.out.println("setIndex");
		int expResult = 5;
		instance.setIndex(expResult);
		int result = instance.getIndex();
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getMessage and setMessage method, of class ConstraintViolation.
	 */
	@Test
	public void testSetGetMessage() {
		System.out.println("getMessage");
		System.out.println("setMessage");
		String expResult = "message";
		instance.setMessage(expResult);
		String result = instance.getMessage();
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getName and setName method, of class ConstraintViolation.
	 */
	@Test
	public void testSetGetName() {
		System.out.println("getName");
		System.out.println("setName");
		String expResult = "name";
		instance.setName(expResult);
		String result = instance.getName();
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getProp and setProp method, of class ConstraintViolation.
	 */
	@Test
	public void testSetGetProp() {
		System.out.println("getProp");
		System.out.println("setProp");
		String expResult = "prop";
		instance.setProp(expResult);
		String result = instance.getProp();
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of toString method, of class ConstraintViolation.
	 */
	@Test
	public void testToString() {
		System.out.println("toString");
		instance.setName("arg");
		instance.setProp(null);
		instance.setMessage("can't be null");
		String expResult = "ConstraintViolation{arg can't be null}";
		String result = instance.toString();
		assertThat(result).isEqualTo(expResult);
		instance.setProp("name");
		expResult = "ConstraintViolation{arg.name can't be null}";
		result = instance.toString();
		assertThat(result).isEqualTo(expResult);
	}

}