/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.i18n;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hhfrancois
 */
public class LocaleTest {
	
	/**
	 * Test of getLanguage method, of class Locale.
	 */
	@Test
	public void testGetSetLanguage() {
		System.out.println("get_setLanguage");
		Locale instance = new Locale();
		String expResult = "fr";
		instance.setLanguage(expResult);
		String result = instance.getLanguage();
		assertEquals(expResult, result);
		expResult = "us";
		instance.setLanguage(expResult);
		result = instance.getLanguage();
		assertEquals(expResult, result);
	}

	/**
	 * Test of getCountry method, of class Locale.
	 */
	@Test
	public void testGetSetCountry() {
		System.out.println("get_setCountry");
		Locale instance = new Locale();
		String expResult = "fr";
		instance.setCountry(expResult);
		String result = instance.getCountry();
		assertEquals(expResult, result);
		expResult = "us";
		instance.setCountry(expResult);
		result = instance.getCountry();
		assertEquals(expResult, result);
	}
}
