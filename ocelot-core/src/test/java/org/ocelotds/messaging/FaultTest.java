/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.messaging;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hhfrancois
 */
public class FaultTest {
	
	/**
	 * Test of getMessage method, of class Fault.
	 */
	@Test
	public void testGetMessage() {
		System.out.println("getMessage");
		String expResult = "ExceptionMssage";
		try {
			throw new Exception(expResult);
		} catch (Exception e) {
			Fault instance = new Fault(e, 5);
			String result = instance.getMessage();
			assertThat(result).isEqualTo(expResult);
		}
	}

	/**
	 * Test of getClassname method, of class Fault.
	 */
	@Test
	public void testGetClassname() {
		System.out.println("getClassname");
		String expResult = "java.lang.Exception";
		try {
			throw new Exception();
		} catch (Exception e) {
			Fault instance = new Fault(e, 5);
			String result = instance.getClassname();
			assertThat(result).isEqualTo(expResult);
		}
	}

	/**
	 * Test of getStacktrace method, of class Fault.
	 */
	@Test
	public void testNullArgument() {
		Fault instance = new Fault(null, 5);
		String msg = instance.getMessage();
		assertThat(msg).isNull();
		String clsName = instance.getClassname();
		assertThat(clsName).isNull();
		String[] stacktrace = instance.getStacktrace();
		assertThat(stacktrace).isNotNull();
		assertThat(stacktrace.length).isEqualTo(0);
	}
	/**
	 * Test of getStacktrace method, of class Fault.
	 */
	@Test
	public void testGetStacktrace() {
		System.out.println("getStacktrace");
		int expResult = 5;
		try {
			throw new Exception();
		} catch (Exception e) {
			Fault instance = new Fault(e, expResult);
			int result = instance.getStacktrace().length;
			assertThat(result).isEqualTo(expResult);
		}
		expResult = 10;
		try {
			throw new Exception();
		} catch (Exception e) {
			Fault instance = new Fault(e, expResult);
			int result = instance.getStacktrace().length;
			assertThat(result).isEqualTo(expResult);
		}
		try {
			throw new Exception();
		} catch (Exception e) {
			try {
				throw new Exception(e);
			} catch (Exception e1) {
				Fault instance = new Fault(e1, expResult);
				int result = instance.getStacktrace().length;
				assertThat(result).isEqualTo(expResult);
			}
		}
	}

	/**
	 * Test of getThrowable method, of class Fault.
	 */
	@Test
	public void testGetThrowable() {
		System.out.println("getThrowable");
		Exception expResult = new Exception();
		try {
			throw expResult;
		} catch (Exception e) {
			Fault instance = new Fault(e, 5);
			Throwable result = instance.getThrowable();
			assertThat(result).isEqualTo(expResult);
		}
	}

	/**
	 * Test of createFromJson method, of class Fault.
	 */
	@Test
	public void testCreateFromJson() throws Exception {
		System.out.println("createFromJson");
		String expResult = "ExceptionMssage";
		String expResult2 = "java.lang.Exception";
		try {
			throw new Exception(expResult);
		} catch (Exception e) {
			Fault instance = new Fault(e, 5);
			Fault result = Fault.createFromJson(instance.toJson());
			assertThat(result.getMessage()).isEqualTo(expResult);
			assertThat(result.getClassname()).isEqualTo(expResult2);
			assertThat(instance.toJson()).isEqualTo(instance.toString());
		}
	}

	@Test
	public void testEquals() {
		System.out.println("testEquals");
		try {
			throw new Exception("Message");
		} catch (Exception e) {
			Fault instance = new Fault(e, 5);
			Fault test = null;
			assertThat(instance.equals(test)).isFalse();
			assertThat(instance.equals("NotSameClass")).isFalse();
			test = new Fault(null, 5);
			assertThat(instance.equals(test)).isFalse();
			test = new Fault(e, 5);
			assertThat(instance.equals(test)).isTrue();
		}
	}

	@Test
	public void testHashCode() {
		System.out.println("testHashCode");
		Fault a = null;
		Fault b = null;
		try {
			throw new Exception("Message");
		} catch (Exception e) {
			a = new Fault(e, 5);
		}
		assertThat(a.hashCode()).isEqualTo(a.hashCode());
		try {
			throw new IllegalAccessException("Message");
		} catch (Exception e) {
			b = new Fault(e, 5);
		}
		assertThat(a.hashCode()).isNotEqualTo(b.hashCode());
		try {
			throw new Exception("Message1");
		} catch (Exception e) {
			b = new Fault(e, 5);
		}
		assertThat(a.hashCode()).isNotEqualTo(b.hashCode());
	}
}
