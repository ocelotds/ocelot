/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.web.ws.predicates;

import javax.websocket.Session;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class IsOpenAndDifferentPredicateTest {

	/**
	 * Test of test method, of class IsOpenAndDifferentPredicate.
	 */
	@Test
	public void testTest() {
		System.out.println("test");
		IsOpenAndDifferentPredicate instance = new IsOpenAndDifferentPredicate("WSID");
		Session t = mock(Session.class);
		when(t.isOpen()).thenReturn(Boolean.FALSE, Boolean.TRUE);
		when(t.getId()).thenReturn("WSID2").thenReturn("WSID");;
		boolean result = instance.test(t);
		assertThat(result).isFalse();
		result = instance.test(t);
		assertThat(result).isTrue();
		result = instance.test(t);
		assertThat(result).isFalse();
	}

}