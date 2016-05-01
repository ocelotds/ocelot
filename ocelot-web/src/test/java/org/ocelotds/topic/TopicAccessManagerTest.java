/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.topic;

import java.lang.annotation.Annotation;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.websocket.Session;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.objects.FakeCDI;
import org.ocelotds.security.JsTopicAccessController;
import org.ocelotds.security.JsTopicCtrlAnnotationLiteral;
import org.ocelotds.security.UserContext;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class TopicAccessManagerTest {

	private static final String TOPIC1 = "TOPIC_1";
	private static final String TOPIC2 = "TOPIC_2";
	private static final String SUBTOPIC2 = "subscribers:TOPIC_2";

	@InjectMocks
	@Spy
	TopicAccessManager instance;

	@Mock
	private Logger logger;
 
	@Mock
	Instance<JsTopicAccessController> topicAccessController;
	
	@Mock
	private UserContextFactory userContextFactory;


	@Before
	public void init() {
		when(topicAccessController.select(any(Annotation.class))).thenReturn(null);
	}

	private static final Annotation DEFAULT_AT = new AnnotationLiteral<Default>() {
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Test of checkAccessTopic method, of class TopicManager.
	 *
	 * @throws java.lang.IllegalAccessException
	 */
	@Test(expected = IllegalAccessException.class)
	public void testCheckAccessGlobalTopic() throws IllegalAccessException {
		FakeCDI<JsTopicAccessController> globalTAC = new FakeCDI();
		JsTopicAccessController jtac = mock(JsTopicAccessController.class);
		globalTAC.add(jtac);
		doThrow(IllegalAccessException.class).when(jtac).checkAccess(any(UserContext.class), anyString());
		when(topicAccessController.select(DEFAULT_AT)).thenReturn(globalTAC);
		Session session = mock(Session.class);
		instance.checkAccessTopic(session, TOPIC1);
	}

	/**
	 * Test of checkAccessTopic method, of class TopicManager.
	 *
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testCheckAccessOkGlobalTopic() throws IllegalAccessException {
		FakeCDI<JsTopicAccessController> globalTAC = new FakeCDI();
		JsTopicAccessController jtac = mock(JsTopicAccessController.class);
		globalTAC.add(jtac);
		doNothing().when(jtac).checkAccess(any(UserContext.class), anyString());
		when(topicAccessController.select(DEFAULT_AT)).thenReturn(globalTAC);
		Session session = mock(Session.class);
		instance.checkAccessTopic(session, TOPIC1);
	}

	/**
	 * Test of checkAccessTopic method, of class TopicManager.
	 *
	 * @throws java.lang.IllegalAccessException
	 */
	@Test(expected = IllegalAccessException.class)
	public void testCheckAccessSpecificTopic() throws IllegalAccessException {
		FakeCDI<JsTopicAccessController> topic1TAC = new FakeCDI();
		JsTopicAccessController jtac = mock(JsTopicAccessController.class);
		topic1TAC.add(jtac);
		doThrow(IllegalAccessException.class).when(jtac).checkAccess(any(UserContext.class), eq(TOPIC1));
		when(topicAccessController.select(new JsTopicCtrlAnnotationLiteral(TOPIC1))).thenReturn(topic1TAC);
		Session session = mock(Session.class);
		try {
			instance.checkAccessTopic(session, TOPIC2);
		} catch (IllegalAccessException e) {
			fail("Topic2 should be ok");
		}
		instance.checkAccessTopic(session, TOPIC1);
	}

	/**
	 * Test of checkAccessTopic method, of class TopicManager.
	 *
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testCheckAccessOkSpecificTopic() throws IllegalAccessException {
		FakeCDI<JsTopicAccessController> topic1TAC = new FakeCDI();
		JsTopicAccessController jtac = mock(JsTopicAccessController.class);
		topic1TAC.add(jtac);
		doNothing().when(jtac).checkAccess(any(UserContext.class), eq(TOPIC1));
		when(topicAccessController.select(new JsTopicCtrlAnnotationLiteral(TOPIC1))).thenReturn(topic1TAC);
		Session session = mock(Session.class);
		instance.checkAccessTopic(session, TOPIC1);
	}

}