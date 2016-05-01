/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.topic;

import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.objects.FakeCDI;
import org.ocelotds.security.JsTopicMessageController;
import org.ocelotds.security.NotRecipientException;
import org.ocelotds.security.UserContext;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageControllerCacheTest {

	@InjectMocks
	@Spy
	MessageControllerCache instance;

	/**
	 * Test of saveToCache method, of class MessageControllerCache.
	 */
	@Test
	public void testSaveToCache() {
		System.out.println("saveToCache");
		String topic = "TOPIC";
		Class<? extends JsTopicMessageController> cls = JsTopicMessageController.class;
		instance.messageControllers.clear();
		instance.saveToCache(topic, null);
		instance.saveToCache(null, cls);
		instance.saveToCache(topic, cls);
		assertThat(instance.messageControllers).hasSize(1);
	}

	/**
	 * Test of loadFromCache method, of class MessageControllerCache.
	 */
	@Test
	public void testLoadFromCache() {
		System.out.println("loadFromCache");
		String topic = "TOPIC";
		instance.messageControllers.clear();
		instance.messageControllers.put(topic, JsTopicMessageController.class);
		FakeCDI<JsTopicMessageController> instances = new FakeCDI<>();
		doReturn(instances).when(instance).getInstances(any(Class.class));
		JsTopicMessageController result = instance.loadFromCache(topic);
		assertThat(result).isNull();
		instances.add(jtmc);
		result = instance.loadFromCache(topic);
		assertThat(result).isNotNull();
		result = instance.loadFromCache(null);
		assertThat(result).isNull();
		result = instance.loadFromCache("UNKNOWN");
		assertThat(result).isNull();
	}

	JsTopicMessageController jtmc = new JsTopicMessageController() {
		@Override
		public void checkRight(UserContext ctx, String topic, Object payload) throws NotRecipientException {
		}
	};

}
