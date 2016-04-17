/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ocelotds.security;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.annotations.JsTopicControl;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class JsTopicCtrlsAnnotationLiteralTest {

	/**
	 * Test of value method, of class JsTopicCtrlsAnnotationLiteral.
	 */
	@Test
	public void testValue() {
		System.out.println("value");
		JsTopicCtrlsAnnotationLiteral instance = new JsTopicCtrlsAnnotationLiteral();
		JsTopicControl[] result = instance.value();
		assertThat(result).isEmpty();
	}

}