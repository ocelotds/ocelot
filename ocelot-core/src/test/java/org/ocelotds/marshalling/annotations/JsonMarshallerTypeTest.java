/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.marshalling.annotations;

import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonMarshallerTypeTest {

	/**
	 * Test of values method, of class JsonMarshallerType.
	 */
	@Test
	public void testValues() {
		System.out.println("values");
		JsonMarshallerType[] result = JsonMarshallerType.values();
		assertThat(result).hasSize(3);
	}

	/**
	 * Test of valueOf method, of class JsonMarshallerType.
	 */
	@Test
	public void testValueOf() {
		System.out.println("valueOf");
		JsonMarshallerType result = JsonMarshallerType.valueOf("MAP");
		assertThat(result).isEqualTo(JsonMarshallerType.MAP);
		result = JsonMarshallerType.valueOf("SINGLE");
		assertThat(result).isEqualTo(JsonMarshallerType.SINGLE);
		result = JsonMarshallerType.valueOf("LIST");
		assertThat(result).isEqualTo(JsonMarshallerType.LIST);
	}

}