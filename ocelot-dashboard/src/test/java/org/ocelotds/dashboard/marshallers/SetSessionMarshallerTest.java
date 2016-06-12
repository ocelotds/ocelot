/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.dashboard.marshallers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.Session;
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
public class SetSessionMarshallerTest {

	@InjectMocks
	@Spy
	SetSessionMarshaller instance;

	/**
	 * Test of toJson method, of class SetSessionMarshaller.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testToJson() throws Exception {
		System.out.println("toJson");
		Set<Session> objs = new HashSet<>();
		String result = instance.toJson(objs);
		assertThat(result).isEqualTo("[]");

		Session session1 = mock(Session.class);
		when(session1.getId()).thenReturn("ID1");
		objs.add(session1);
		result = instance.toJson(objs);
		assertThat(result).isEqualTo("[\"ID1\"]");

		Session session2 = mock(Session.class);
		when(session2.getId()).thenReturn("ID2");
		objs.add(session2);
		result = instance.toJson(objs);
		assertThat(result).isIn("[\"ID1\",\"ID2\"]", "[\"ID2\",\"ID1\"]");
	}

	/**
	 * Test of toJava method, of class SetSessionMarshaller.
	 */
	@Test
	public void testToJava() throws Exception {
		System.out.println("toJava");
		Set<Session> result = instance.toJava(null);
		assertThat(result).isEmpty();
	}

}