/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.objects;

import java.util.List;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hhfrancois
 */
public class OcelotMethodTest {

	private OcelotMethod instance = new OcelotMethod("methodName", "java.lang.String");

	/**
	 * Test of getName method, of class OcelotMethod.
	 */
	@Test
	public void testGetName() {
		System.out.println("getName");
		String expResult = "methodName";
		String result = instance.getName();
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getReturntype method, of class OcelotMethod.
	 */
	@Test
	public void testGetReturntype() {
		System.out.println("getReturntype");
		String expResult = "java.lang.String";
		String result = instance.getReturntype();
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getArgtypes method, of class OcelotMethod.
	 */
	@Test
	public void testGetArgtypes() {
		System.out.println("getArgtypes");
		List<String> result = instance.getArgtypes();
		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
	}

	/**
	 * Test of getArgnames method, of class OcelotMethod.
	 */
	@Test
	public void testGetArgnames() {
		System.out.println("getArgnames");
		List<String> result = instance.getArgnames();
		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
	}

	/**
	 * Test of getArgtemplates method, of class OcelotMethod.
	 */
	@Test
	public void testGetArgtemplates() {
		System.out.println("getArgtemplates");
		List<String> result = instance.getArgtemplates();
		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
	}

}