/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.dashboard.services;

import java.util.ArrayList;
import java.util.Collection;
import javax.inject.Inject;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.dashboard.objects.SessionInfo;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionServicesTest {

	@InjectMocks
	@Spy
	SessionServices instance;
	
	@Mock
	HttpSessionManager httpSessionManager;
	
	@Mock
	MonitorSessionManager monitorSessionManager;


	/**
	 * Test of getSessionInfos method, of class SessionServices.
	 */
	@Test
	public void testGetSessionInfos() {
		System.out.println("getSessionInfos");
		Collection<SessionInfo> sessionInfos = new ArrayList<>();
		when(httpSessionManager.getSessionInfos()).thenReturn(sessionInfos);
		Collection<SessionInfo> result = instance.getSessionInfos();
		assertThat(result).isEqualTo(sessionInfos);
		verify(httpSessionManager).getSessionInfos();
	}

	/**
	 * Test of monitorSession method, of class SessionServices.
	 */
	@Test
	public void testMonitorSession() {
		System.out.println("monitorSession");
		String httpid = "ID";
		doNothing().when(monitorSessionManager).setMonitored(eq(httpid));
		instance.monitorSession(httpid);
		verify(monitorSessionManager).setMonitored(eq(httpid));
	}

	/**
	 * Test of unmonitorSession method, of class SessionServices.
	 */
	@Test
	public void testUnmonitorSession() {
		System.out.println("unmonitorSession");
		String httpid = "ID";
		doNothing().when(monitorSessionManager).unsetMonitored(eq(httpid));
		instance.unmonitorSession(httpid);
		verify(monitorSessionManager).unsetMonitored(eq(httpid));
	}

}