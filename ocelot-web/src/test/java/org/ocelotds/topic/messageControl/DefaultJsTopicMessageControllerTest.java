/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.topic.messageControl;

import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.security.UserContext;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultJsTopicMessageControllerTest {

	@InjectMocks
	@Spy
	DefaultJsTopicMessageController instance;

	/**
	 * Test of checkRight method, of class DefaultJsTopicMessageController.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testCheckRight() throws Exception {
		System.out.println("checkRight");
		UserContext ctx = null;
		String topic = "";
		Object payload = null;
		instance.checkRight(ctx, topic, payload);
	}

}