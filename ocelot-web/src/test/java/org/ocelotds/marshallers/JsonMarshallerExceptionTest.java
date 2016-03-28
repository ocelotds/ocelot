/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ocelotds.marshallers;

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
public class JsonMarshallerExceptionTest {

	@InjectMocks
	@Spy
	JsonMarshallerException instance = new JsonMarshallerException("MESSAGE");

	@Test
	public void testSomeMethod() {
		String msg = instance.getMessage();
		assertThat(msg).isEqualTo("MESSAGE");
	}

}