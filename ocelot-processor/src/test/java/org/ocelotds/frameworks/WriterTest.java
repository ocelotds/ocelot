/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.frameworks;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.anyString;
import org.ocelotds.processors.ProcessorConstants;

/**
 *
 * @author hhfrancois
 */
public class WriterTest implements ProcessorConstants {
	public static Writer getMockWriter() throws IOException {
		Writer writer = mock(Writer.class);
		when(writer.append(anyString())).thenReturn(writer);
		return writer;
	}
	
	public static List<String> testBraces(Writer writer) throws IOException {
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(writer, atLeastOnce()).append(captor.capture());
		List<String> allValues = captor.getAllValues();
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
		return allValues;
	}

	public static List<String> captureWrite(Writer writer) throws IOException {
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(writer, atLeastOnce()).write(captor.capture());
		List<String> allValues = captor.getAllValues();
		return allValues;
	}
}
