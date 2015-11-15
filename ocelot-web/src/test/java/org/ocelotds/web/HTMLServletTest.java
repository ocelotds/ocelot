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
public class HTMLServletTest {

	private final HTMLServlet instance = new HTMLServlet();

	@Test
	public void testGetFilename() {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		ServletContext servletContext = mock(ServletContext.class);
		// when
		when(request.getServletContext()).thenReturn(servletContext);
		when(servletContext.getInitParameter(eq(Constants.OCELOT_HTML))).thenReturn("htmlfile");
		// then
		String result = instance.getFilename(request);
		assertThat(result).isEqualTo("htmlfile");
	}

	@Test
	public void testGetMimeType() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		String result = instance.getMimetype(request);
		assertThat(result).isEqualTo(Constants.HTMLTYPE);
	}
}