/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.topic.topicAccess;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.annotations.JsTopicControl;
import org.ocelotds.annotations.JsTopicControls;
import org.ocelotds.security.JsTopicAccessController;
import org.ocelotds.security.JsTopicCtrlAnnotationLiteral;
import org.ocelotds.security.JsTopicCtrlsAnnotationLiteral;
import org.ocelotds.security.UserContext;
import org.ocelotds.topic.JsTopicControlsTools;
import org.ocelotds.topic.UserContextFactory;
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

	@Mock
	private JsTopicControlsTools jsTopicControlsTools;


	@Before
	public void init() {
		when(topicAccessController.select(any(Annotation.class))).thenReturn(null);
	}

	private static final Annotation DEFAULT_AT = new AnnotationLiteral<Default>() {
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Test of checkAccessTopic method, of class.
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void checkAccessTopicTestNoAccessController() throws IllegalAccessException {
		System.out.println("checkAccessTopic");
		UserContext userContext = mock(UserContext.class);
		doReturn(Boolean.FALSE).when(instance).checkAccessTopicGlobalAC(eq(userContext), eq(TOPIC1));
		doReturn(Boolean.FALSE).when(instance).checkAccessTopicFromJsTopicControl(eq(userContext), eq(TOPIC1));
		doReturn(Boolean.FALSE).when(instance).checkAccessTopicFromJsTopicControls(eq(userContext), eq(TOPIC1));
		instance.checkAccessTopic(userContext, TOPIC1);
		verify(logger).info(anyString(), eq(TOPIC1), any(), any(), eq(TOPIC1));
	}

	/**
	 * Test of checkAccessTopic method, of class.
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void checkAccessTopicTest() throws IllegalAccessException {
		System.out.println("checkAccessTopic");
		UserContext userContext = mock(UserContext.class);
		doReturn(Boolean.TRUE).when(instance).checkAccessTopicGlobalAC(eq(userContext), eq(TOPIC1));
		doReturn(Boolean.FALSE).when(instance).checkAccessTopicFromJsTopicControl(eq(userContext), eq(TOPIC1));
		doReturn(Boolean.FALSE).when(instance).checkAccessTopicFromJsTopicControls(eq(userContext), eq(TOPIC1));
		instance.checkAccessTopic(userContext, TOPIC1);
		doReturn(Boolean.FALSE).when(instance).checkAccessTopicGlobalAC(eq(userContext), eq(TOPIC1));
		doReturn(Boolean.TRUE).when(instance).checkAccessTopicFromJsTopicControl(eq(userContext), eq(TOPIC1));
		doReturn(Boolean.FALSE).when(instance).checkAccessTopicFromJsTopicControls(eq(userContext), eq(TOPIC1));
		instance.checkAccessTopic(userContext, TOPIC1);
		doReturn(Boolean.FALSE).when(instance).checkAccessTopicGlobalAC(eq(userContext), eq(TOPIC1));
		doReturn(Boolean.FALSE).when(instance).checkAccessTopicFromJsTopicControl(eq(userContext), eq(TOPIC1));
		doReturn(Boolean.TRUE).when(instance).checkAccessTopicFromJsTopicControls(eq(userContext), eq(TOPIC1));
		instance.checkAccessTopic(userContext, TOPIC1);
		verify(logger, never()).info(anyString(), any(), any());
	}

	/**
	 * Test of checkAccessTopic method, of class.
	 * @throws java.lang.IllegalAccessException
	 */
	@Test(expected = IllegalAccessException.class)
	public void checkAccessTopicTestFail1() throws IllegalAccessException {
		System.out.println("checkAccessTopicFail1");
		UserContext userContext = mock(UserContext.class);
		doThrow(IllegalAccessException.class).when(instance).checkAccessTopicGlobalAC(eq(userContext), eq(TOPIC1));
		doReturn(Boolean.FALSE).when(instance).checkAccessTopicFromJsTopicControl(eq(userContext), eq(TOPIC1));
		doReturn(Boolean.FALSE).when(instance).checkAccessTopicFromJsTopicControls(eq(userContext), eq(TOPIC1));
		instance.checkAccessTopic(userContext, TOPIC1);
	}

	/**
	 * Test of checkAccessTopic method, of class.
	 * @throws java.lang.IllegalAccessException
	 */
	@Test(expected = IllegalAccessException.class)
	public void checkAccessTopicTestFail2() throws IllegalAccessException {
		System.out.println("checkAccessTopicFail2");
		UserContext userContext = mock(UserContext.class);
		doReturn(Boolean.FALSE).when(instance).checkAccessTopicGlobalAC(eq(userContext), eq(TOPIC1));
		doThrow(IllegalAccessException.class).when(instance).checkAccessTopicFromJsTopicControl(eq(userContext), eq(TOPIC1));
		doReturn(Boolean.FALSE).when(instance).checkAccessTopicFromJsTopicControls(eq(userContext), eq(TOPIC1));
		instance.checkAccessTopic(userContext, TOPIC1);
	}

	/**
	 * Test of checkAccessTopic method, of class.
	 * @throws java.lang.IllegalAccessException
	 */
	@Test(expected = IllegalAccessException.class)
	public void checkAccessTopicTestFail3() throws IllegalAccessException {
		System.out.println("checkAccessTopicFail3");
		UserContext userContext = mock(UserContext.class);
		doReturn(Boolean.FALSE).when(instance).checkAccessTopicGlobalAC(eq(userContext), eq(TOPIC1));
		doReturn(Boolean.FALSE).when(instance).checkAccessTopicFromJsTopicControl(eq(userContext), eq(TOPIC1));
		doThrow(IllegalAccessException.class).when(instance).checkAccessTopicFromJsTopicControls(eq(userContext), eq(TOPIC1));
		instance.checkAccessTopic(userContext, TOPIC1);
	}
	
	/**
	 * Test of checkAccessTopicGlobalAC method, of class.
	 * @throws java.lang.IllegalAccessException
	 */
	@Test(expected = IllegalAccessException.class)
	public void checkAccessTopicGlobalACTest() throws IllegalAccessException {
		System.out.println("checkAccessTopicGlobalAC");
		UserContext userContext = mock(UserContext.class);
		Instance instances = mock(Instance.class);
		when(topicAccessController.select(eq(DEFAULT_AT))).thenReturn(instances);
		doReturn(Boolean.FALSE).doReturn(Boolean.TRUE).doThrow(IllegalAccessException.class).when(instance).checkAccessTopicFromControllers(eq(userContext), eq(TOPIC1), eq(instances));
		boolean result = instance.checkAccessTopicGlobalAC(userContext, TOPIC1);
		assertThat(result).isFalse();
		result = instance.checkAccessTopicGlobalAC(userContext, TOPIC1);
		assertThat(result).isTrue();
		instance.checkAccessTopicGlobalAC(userContext, TOPIC1);
	}
	
	/**
	 * Test of checkAccessTopicFromJsTopicControl method, of class.
	 * @throws java.lang.IllegalAccessException
	 */
	@Test(expected = IllegalAccessException.class)
	public void checkAccessTopicFromJsTopicControlTest() throws IllegalAccessException {
		System.out.println("checkAccessTopicFromJsTopicControl");
		UserContext userContext = mock(UserContext.class);
		Instance instances = mock(Instance.class);
		when(topicAccessController.select(eq(new JsTopicCtrlAnnotationLiteral(TOPIC1)))).thenReturn(instances);
		doReturn(Boolean.FALSE).doReturn(Boolean.TRUE).doThrow(IllegalAccessException.class).when(instance).checkAccessTopicFromControllers(eq(userContext), eq(TOPIC1), eq(instances));
		boolean result = instance.checkAccessTopicFromJsTopicControl(userContext, TOPIC1);
		assertThat(result).isFalse();
		result = instance.checkAccessTopicFromJsTopicControl(userContext, TOPIC1);
		assertThat(result).isTrue();
		instance.checkAccessTopicFromJsTopicControl(userContext, TOPIC1);
	}
	
	/**
	 * Test of checkAccessTopicFromJsTopicAccessControllers method, of class.
	 * @throws java.lang.IllegalAccessException
	 */
	@Test(expected = IllegalAccessException.class)
	public void checkAccessTopicFromJsTopicControlsTest() throws IllegalAccessException {
		System.out.println("checkAccessTopicFromJsTopicControls");
		UserContext userContext = mock(UserContext.class);
		Instance instances = mock(Instance.class);
		when(topicAccessController.select(eq(new JsTopicCtrlsAnnotationLiteral()))).thenReturn(instances);
		when(instances.isUnsatisfied()).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE);
		doReturn(Boolean.TRUE).doReturn(Boolean.FALSE).doThrow(IllegalAccessException.class).when(instance).checkAccessTopicFromJsTopicAccessControllers(eq(userContext), eq(TOPIC1), eq(instances));
		boolean result = instance.checkAccessTopicFromJsTopicControls(userContext, TOPIC1);
		assertThat(result).isFalse();
		result = instance.checkAccessTopicFromJsTopicControls(userContext, TOPIC1);
		assertThat(result).isTrue();
		result = instance.checkAccessTopicFromJsTopicControls(userContext, TOPIC1);
		assertThat(result).isFalse();
		instance.checkAccessTopicFromJsTopicControls(userContext, TOPIC1);
	}
	
	/**
	 * Test of checkAccessTopicFromJsTopicAccessControllers method, of class.
	 * @throws java.lang.IllegalAccessException
	 */
	@Test(expected = IllegalAccessException.class)
	public void checkAccessTopicFromJsTopicAccessControllersTest() throws IllegalAccessException {
		System.out.println("checkAccessTopicFromJsTopicAccessControllers");
		Collection<JsTopicAccessController> controllers = new ArrayList<>();
		JsTopicAccessController jtac0 = mock(JsTopicAccessController.class);
		JsTopicAccessController jtac1 = mock(JsTopicAccessController.class);
		JsTopicAccessController jtac2 = mock(JsTopicAccessController.class);
		UserContext userContext = mock(UserContext.class);
		doReturn(Boolean.FALSE).when(instance).checkAccessTopicFromController(eq(userContext), eq(TOPIC1), eq(jtac0));
		doReturn(Boolean.TRUE).doThrow(IllegalAccessException.class).when(instance).checkAccessTopicFromController(eq(userContext), eq(TOPIC1), eq(jtac1));
		boolean result = instance.checkAccessTopicFromJsTopicAccessControllers(userContext, TOPIC1, controllers);
		assertThat(result).isFalse();
		controllers.add(jtac0);
		controllers.add(jtac1);
		controllers.add(jtac2);
		instance.checkAccessTopicFromJsTopicAccessControllers(userContext, TOPIC1, controllers);
		instance.checkAccessTopicFromJsTopicAccessControllers(userContext, TOPIC1, controllers);
	}
	
	/**
	 * Test of checkAccessTopicFromController method, of class.
	 * @throws java.lang.IllegalAccessException
	 */
	@Test(expected = IllegalAccessException.class)
	public void checkAccessTopicFromControllerTest() throws IllegalAccessException {
		System.out.println("checkAccessTopicFromController");
		UserContext userContext = mock(UserContext.class);
		JsTopicAccessController jtac = mock(JsTopicAccessController.class);
		JsTopicControls jtcs = mock(JsTopicControls.class);
		JsTopicControl[] controls1 = new JsTopicControl[] {new JsTopicCtrlAnnotationLiteral(TOPIC2)};
		JsTopicControl[] controls2 = new JsTopicControl[] {new JsTopicCtrlAnnotationLiteral(TOPIC2), new JsTopicCtrlAnnotationLiteral(TOPIC1)};
		when(jtcs.value()).thenReturn(controls1);
		doReturn(jtcs).when(jsTopicControlsTools).getJsTopicControlsFromProxyClass(eq(jtac.getClass()));
		doReturn(Boolean.TRUE).doThrow(IllegalAccessException.class).when(instance).checkAccessTopicFromControllers(eq(userContext), eq(TOPIC1), any(Collection.class));
	
		boolean result = instance.checkAccessTopicFromController(userContext, TOPIC1, jtac);
		assertThat(result).isFalse();
		when(jtcs.value()).thenReturn(controls2);
		result = instance.checkAccessTopicFromController(userContext, TOPIC1, jtac);
		assertThat(result).isTrue();
		instance.checkAccessTopicFromController(userContext, TOPIC1, jtac);
	}
	
	
	/**
	 * Test of checkAccessTopicFromControllers method, of class.
	 * @throws java.lang.IllegalAccessException
	 */
	@Test(expected = IllegalAccessException.class)
	public void checkAccessTopicFromControllersTest() throws IllegalAccessException {
		System.out.println("checkAccessTopicFromControllers");
		UserContext userContext = mock(UserContext.class);
		JsTopicAccessController jtac1 = mock(JsTopicAccessController.class);
		Collection<JsTopicAccessController> jtacs = Arrays.asList(jtac1);
		doNothing().doThrow(IllegalAccessException.class).when(jtac1).checkAccess(eq(userContext), eq(TOPIC1));
		
		boolean result = instance.checkAccessTopicFromControllers(userContext, TOPIC1, null);
		assertThat(result).isFalse();
		result = instance.checkAccessTopicFromControllers(userContext, TOPIC1, Collections.EMPTY_LIST);
		assertThat(result).isFalse();
		result = instance.checkAccessTopicFromControllers(userContext, TOPIC1, jtacs);
		assertThat(result).isTrue();
		instance.checkAccessTopicFromControllers(userContext, TOPIC1, jtacs);
	}
}