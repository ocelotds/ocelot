/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class KeyMakerTest {

	@Mock
	private Logger logger;

	@Spy
	@InjectMocks
	private KeyMaker instance;

	/**
	 * Test of getMd5 method, of class KeyMaker.
	 */
	@Test
	public void testGetMd5() {
		System.out.println("getMd5");
		String msg = "sentenceforhashing";
		String expResult = "f3c1cdbb34896be04a270098f6772e96";
		String result = instance.getMd5(msg);
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getMd5 method, of class CacheManager.
	 * @throws java.security.NoSuchAlgorithmException
	 * @throws java.io.UnsupportedEncodingException
	 */
	@Test
	public void testGetMd5InError() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		System.out.println("getMd5InError");
		when(instance.getMessageDigest()).thenThrow(UnsupportedEncodingException.class);
		String result = instance.getMd5("");
		assertThat(result).isNull();
	}

	/**
	 * Test of getMd5 method, of class CacheManager.
	 * @throws java.security.NoSuchAlgorithmException
	 * @throws java.io.UnsupportedEncodingException
	 */
	@Test
	public void testGetMd5InError2() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		System.out.println("getMd5InError2");
		when(instance.getMessageDigest()).thenThrow(NoSuchAlgorithmException.class);
		String result = instance.getMd5("");
		assertThat(result).isNull();
	}
}