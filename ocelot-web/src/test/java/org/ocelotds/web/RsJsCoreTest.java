/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ocelotds.web;

import java.io.InputStream;
import java.util.List;
import javax.ws.rs.core.UriInfo;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class RsJsCoreTest {

	@InjectMocks
	@Spy
	RsJsCore instance;
	
	@Mock
	UriInfo context;

	/**
	 * Test of getStreams method, of class RsJsCore.
	 */
	@Test
	public void testGetStreams() {
		System.out.println("getStreams");
		doReturn("").when(instance).getJsFilename(anyString());
		doReturn("").when(instance).getJsCore();
		List<InputStream> result = instance.getStreams();
		verify(instance, times(2)).addStream(any(List.class), anyString());
		assertThat(result).isNotNull();
	}

	/**
	 * Test of getJsCore method, of class RsJsCore.
	 */
	@Test
	public void testGetJsCore() {
		System.out.println("getJsCore");
		when(context.getPath()).thenReturn("core.js").thenReturn("core.min.js");
		String result = instance.getJsCore();
		assertThat(result).isEqualTo(Constants.SLASH + Constants.OCELOT_CORE + Constants.JS);
		result = instance.getJsCore();
		assertThat(result).isEqualTo(Constants.SLASH + Constants.OCELOT_CORE_MIN + Constants.JS);
	}

}