/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.web.rest;

import java.io.InputStream;
import java.util.List;
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
public class RsJsPromiseTest {

	@InjectMocks
	@Spy
	RsJsPromise instance;

	/**
	 * Test of getStreams method, of class RsJsPromise.
	 */
	@Test
	public void testGetStreams() {
		System.out.println("getStreams");
		doNothing().when(instance).addStream(anyList(), anyString());
		List<InputStream> result = instance.getStreams();
		assertThat(result).isNotNull();
		verify(instance).addStream(anyList(), eq("/promiseFactory.js"));
	}
}