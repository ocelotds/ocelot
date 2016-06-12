/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.dashboard.services;

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
public class MonitorSessionManagerTest {

	@InjectMocks
	@Spy
	MonitorSessionManager instance;

	/**
	 * Test of setMonitored method, of class MonitorSessionManager.
	 */
	@Test
	public void testSetMonitored() {
		System.out.println("setMonitored");
		String httpid = "NO";
		boolean monitored = instance.isMonitored(httpid);
		assertThat(monitored).isFalse();
		monitored = instance.isMonitored(null);
		assertThat(monitored).isFalse();
		httpid = "YES";
		instance.setMonitored(httpid);
		monitored = instance.isMonitored(httpid);
		assertThat(monitored).isTrue();
		instance.unsetMonitored(httpid);
		monitored = instance.isMonitored(httpid);
		assertThat(monitored).isFalse();
	}
}