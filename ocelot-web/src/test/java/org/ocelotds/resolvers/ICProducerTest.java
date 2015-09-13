/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.resolvers;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class ICProducerTest {

	@InjectMocks
	private ICProducer instance;

	/**
	 * Test of getInitialContext method, of class ICProducer.
	 * 
	 * @throws NamingException 
	 */
	@Test
	public void testGetInitialContext() throws NamingException {
		System.out.println("getInitialContext");
		InitialContext result = instance.getInitialContext();
		assertThat(result).isNotNull();
	}

}