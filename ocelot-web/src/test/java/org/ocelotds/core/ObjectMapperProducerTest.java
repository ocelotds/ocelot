/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hhfrancois
 */
public class ObjectMapperProducerTest {

	@Test
	public void testObjectMapperProducer() {
		ObjectMapperProducer instance = new ObjectMapperProducer();
		ObjectMapper result = instance.objectMapper;
		assertThat(result).isNotNull();
	}

}