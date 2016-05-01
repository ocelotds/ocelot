/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.topic.messageControl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.enterprise.inject.Instance;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.annotations.JsTopicControl;
import org.ocelotds.annotations.JsTopicControls;
import org.ocelotds.security.JsTopicCtrlAnnotationLiteral;
import org.ocelotds.security.JsTopicCtrlsAnnotationLiteral;
import org.ocelotds.security.JsTopicMessageController;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageControllerManagerTest {

	private final String TOPIC = "TOPIC";

	@Mock
	private Logger logger;

	@Mock
	MessageControllerCache messageControllerCache;

	@Mock
	Instance<JsTopicMessageController<?>> topicMessageController;

	@InjectMocks
	@Spy
	MessageControllerManager instance;

	/**
	 * Test of getJsTopicMessageController method, of class.
	 */
	@Test
	public void getDefaultJsTopicMessageControllerTest() {
		System.out.println("GetJsTopicMessageController");
		when(messageControllerCache.loadFromCache(eq(TOPIC))).thenReturn(null);
		doReturn(null).when(instance).getJsTopicMessageControllerFromJsTopicControl(eq(TOPIC));
		doReturn(null).when(instance).getJsTopicMessageControllerFromJsTopicControls(eq(TOPIC));
		JsTopicMessageController result = instance.getJsTopicMessageController(TOPIC);
		assertThat(result).isInstanceOf(DefaultJsTopicMessageController.class);
		verify(messageControllerCache).saveToCache(eq(TOPIC), eq(DefaultJsTopicMessageController.class));
	}

	/**
	 * Test of getJsTopicMessageController method, of class.
	 */
	@Test
	public void getJsTopicMessageControllerTestFromCache() {
		System.out.println("GetJsTopicMessageController");
		JsTopicMessageController jtmc = mock(JsTopicMessageController.class);
		when(messageControllerCache.loadFromCache(eq(TOPIC))).thenReturn(jtmc);
		JsTopicMessageController result = instance.getJsTopicMessageController(TOPIC);
		assertThat(result).isEqualTo(jtmc);
		verify(messageControllerCache, never()).saveToCache(eq(TOPIC), any(Class.class));
	}

	/**
	 * Test of getJsTopicMessageController method, of class.
	 */
	@Test
	public void getDefaultJsTopicMessageControllerTestFromJsTopicControl() {
		System.out.println("GetJsTopicMessageController");
		JsTopicMessageController jtmc = mock(JsTopicMessageController.class);
		when(messageControllerCache.loadFromCache(eq(TOPIC))).thenReturn(null);
		doReturn(jtmc).when(instance).getJsTopicMessageControllerFromJsTopicControl(eq(TOPIC));
		JsTopicMessageController result = instance.getJsTopicMessageController(TOPIC);
		assertThat(result).isEqualTo(jtmc);
		verify(messageControllerCache).saveToCache(eq(TOPIC), any(Class.class));
	}

	/**
	 * Test of getJsTopicMessageController method, of class.
	 */
	@Test
	public void getDefaultJsTopicMessageControllerTestFromJsTopicControls() {
		System.out.println("GetJsTopicMessageController");
		JsTopicMessageController jtmc = mock(JsTopicMessageController.class);
		when(messageControllerCache.loadFromCache(eq(TOPIC))).thenReturn(null);
		doReturn(null).when(instance).getJsTopicMessageControllerFromJsTopicControl(eq(TOPIC));
		doReturn(jtmc).when(instance).getJsTopicMessageControllerFromJsTopicControls(eq(TOPIC));
		JsTopicMessageController result = instance.getJsTopicMessageController(TOPIC);
		assertThat(result).isEqualTo(jtmc);
		verify(messageControllerCache).saveToCache(eq(TOPIC), any(Class.class));
	}
	
	/**
	 * Test of getJsTopicMessageControllerFromJsTopicControl method, of class.
	 */
	@Test
	public void getJsTopicMessageControllerFromJsTopicControlTest() {
		System.out.println("getJsTopicMessageControllerFromJsTopicControl");
		Instance instances = mock(Instance.class);
		JsTopicMessageController jtmc = mock(JsTopicMessageController.class);
		when(topicMessageController.select(any(JsTopicControl.class))).thenReturn(instances);
		when(instances.isUnsatisfied()).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE);
		when(instances.get()).thenReturn(jtmc);
		JsTopicMessageController result = instance.getJsTopicMessageControllerFromJsTopicControl(eq(TOPIC));
		assertThat(result).isNull();
		result = instance.getJsTopicMessageControllerFromJsTopicControl(eq(TOPIC));
		assertThat(result).isEqualTo(jtmc);
	}
	
	/**
	 * Test of getJsTopicMessageControllerFromJsTopicControls method, of class.
	 */
	@Test
	public void getJsTopicMessageControllerFromJsTopicControlsTest() {
		System.out.println("getJsTopicMessageControllerFromJsTopicControls");
		JsTopicMessageController jtmc = mock(JsTopicMessageController.class);
		Instance select = mock(Instance.class);

		when(topicMessageController.select(any(JsTopicCtrlsAnnotationLiteral.class))).thenReturn(select);
		when(select.isUnsatisfied()).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE);
		doReturn(jtmc).when(instance).getJsTopicMessageControllerFromIterable(eq(TOPIC), any(Instance.class));

		JsTopicMessageController result = instance.getJsTopicMessageControllerFromJsTopicControls(TOPIC);
		assertThat(result).isNull();

		result = instance.getJsTopicMessageControllerFromJsTopicControls(TOPIC);
		assertThat(result).isEqualTo(jtmc);
	}
	
	/**
	 * Test of getJsTopicMessageControllerFromIterable method, of class.
	 */
	@Test
	public void getJsTopicMessageControllerFromIterableTest() {
		System.out.println("getJsTopicMessageControllerFromIterable");
		Collection<JsTopicMessageController<?>> controllers = new ArrayList<>();
		JsTopicMessageController<?> jtmc = mock(JsTopicMessageController.class);
		JsTopicControls jtcs = mock(JsTopicControls.class);
		
		doReturn(jtcs).when(instance).getJsTopicControls(eq(jtmc));
		when(jtcs.value()).thenReturn(new JsTopicControl[]{})
				  .thenReturn(new JsTopicControl[]{new JsTopicCtrlAnnotationLiteral("FOO")})
				  .thenReturn(new JsTopicControl[]{new JsTopicCtrlAnnotationLiteral(TOPIC)});
		
		JsTopicMessageController result = instance.getJsTopicMessageControllerFromIterable(TOPIC, null);
		assertThat(result).isNull();
		result = instance.getJsTopicMessageControllerFromIterable(null, mock(Iterable.class));
		assertThat(result).isNull();
		result = instance.getJsTopicMessageControllerFromIterable(TOPIC, controllers);
		assertThat(result).isNull();

		controllers.add(jtmc);
		result = instance.getJsTopicMessageControllerFromIterable(TOPIC, controllers);
		assertThat(result).isNull();
		result = instance.getJsTopicMessageControllerFromIterable(TOPIC, controllers);
		assertThat(result).isNull();
		result = instance.getJsTopicMessageControllerFromIterable(TOPIC, controllers);
		assertThat(result).isEqualTo(jtmc);

	}
}