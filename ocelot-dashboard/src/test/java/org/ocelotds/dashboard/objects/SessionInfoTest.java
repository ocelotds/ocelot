/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.dashboard.objects;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionInfoTest {

	SessionInfo instance;
	
	@Before
	public void init() {
		instance = new SessionInfo("ID", "USERNAME", true);
	}

	/**
	 * Test of Constructor method, of class.
	 */
	@Test
	public void ConstructorTest() {
		System.out.println("Constructor");
		SessionInfo sessionInfo = new SessionInfo();
		assertThat(sessionInfo.getId()).isNull();
		assertThat(sessionInfo.getUsername()).isNull();
		assertThat(sessionInfo.isOpen()).isFalse();
	}
	
	/**
	 * Test of getId method, of class SessionInfo.
	 */
	@Test
	public void testGetId() {
		System.out.println("getId");
		String result = instance.getId();
		assertThat(result).isEqualTo("ID");
	}

	/**
	 * Test of setId method, of class SessionInfo.
	 */
	@Test
	public void testSetId() {
		System.out.println("setId");
		instance.setId("ID2");
		String result = instance.getId();
		assertThat(result).isEqualTo("ID2");
	}

	/**
	 * Test of getUsername method, of class SessionInfo.
	 */
	@Test
	public void testGetUsername() {
		System.out.println("getUsername");
		String result = instance.getUsername();
		assertThat(result).isEqualTo("USERNAME");
	}

	/**
	 * Test of setUsername method, of class SessionInfo.
	 */
	@Test
	public void testSetUsername() {
		System.out.println("setUsername");
		instance.setUsername("USERNAME2");
		String result = instance.getUsername();
		assertThat(result).isEqualTo("USERNAME2");
	}

	/**
	 * Test of isOpen method, of class SessionInfo.
	 */
	@Test
	public void testIsOpen() {
		System.out.println("isOpen");
		boolean result = instance.isOpen();
		assertThat(result).isTrue();
	}

	/**
	 * Test of setOpen method, of class SessionInfo.
	 */
	@Test
	public void testSetOpen() {
		System.out.println("setOpen");
		instance.setOpen(false);
		boolean result = instance.isOpen();
		assertThat(result).isFalse();
	}

}