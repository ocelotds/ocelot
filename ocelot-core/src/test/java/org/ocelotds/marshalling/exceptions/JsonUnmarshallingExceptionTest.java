/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.marshalling.exceptions;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hhfrancois
 */
public class JsonUnmarshallingExceptionTest {

	@Test
	public void testConstructor() {
		JsonUnmarshallingException instance = new JsonUnmarshallingException("MSG");
		assertThat(instance.getMessage()).isEqualTo("MSG");
	}

}