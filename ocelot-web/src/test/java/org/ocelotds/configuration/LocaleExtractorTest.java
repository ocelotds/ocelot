/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

import java.util.Locale;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.exceptions.LocaleNotFoundException;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class LocaleExtractorTest {
	
	@InjectMocks
	@Spy
	private LocaleExtractor instance;
	
	
	/**
	 * Test of extractFromAccept method, of class LocaleExtractor.
	 * @throws org.ocelotds.exceptions.LocaleNotFoundException
	 */
	@Test
	public void testExtractFromAccept() throws LocaleNotFoundException {
		Locale result = instance.extractFromAccept("fr-FR;q=1");
		assertThat(result).isEqualTo(Locale.FRANCE);
	}
	
	@Test(expected = LocaleNotFoundException.class)
	public void testExtractFromAcceptNok() throws LocaleNotFoundException {
		instance.extractFromAccept("fr");
	}

	@Test(expected = LocaleNotFoundException.class)
	public void testExtractFromAcceptNok2() throws LocaleNotFoundException {
		instance.extractFromAccept("");
	}

	@Test(expected = LocaleNotFoundException.class)
	public void testExtractFromAcceptNok3() throws LocaleNotFoundException {
		instance.extractFromAccept(null);
	}
}
