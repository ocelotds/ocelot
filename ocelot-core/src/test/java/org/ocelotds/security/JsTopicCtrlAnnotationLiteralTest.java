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

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class JsTopicCtrlAnnotationLiteralTest {

	/**
	 * Test of value method, of class JsTopicCtrlAnnotationLiteral.
	 */
	@Test
	public void testValue() {
		System.out.println("value");
		String expResult = "VALUE";
		JsTopicCtrlAnnotationLiteral instance = new JsTopicCtrlAnnotationLiteral(expResult);
		String result = instance.value();
		assertThat(result).isEqualTo(expResult);
	}

}