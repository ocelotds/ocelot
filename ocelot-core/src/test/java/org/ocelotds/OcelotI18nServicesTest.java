/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.context.OcelotContext;
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
	
	@Mock
	private OcelotContext ocelotContext;

	@Before
	public void init() {
	}
	
	/**
	 * Test of getUserLocale method, of class OcelotI18nServices.
	 */
	@Test
	public void testGetUserLocale() {
		System.out.println("getUserLocale");
		Map<String, Object> map = new HashMap<>();
		Locale expResult = Locale.FRANCE;

		when(ocelotContext.getLocale()).thenReturn(expResult);

		Locale result = ocelotI18nServices.getUserLocale();
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getLocalizedMessage method, of class OcelotI18nServices.
	 */
	@Test
	public void testGetLocalizedMessage() {
		System.out.println("getLocalizedMessage");
		String bundleName = "test";

		when(ocelotContext.getLocale()).thenReturn(Locale.US).thenReturn(Locale.FRANCE);
		
		String expResult = "Hello François";
		String result = ocelotI18nServices.getLocalizedMessage(bundleName, "HELLOGUY", new Object[]{"François"});
		assertThat(result).isEqualTo(expResult);

		expResult = "Bonjour François";
		result = ocelotI18nServices.getLocalizedMessage(bundleName, "HELLOGUY", new Object[]{"François"});
		assertThat(result).isEqualTo(expResult);
	}
	
}
