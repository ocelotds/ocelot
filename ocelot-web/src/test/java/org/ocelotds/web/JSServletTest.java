/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.ocelotds.Constants;

/**
 *
 * @author hhfrancois
 */
public class JSServletTest {

	private final JSServlet instance = new JSServlet();
	
	@Test
	public void testGetFilename() {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		ServletContext servletContext = mock(ServletContext.class);
		// when
		when(request.getParameter(Constants.MINIFY_PARAMETER))
				  .thenReturn(null).thenReturn("true").thenReturn("TRUE")
				  .thenReturn("false").thenReturn("FALSE");
		when(request.getServletContext()).thenReturn(servletContext);
		when(servletContext.getInitParameter(eq(Constants.OCELOT_MIN))).thenReturn("minfile");
		when(servletContext.getInitParameter(eq(Constants.OCELOT))).thenReturn("maxfile");
		// then
		String result = instance.getFilename(request);
		assertThat(result).isEqualTo("minfile");

		result = instance.getFilename(request);
		assertThat(result).isEqualTo("minfile");

		result = instance.getFilename(request);
		assertThat(result).isEqualTo("minfile");

		result = instance.getFilename(request);
		assertThat(result).isEqualTo("maxfile");

		result = instance.getFilename(request);
		assertThat(result).isEqualTo("maxfile");
	}

	@Test
	public void testGetMimeType() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		String result = instance.getMimetype(request);
		assertThat(result).isEqualTo(Constants.JSTYPE);
	}
}