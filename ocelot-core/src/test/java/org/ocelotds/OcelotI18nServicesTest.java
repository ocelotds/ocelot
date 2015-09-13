/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import org.ocelotds.context.ThreadLocalContextHolder;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class OcelotI18nServicesTest {

	@Mock
	private Logger logger;

	@InjectMocks
	private OcelotI18nServices ocelotI18nServices;

	@Before
	public void init() {
		ThreadLocalContextHolder.put(Constants.LOCALE, null);
	}
	
	/**
	 * Test of getUserLocale method, of class OcelotI18nServices.
	 */
	@Test
	public void testGetUserLocale() {
		System.out.println("getUserLocale");
		Locale expResult = new Locale("en", "US");
		Locale result = ocelotI18nServices.getUserLocale();
		assertThat(result).isEqualTo(expResult);
		expResult = new Locale("fr", "FR");
		ThreadLocalContextHolder.put(Constants.LOCALE, expResult);
		result = ocelotI18nServices.getUserLocale();
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getLocalizedMessage method, of class OcelotI18nServices.
	 */
	@Test
	public void testGetLocalizedMessage() {
		System.out.println("getLocalizedMessage");
		String bundleName = "test";
		Locale locale = new Locale("en", "US");
		String expResult = "Hello François";
		ThreadLocalContextHolder.put(Constants.LOCALE, locale);
		String result = ocelotI18nServices.getLocalizedMessage(bundleName, "HELLOGUY", new Object[]{"François"});
		assertThat(result).isEqualTo(expResult);

		expResult = "Bonjour François";
		locale = new Locale("fr", "FR");
		ThreadLocalContextHolder.put(Constants.LOCALE, locale);
		result = ocelotI18nServices.getLocalizedMessage(bundleName, "HELLOGUY", new Object[]{"François"});
		assertThat(result).isEqualTo(expResult);
	}
	
}
