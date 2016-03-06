/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.configuration;

import java.util.Locale;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.exceptions.LocaleNotFoundException;
import static org.mockito.Mockito.*;
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
}
