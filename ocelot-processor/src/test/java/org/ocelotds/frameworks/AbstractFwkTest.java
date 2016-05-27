/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.frameworks;

import java.io.Writer;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.mockito.ArgumentCaptor;
import org.ocelotds.processors.ProcessorConstants;

/**
 *
 * @author hhfrancois
 */
public abstract class AbstractFwkTest implements ProcessorConstants {

	abstract FwkWriter getInstance();

	/**
	 * Test of writeHeaderService method, of class AngularFwk.
	 */
	public void testWriteHeaderFooterService() throws Exception {
		System.out.println("writeHeaderService");
		Writer writer = mock(Writer.class);
		when(writer.append(anyString())).thenReturn(writer);
		getInstance().writeHeaderService(writer, "servicename");
		getInstance().writeFooterService(writer);
		ArgumentCaptor<String> appendCapture = ArgumentCaptor.forClass(String.class);
		verify(writer, atLeastOnce()).append(appendCapture.capture());
		List<String> allValues = appendCapture.getAllValues();
		int countOpenBrace = 0;
		int countCloseBrace = 0;
		int countOpenParenthesis = 0;
		int countCloseParenthesis = 0;
		for (String value : allValues) {
			switch (value) {
				case OPENBRACE:
					countOpenBrace++;
					break;
				case CLOSEBRACE:
					countCloseBrace++;
					break;
				case OPENPARENTHESIS:
					countOpenParenthesis++;
					break;
				case CLOSEPARENTHESIS:
					countCloseParenthesis++;
					break;
			}
		}
		assertThat(countOpenBrace).isEqualTo(countCloseBrace);
		assertThat(countOpenParenthesis).isEqualTo(countCloseParenthesis);
	}
}