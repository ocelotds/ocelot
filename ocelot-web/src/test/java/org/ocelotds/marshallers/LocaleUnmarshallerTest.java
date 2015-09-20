/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.marshallers;

import java.util.Locale;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class LocaleUnmarshallerTest {

	@Mock
	private Logger logger;

	@InjectMocks
	private LocaleUnmarshaller instance;

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
	 * Test of toJava method, of class LocaleUnmarshaller.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testToJava() throws Exception {
		System.out.println("toJava");
		String json = "{\"language\":\"fr\",\"country\":\"FR\"}";
		Locale expResult = new Locale("FR", "fr");
		Locale result = instance.toJava(json);
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of toJava method, of class LocaleUnmarshaller.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testToNullJava() throws Exception {
		System.out.println("toJava");
		String json = null;
		Locale result = instance.toJava(json);
		assertThat(result).isNull();
	}
}