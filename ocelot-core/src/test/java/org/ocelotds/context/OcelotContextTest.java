/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.context;

import java.security.Principal;
import java.util.Locale;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class OcelotContextTest {

	@Mock
	private Logger logger;
	
	@Mock
	private Principal principal;

	@InjectMocks
	private OcelotContext instance;

	/**
	 * Test of getLocale method, of class OcelotContext.
	 */
	@Test
	public void testGetLocale() {
		System.out.println("getLocale");
		ThreadLocalContextHolder.put(Constants.LOCALE, null);
		Locale expResult = new Locale("en", "US");
		Locale result = instance.getLocale();
		assertThat(result).isEqualTo(expResult);

		expResult = new Locale("fr", "FR");
		ThreadLocalContextHolder.put(Constants.LOCALE, expResult);
		result = instance.getLocale();
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getUsername method, of class OcelotContext.
	 */
	@Test
	public void testGetUsername() {
		System.out.println("getUsername");
		String expResult = "username";
		when(principal.getName()).thenReturn(expResult);
		String result = instance.getUsername();
		assertThat(result).isEqualTo(expResult);
	}
}