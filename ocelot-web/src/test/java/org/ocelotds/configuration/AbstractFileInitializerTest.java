/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.configuration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractFileInitializerTest {

	private AbstractFileInitializer instance = new AbstractFileInitializerImpl();

	/**
	 * Test of deleteFile method, of class AbstractFileInitializer.
	 */
	@Test
	public void testDeleteFile() throws IOException {
		System.out.println("deleteFile");
		File f = File.createTempFile("test", "tmp");
		String filename = f.getAbsolutePath();
		boolean result = instance.deleteFile("unknownfile");
		assertThat(result).isEqualTo(false);
		
		result = instance.deleteFile(filename);		
		assertThat(result).isEqualTo(true);
		assertThat(f).doesNotExist();
	}

	/**
	 * Test of getContentURL method, of class AbstractFileInitializer.
	 */
	@Test
	public void testGetContentURL() {
		System.out.println("getContentURL");
		URL result = instance.getContentURL(Constants.SLASH + Constants.OCELOT_CORE + Constants.JS);
		assertThat(result).isNotNull();
	}
	
	final class AbstractFileInitializerImpl extends AbstractFileInitializer {
		
	}
}