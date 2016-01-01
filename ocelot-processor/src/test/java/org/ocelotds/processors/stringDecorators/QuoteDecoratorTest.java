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
public class QuoteDecoratorTest {

	@InjectMocks
	private QuoteDecorator instance;

	/**
	 * Test of decorate method, of class QuoteDecorator.
	 */
	@Test
	public void testDecorate() {
		System.out.println("decorate");
		String result = instance.decorate("a");
		assertThat(result).isEqualTo("\"a\"");
		result = instance.decorate(null);
		assertThat(result).isEqualTo("\"null\"");
		result = instance.decorate("b");
		assertThat(result).isEqualTo("\"b\"");
	}

}