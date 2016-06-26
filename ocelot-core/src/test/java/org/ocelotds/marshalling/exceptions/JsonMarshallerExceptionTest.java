/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.marshalling.exceptions;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonMarshallerExceptionTest {

	@InjectMocks
	@Spy
	JsonMarshallerException instance = new JsonMarshallerException("MESSAGE");

	@Test
	public void testSomeMethod() {
		String msg = instance.getMessage();
		assertThat(msg).isEqualTo("MESSAGE");
	}

}