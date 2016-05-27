/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.processors;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.annotations.DataService;
import org.ocelotds.frameworks.Frameworks;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class OcelotProcessorTest {

	@Before
	public void setUp() {
		when(processingEnv.getFiler()).thenReturn(filer);
		when(processingEnv.getMessager()).thenReturn(messager);
		Map<String, String> map = new HashMap<>();
		map.put("jsdir", "/home/services");
		when(processingEnv.getOptions()).thenReturn(map);
		doReturn("").when(instance).getPromiseCreatorScript();
		instance.init(processingEnv);
		OcelotProcessor.setDone(false);
	}

	@Mock
	private Logger logger;

	@InjectMocks
	@Spy
	private OcelotProcessor instance;

	@Mock
	private ProcessingEnvironment processingEnv;

	@Mock
	private Filer filer;

	@Mock
	private Messager messager;

	/**
	 * Test of getJsDirectory method, of class.
	 */
	@Test
	public void test_getJsDirectory() {
		System.out.println("getJsDirectory");
		Map<String, String> map = new HashMap<>();
		Object result = instance.getJsDirectory(map);
		assertThat(result).isNull();
		String path = "/home/services";
		map.put(ProcessorConstants.DIRECTORY, path);
		result = instance.getJsDirectory(map);
		assertThat(result).isEqualTo(path);
		map.put(ProcessorConstants.DIRECTORY, null);
		result = instance.getJsDirectory(map);
		assertThat(result).isNull();
		result = instance.getJsDirectory(null);
		assertThat(result).isNull();
	}

	/**
	 * Test of getJsFramework method, of class.
	 */
	@Test
	public void test_getJsFramework() {
		System.out.println("getJsDirectory");
		Map<String, String> map = new HashMap<>();
		map.put(ProcessorConstants.FRAMEWORK, "ANGULARJS");
		Frameworks result = instance.getJsFramework(map);
		assertThat(result).isEqualTo(Frameworks.ANGULARJS);

		map.put(ProcessorConstants.FRAMEWORK, "BAD");
		result = instance.getJsFramework(map);
		assertThat(result).isEqualTo(Frameworks.NOFWK);

		map.put(ProcessorConstants.FRAMEWORK, null);
		result = instance.getJsFramework(map);
		assertThat(result).isEqualTo(Frameworks.NOFWK);

		result = instance.getJsFramework(null);
		assertThat(result).isEqualTo(Frameworks.NOFWK);
	}

	/**
	 * Test of isDone method, of class OcelotProcessor.
	 */
	@Test
	public void testIsDone() {
		System.out.println("isDone");
		boolean expResult = Boolean.FALSE;
		boolean result = OcelotProcessor.isDone();
		assertThat(result).isEqualTo(expResult);
		expResult = Boolean.TRUE;
		OcelotProcessor.setDone(expResult);
		result = OcelotProcessor.isDone();
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of process method, of class OcelotProcessor.
	 */
	@Test
	public void testProcessIsDone() {
		System.out.println("processingOver is true/false");
		Set<? extends TypeElement> annotations = mock(Set.class);
		RoundEnvironment roundEnv = mock(RoundEnvironment.class);
		when(roundEnv.processingOver()).thenReturn(Boolean.TRUE);
		OcelotProcessor.setDone(Boolean.FALSE);
		boolean result = instance.process(annotations, roundEnv);
		assertThat(result).isTrue();
		when(roundEnv.processingOver()).thenReturn(Boolean.FALSE);
		OcelotProcessor.setDone(Boolean.TRUE);
		result = instance.process(annotations, roundEnv);
		assertThat(result).isTrue();
	}

	/**
	 * Test of process method, of class OcelotProcessor.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testProcess() throws IOException {
		System.out.println("process");
		OcelotProcessor.setDone(Boolean.FALSE);
		RoundEnvironment roundEnv = mock(RoundEnvironment.class);
		Set<? extends TypeElement> annotations = mock(Set.class);
		Set elements = new HashSet<>();
		elements.add(mock(Element.class));
		elements.add(mock(Element.class));

		doReturn("").when(instance).getPromiseCreatorScript();
		when(roundEnv.processingOver()).thenReturn(Boolean.FALSE);
		when(roundEnv.getElementsAnnotatedWith(eq(DataService.class))).thenReturn(elements);

		boolean result = instance.process(annotations, roundEnv);
		assertThat(result).isTrue();
		verify(instance, times(2)).processElement(any(Element.class));
	}

	/**
	 * Test of processElement method, of class.
	 */
	@Test
	public void test_processElement() {
		System.out.println("processElement");
		doNothing().when(instance).processTypeElement(any(TypeElement.class));
		instance.processElement(mock(Element.class));
		instance.processElement(mock(TypeElement.class));
		verify(instance).processTypeElement(any(TypeElement.class));
	}

	/**
	 * Test of processTypeElement method, of class.
	 */
	@Test
	public void test_processTypeElement() {
		System.out.println("processTypeElement");
		doReturn("").when(instance).getPackagePath(any(TypeElement.class));
		doReturn("").when(instance).getFilename(any(TypeElement.class));
		doNothing().when(instance).writeJsFileToJsDir(any(TypeElement.class), any(ElementVisitor.class), anyString(), anyString(), anyString());
		doNothing().when(instance).writeJsFile(any(TypeElement.class), any(ElementVisitor.class), anyString(), anyString());
		instance.processTypeElement(mock(TypeElement.class));
		verify(instance).writeJsFileToJsDir(any(TypeElement.class), any(ElementVisitor.class), anyString(), anyString(), anyString());
		verify(instance).writeJsFile(any(TypeElement.class), any(ElementVisitor.class), anyString(), anyString());
	}

	/**
	 * Test of writeJsFile method, of class.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void test_writeJsFile() throws IOException {
		System.out.println("writeJsFile");
		TypeElement element = mock(TypeElement.class);
		doThrow(IOException.class).doReturn(mock(Writer.class)).when(instance).getResourceFileObjectWriter(anyString(), anyString());
		instance.writeJsFile(element, mock(ElementVisitor.class), "", "");
		instance.writeJsFile(element, mock(ElementVisitor.class), "", "");
		instance.writeJsFile(element, mock(ElementVisitor.class), "", "");
		verify(element, times(2)).accept(any(ElementVisitor.class), any(Writer.class));
	}

	/**
	 * Test of writeJsFileToJsDir method, of class.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void test_writeJsFileToJsDir() throws IOException {
		System.out.println("writeJsFileToJsDir");
		TypeElement element = mock(TypeElement.class);
		doThrow(IOException.class).doReturn(mock(Writer.class)).when(instance).getFileObjectWriter(anyString(), anyString(), anyString());
		instance.writeJsFileToJsDir(element, mock(ElementVisitor.class), "", "", "");
		instance.writeJsFileToJsDir(element, mock(ElementVisitor.class), "", "", "");
		instance.writeJsFileToJsDir(element, mock(ElementVisitor.class), "", "", null);
		verify(element).accept(any(ElementVisitor.class), any(Writer.class));
	}

	/**
	 * Test of getPackagePath method, of class.
	 */
	@Test
	public void test_getPackagePath() {
		System.out.println("getPackagePath");
		TypeElement element = mock(TypeElement.class);
		Name qname = mock(Name.class);
		when(qname.toString()).thenReturn("a.b.c.d.Test");
		Name name = mock(Name.class);
		when(name.toString()).thenReturn("Test");
		when(element.getQualifiedName()).thenReturn(qname);
		when(element.getSimpleName()).thenReturn(name);
		String result = instance.getPackagePath(element);
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo("a.b.c.d");
	}

	/**
	 * Test of getFilename method, of class.
	 */
	@Test
	public void test_getFilename() {
		System.out.println("getFilename");
		TypeElement element = mock(TypeElement.class);
		Name name = mock(Name.class);

		when(name.toString()).thenReturn("Test");
		when(element.getSimpleName()).thenReturn(name);

		String result = instance.getFilename(element);
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo("Test.js");
	}

	/**
	 * Test of getSourceFileObjectWriter method, of class.
	 */
	@Test
	public void test_getSourceFileObjectWriter() throws IOException {
		System.out.println("getSourceFileObjectWriter");
		Writer writer = mock(Writer.class);
		JavaFileObject source = mock(JavaFileObject.class);
		when(source.openWriter()).thenReturn(writer);
		when(filer.createSourceFile(anyString())).thenReturn(source);
		Writer result = instance.getSourceFileObjectWriter("");
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(writer);
	}

	/**
	 * Test of getResourceFileObjectWriter method, of class.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void test_getResourceFileObjectWriter() throws IOException {
		System.out.println("getResourceFileObjectWriter");
		Writer writer = mock(Writer.class);
		JavaFileObject source = mock(JavaFileObject.class);
		when(source.openWriter()).thenReturn(writer);
		when(filer.createResource(any(StandardLocation.class), anyString(), anyString())).thenReturn(source);
		Writer result = instance.getResourceFileObjectWriter("", "");
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(writer);
	}

	/**
	 * Test of getFileObjectWriter method, of class.
	 * @throws java.io.IOException
	 */
	@Test
	public void test_getFileObjectWriter() throws IOException {
		System.out.println("getFileObjectWriter");
		Writer result = instance.getFileObjectWriter("a.b", "File.js", "/tmp");
		assertThat(result).isNotNull();
		File file = new File("/tmp/a.b.File.js");
		assertThat(file).exists();
		file.delete();

		result = instance.getFileObjectWriter("a.b", "File.js", "/tmp/sub");
		assertThat(result).isNotNull();
		file = new File("/tmp/sub/a.b.File.js");
		assertThat(file).exists();
		file.delete();
		file = new File("/tmp/sub");
		assertThat(file).exists();
		file.delete();
	}
}
