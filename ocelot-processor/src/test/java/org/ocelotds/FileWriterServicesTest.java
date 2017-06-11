/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.frameworks.angularjs.BodyWriter;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class FileWriterServicesTest {

	/**
	 * Test of copyFileToClassesOutput method, of class FileWriterServices.
	 * @throws java.io.IOException
	 */
	@Test
	public void testCopyFileToClassesOutput() throws IOException {
		System.out.println("copyFileToClassesOutput");
		String path = "";
		String filename = "test.js";
		Messager messager = mock(Messager.class);
		Filer filer = mock(Filer.class);
		Writer writer = mock(Writer.class);
		BodyWriter bodyWriter = mock(BodyWriter.class);
		FileWriterServices instance = spy(new FileWriterServices(messager, filer));
		instance.bodyWriter = bodyWriter;

		doReturn(writer).doThrow(IOException.class).when(instance).getFileObjectWriterInClassOutput(eq(""), eq(filename));

		instance.copyResourceToClassesOutput(path, filename);
		instance.copyResourceToClassesOutput(path, filename);
		verify(bodyWriter).write(eq(writer), any(InputStream.class));
	}

	/**
	 * Test of copyFileToDir method, of class FileWriterServices.
	 * @throws java.io.IOException
	 */
	@Test
	public void testCopyFileToDir() throws IOException {
		System.out.println("copyFileToDir");
		String path = "";
		String filename = "test.js";
		Messager messager = mock(Messager.class);
		Filer filer = mock(Filer.class);
		Writer writer = mock(Writer.class);
		BodyWriter bodyWriter = mock(BodyWriter.class);
		FileWriterServices instance = spy(new FileWriterServices(messager, filer));
		instance.bodyWriter = bodyWriter;

		doReturn(writer).doThrow(IOException.class).when(instance).getFileObjectWriter(eq("DIR"), eq("org.ocelotds."+filename));

		instance.copyResourceToDir(path, filename, "DIR");
		instance.copyResourceToDir(path, filename, "DIR");
		verify(bodyWriter).write(eq(writer), any(InputStream.class));
	}

	/**
	 * Test of getFileObjectWriterInClassOutput method, of class FileWriterServices.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testGetFileObjectWriterInClassOutput() throws Exception {
		System.out.println("getFileObjectWriterInClassOutput");
		String pkg = "PKG";
		String name= "NAME";
		FileObject fileObject = mock(FileObject.class);
		Messager messager = mock(Messager.class);
		Filer filer = mock(Filer.class);
		Writer writer = mock(Writer.class);
		BodyWriter bodyWriter = mock(BodyWriter.class);
		FileWriterServices instance = spy(new FileWriterServices(messager, filer));
		instance.bodyWriter = bodyWriter;

		when(filer.createResource(eq(StandardLocation.CLASS_OUTPUT), eq(pkg), eq(name))).thenReturn(fileObject);
		when(fileObject.openWriter()).thenReturn(writer);
		Writer result = instance.getFileObjectWriterInClassOutput(pkg, name);
		assertThat(result).isEqualTo(writer);
	}

	/**
	 * Test of getFileObjectWriter method, of class FileWriterServices.
	 * @throws java.lang.Exception
	 */
	@Test
	public void testGetFileObjectWriter() throws Exception {
		System.out.println("getFileObjectWriter");
		String path = "/tmp/ocelot";
		String filename = "test.js";
		File file = new File(path, filename);
		if(file.exists()) {
			file.delete();
		}
		assertThat(file).doesNotExist();
		Messager messager = mock(Messager.class);
		Filer filer = mock(Filer.class);
		FileWriterServices instance = spy(new FileWriterServices(messager, filer));
		Writer result = instance.getFileObjectWriter(path, filename);
		assertThat(result).isNotNull();
		assertThat(file).exists();
		file.delete();
		file = new File(path);
		if(file.exists()) {
			file.delete();
		}
	}

}