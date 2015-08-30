/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core;

import java.lang.reflect.Method;
import javax.websocket.Session;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.spi.IDataServiceResolver;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author hhfrancois
 */
public class CallServiceManagerTest {
	
	public CallServiceManagerTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}

	/**
	 * Test of getResolver method, of class CallServiceManager.
	 */
//	@Test
	public void testGetResolver() {
		System.out.println("getResolver");
		String type = "";
		CallServiceManager instance = new CallServiceManager();
		IDataServiceResolver expResult = null;
		IDataServiceResolver result = instance.getResolver(type);
	}

	/**
	 * Test of getMethodFromDataService method, of class CallServiceManager.
	 */
//	@Test
	public void testGetMethodFromDataService() {
		System.out.println("getMethodFromDataService");
		Class dsClass = null;
		MessageFromClient message = null;
		Object[] arguments = null;
		CallServiceManager instance = new CallServiceManager();
		Method expResult = null;
		Method result = instance.getMethodFromDataService(dsClass, message, arguments);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}

	/**
	 * Test of getMethodFromDataServiceWithSessionInjection method, of class CallServiceManager.
	 */
//	@Test
	public void testGetMethodFromDataServiceWithSessionInjection() {
		System.out.println("getMethodFromDataServiceWithSessionInjection");
		Session session = null;
		Class dsClass = null;
		MessageFromClient message = null;
		Object[] arguments = null;
		CallServiceManager instance = new CallServiceManager();
		Method expResult = null;
		Method result = instance.getMethodFromDataServiceWithSessionInjection(session, dsClass, message, arguments);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}

	/**
	 * Test of getDataService method, of class CallServiceManager.
	 */
//	@Test
	public void testGetDataService() throws Exception {
		System.out.println("getDataService");
		Session client = null;
		Class cls = null;
		CallServiceManager instance = new CallServiceManager();
		Object expResult = null;
		Object result = instance.getDataService(client, cls);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}

	/**
	 * Test of sendMessageToClient method, of class CallServiceManager.
	 */
//	@Test
	public void testSendMessageToClient() {
		System.out.println("sendMessageToClient");
		MessageFromClient message = null;
		Session client = null;
		CallServiceManager instance = new CallServiceManager();
		instance.sendMessageToClient(message, client);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}
}
