/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ocelotds.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.Response;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractRsJsTest {

	@InjectMocks
	@Spy
	AbstractRsJsImpl instance = new AbstractRsJsImpl();
	
	@Mock
	Logger logger;

	/**
	 * Test of getStreams method, of class AbstractRsJs.
	 */
	@Test
	public void testGetStreams() {
		System.out.println("getStreams");
		List<InputStream> result = instance.getStreams();
		assertThat(result).isNotNull();
	}

	/**
	 * Test of getResource method, of class AbstractRsJs.
	 */
	@Test
	public void testGetResource() {
		System.out.println("getResource");
		String name = "/test.js";
		URL result = instance.getResource(name);
		assertThat(result).isNotNull();
	}

	/**
	 * Test of getJsFilename method, of class AbstractRsJs.
	 */
	@Test
	public void testGetJsFilename() {
		System.out.println("getJsFilename");
		String classname = "";
		String expResult = "";
		String result = instance.getJsFilename(classname);
		assertThat(result);
	}

	/**
	 * Test of addStream method, of class AbstractRsJs.
	 * @throws java.io.IOException
	 */
	@Test
	public void testAddStream() throws IOException {
		System.out.println("addStream");
		List<InputStream> streams = new ArrayList<>();
		String filename = "/test.js";
		instance.addStream(streams, filename);
		assertThat(streams).hasSize(1);

		instance.addStream(streams, "unknown.js");
		assertThat(streams).hasSize(1);
	}

	/**
	 * Test of getSequenceInputStream method, of class AbstractRsJs.
	 */
	@Test
	public void testGetSequenceInputStream() {
		System.out.println("getSequenceInputStream");
		InputStream stream1 = new ByteArrayInputStream("body1".getBytes());
		InputStream stream2 = new ByteArrayInputStream("body2".getBytes());
		InputStream expresult = new ByteArrayInputStream("body1body2".getBytes());
		List<InputStream> streams = Arrays.asList(stream1, stream2);
		SequenceInputStream result = instance.getSequenceInputStream(streams);
		assertThat(result).hasSameContentAs(expresult);
	}
	
	class AbstractRsJsImpl extends AbstractRsJs{

		@Override
		List<InputStream> getStreams() {
			return new ArrayList<>();
		}
	}
}