/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.core.services;

import java.security.Principal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.websocket.Session;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.ocelotds.context.ThreadLocalContextHolder;
import org.ocelotds.security.SubjectServices;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class CallServiceDecoratorTest {

	@Mock
	private Logger logger;

	@Mock
	private SubjectServices subjectServices;

	@Mock
	private CallService callSercice;
	
	@InjectMocks
	private CallServiceDecorator instance = new CallServiceDecoratorImpl();

	/**
	 * Test of decorrate method sendMessageToClient, of class CallServiceDecorator.
	 */
	@Test
	public void testDecorator() {
		System.out.println("Decorate sendMessageToClient");
		Session session = mock(Session.class);
		Map<String, Object> sessionProperties = new HashMap<>();
		Principal p = mock(Principal.class);
		Locale l = new Locale("fr", "FR");
		sessionProperties.put(Constants.PRINCIPAL, p);
		sessionProperties.put(Constants.LOCALE, l);
		sessionProperties.put(Constants.SECURITY_CONTEXT, null);

		when(session.getUserProperties()).thenReturn(sessionProperties);
		
		instance.sendMessageToClient(null, session);
		assertThat(ThreadLocalContextHolder.get(Constants.PRINCIPAL)).isEqualTo(p);
		assertThat(ThreadLocalContextHolder.get(Constants.LOCALE)).isEqualTo(l);
	}
	
	private static class CallServiceDecoratorImpl extends CallServiceDecorator {
		
	}

}