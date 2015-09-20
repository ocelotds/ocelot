/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.security;

import java.util.HashMap;
import java.util.Map;
import javax.interceptor.InvocationContext;
import javax.websocket.Session;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.util.Arrays;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class CallServiceInterceptorTest {

	@Mock
	private Logger logger;

	@InjectMocks
	private CallServiceInterceptor instance;

	/**
	 * Test of intercept method, of class CallServiceInterceptor.
	 */
	@Test
	public void testIntercept() throws Exception {
		System.out.println("intercept");
		Object expResult = "RESULT";

		InvocationContext ctx = mock(InvocationContext.class);
		Session session = mock(Session.class);
		final Object[] params = Arrays.array("", session);
		Map<String, Object> sessionProperties = new HashMap<>();
		sessionProperties.put(Constants.PRINCIPAL, null);
		sessionProperties.put(Constants.LOCALE, null);
		sessionProperties.put(Constants.SUBJECT, null);

		when(ctx.getParameters()).thenReturn(params);
		when(session.getUserProperties()).thenReturn(sessionProperties);
		when(ctx.proceed()).thenReturn(expResult);
		
		Object result = instance.intercept(ctx);
		assertThat(result).isEqualTo(expResult);
	}

}