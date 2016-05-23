/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.topic;

import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.ocelotds.annotations.JsTopicControls;
import org.ocelotds.core.UnProxyClassServices;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class JsTopicControlsToolsTest {

	@InjectMocks
	@Spy
	JsTopicControlsTools instance;

	@Mock
	UnProxyClassServices unProxyClassServices;

	/**
	 * Test of getJsTopicControlsFromProxyClass method, of class JsTopicControlsTools.
	 */
	@Test
	public void testGetJsTopicControlsFromProxyClass() {
		System.out.println("getJsTopicControlsFromProxyClass");
		Class proxy = String.class;
		when(unProxyClassServices.getRealClass(eq(String.class))).thenReturn((Class) String.class).thenReturn((Class) RealClass.class);
		JsTopicControls result = instance.getJsTopicControlsFromProxyClass(proxy);
		assertThat(result).isNull();
		result = instance.getJsTopicControlsFromProxyClass(proxy);
		assertThat(result).isNotNull();
	}
	@JsTopicControls()
	static class RealClass {
	
	}

}