/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.exceptions;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hhfrancois
 */
public class DataServiceNotFoundExceptionTest {
	private final static String MESSAGE = "MESSAGE";
	
	private final static DataServiceNotFoundException DSNFE = new DataServiceNotFoundException(MESSAGE);
	
	@Test
	public void testConstructor() {
		assertThat(DSNFE.getMessage()).isEqualTo(MESSAGE);
	}
	
	@Test(expected = DataServiceNotFoundException.class)
	public void testIsException() throws DataServiceNotFoundException {
		throw DSNFE;
	}
}
