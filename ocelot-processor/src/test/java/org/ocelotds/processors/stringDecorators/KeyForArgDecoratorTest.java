/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.processors.stringDecorators;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class KeyForArgDecoratorTest {


	@InjectMocks
	private KeyForArgDecorator instance;

	/**
	 * Test of decorate method, of class KeyForArgDecorator.
	 */
	@Test
	public void testDecorate() {
		System.out.println("decorate");
		String result = instance.decorate("c");
		assertThat(result).isEqualTo("c");
		result = instance.decorate("c.user");
		assertThat(result).isEqualTo("(c)?c.user:null");
		result = instance.decorate("c.user.id");
		assertThat(result).isEqualTo("(c&&c.user)?c.user.id:null");
	}

}