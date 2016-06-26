/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.processors;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.FileWriterServices;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.JsCacheRemove;
import org.ocelotds.annotations.JsCacheRemoves;
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
		doNothing().when(instance).initVisitors(null);
		instance.init(processingEnv);
		instance.visitorfwk = visitorfwk;
		instance.visitorNgfwk = visitorNgfwk;
		instance.visitorNofwk = visitorNofwk;
		instance.fws = fws;
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

	@Mock
	ElementVisitor visitorfwk;

	@Mock
	ElementVisitor visitorNofwk;

	@Mock
	ElementVisitor visitorNgfwk;

	@Mock
	FileWriterServices fws;

	/**
	 * Test of initVisitors method, of class.
	 */
	@Test
	public void initVisitorsTest() {
		System.out.println("initVisitors");
		doCallRealMethod().when(instance).initVisitors(null);
		instance.initVisitors(null);
		assertThat(instance.visitorfwk).isEqualTo(instance.visitorNofwk);
		instance.initVisitors("bad");
		assertThat(instance.visitorfwk).isEqualTo(instance.visitorNofwk);
		instance.initVisitors("ng");
		assertThat(instance.visitorfwk).isEqualTo(instance.visitorNgfwk);
	}
	/**
	 * Test of getJsDirectory method, of class.
	 */
	@Test
	public void getJsDirectoryTest() {
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
	public void getJsFrameworkTest() {
		System.out.println("getJsFramework");
		Map<String, String> map = new HashMap<>();
		Object result = instance.getJsFramework(map);
		assertThat(result).isNull();
		String fwk = "ng";
		map.put(ProcessorConstants.FRAMEWORK, fwk);
		result = instance.getJsFramework(map);
		assertThat(result).isEqualTo(fwk);
		map.put(ProcessorConstants.FRAMEWORK, null);
		result = instance.getJsFramework(map);
		assertThat(result).isNull();
		result = instance.getJsFramework(null);
		assertThat(result).isNull();
	}

	/**
	 * Test of isDone method, of class OcelotProcessor.
	 */
	@Test
	public void isDoneTest() {
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
	public void processIsDoneTest() {
		System.out.println("processingOver is true/false");
		Set<? extends TypeElement> annotations = mock(Set.class);
		RoundEnvironment roundEnv = mock(RoundEnvironment.class);
		when(roundEnv.processingOver()).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE).thenReturn(Boolean.TRUE);
		OcelotProcessor.setDone(Boolean.FALSE);
		boolean result = instance.process(annotations, roundEnv);
		assertThat(result).isTrue(); // true | false
		OcelotProcessor.setDone(Boolean.TRUE);
		result = instance.process(annotations, roundEnv);
		assertThat(result).isTrue(); // false | true
		OcelotProcessor.setDone(Boolean.TRUE);
		result = instance.process(annotations, roundEnv);
		assertThat(result).isTrue();// true | true
	}

	/**
	 * Test of process method, of class OcelotProcessor.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void processDataServiceTest() throws IOException {
		System.out.println("process");
		OcelotProcessor.setDone(Boolean.FALSE);
		RoundEnvironment roundEnv = mock(RoundEnvironment.class);
		Set<? extends TypeElement> annotations = mock(Set.class);
		Set elements = new HashSet<>();
		elements.add(mock(Element.class));
		elements.add(mock(Element.class));

		when(roundEnv.processingOver()).thenReturn(Boolean.FALSE);
		doNothing().when(instance).writeCoreInClassesOutput();
		doNothing().when(instance).writeCoreInDirectory(anyString(), anyString());
		doNothing().when(instance).processElement(any(Element.class));
		doReturn(Collections.EMPTY_SET).when(instance).getTypeElementContainsJsRemoveAnno(any(RoundEnvironment.class));
		when(roundEnv.getElementsAnnotatedWith(eq(DataService.class))).thenReturn(elements);
		
		boolean result = instance.process(annotations, roundEnv);
		assertThat(result).isTrue();
		verify(instance, times(2)).processElement(any(Element.class));
	}
	
	/**
	 * Test of process method, of class OcelotProcessor.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void processJsCacheRemoveTest() throws IOException {
		System.out.println("process");
		OcelotProcessor.setDone(Boolean.FALSE);
		RoundEnvironment roundEnv = mock(RoundEnvironment.class);
		Set<? extends TypeElement> annotations = mock(Set.class);
		Set elements = new HashSet<>();
		elements.add(mock(TypeElement.class));
		elements.add(mock(TypeElement.class));

		when(roundEnv.processingOver()).thenReturn(Boolean.FALSE);
		doNothing().when(instance).writeCoreInClassesOutput();
		doNothing().when(instance).writeCoreInDirectory(anyString(), anyString());
		doNothing().when(instance).createPropertiesFile(any(TypeElement.class), any(ElementVisitor.class));
		doReturn(elements).when(instance).getTypeElementContainsJsRemoveAnno(any(RoundEnvironment.class));
		when(roundEnv.getElementsAnnotatedWith(eq(DataService.class))).thenReturn(Collections.EMPTY_SET);
		
		boolean result = instance.process(annotations, roundEnv);
		assertThat(result).isTrue();
		verify(instance, times(2)).createPropertiesFile(any(TypeElement.class), any(ElementVisitor.class));
	}
	
	/**
	 * Test of createPropertiesFile method, of class.
	 */
	@Test
	public void createPropertiesFileTest() throws IOException {
		System.out.println("createPropertiesFile");
		TypeElement te = mock(TypeElement.class);
		Name name = mock(Name.class);
		ElementVisitor visitor = mock(ElementVisitor.class);
		Writer writer = mock(Writer.class);
		when(te.getSimpleName()).thenReturn(name);
		when(name.toString()).thenReturn("ClassName");
		when(fws.getFileObjectWriterInClassOutput(eq("package"), eq("ClassName.properties"))).thenReturn(writer).thenThrow(IOException.class);
		doReturn("package").when(instance).getPackagePath(eq(te));
		instance.createPropertiesFile(te, visitor);
		instance.createPropertiesFile(te, visitor);
		verify(te).accept(eq(visitor), eq(writer));
	}

	/**
	 * Test of getTypeElementContainsJsRemoveAnno method, of class.
	 */
	@Test
	public void getTypeElementContainsJsRemoveAnnoTest() {
		System.out.println("getTypeElementContainsJsRemoveAnno");
		RoundEnvironment roundEnv = mock(RoundEnvironment.class);
		ExecutableElement ee0 = mock(ExecutableElement.class);
		ExecutableElement ee1 = mock(ExecutableElement.class);
		ExecutableElement ee2 = mock(ExecutableElement.class);
		ExecutableElement ee3 = mock(ExecutableElement.class);
		TypeElement te1 = mock(TypeElement.class);
		TypeElement te2 = mock(TypeElement.class);
		TypeElement te3 = mock(TypeElement.class);
		Set elements = new HashSet<>();
		elements.add(ee0);
		elements.add(ee1);
		elements.add(ee2);
		elements.add(ee3);
		
		when(ee0.getEnclosingElement()).thenReturn(te1);
		when(ee1.getEnclosingElement()).thenReturn(te1);
		when(ee2.getEnclosingElement()).thenReturn(te2);
		when(ee3.getEnclosingElement()).thenReturn(te3);
		when(roundEnv.getElementsAnnotatedWith(eq(JsCacheRemove.class))).thenReturn(elements);
		when(roundEnv.getElementsAnnotatedWith(eq(JsCacheRemoves.class))).thenReturn(elements);
		Set<TypeElement> result = instance.getTypeElementContainsJsRemoveAnno(roundEnv);
		assertThat(result).hasSize(3);
	}

	/**
	 * Test of processElement method, of class.
	 */
	@Test
	public void processElementTest() {
		System.out.println("processElement");
		doNothing().when(instance).writeGeneratedJsInDiferentTargets(any(TypeElement.class));
		instance.processElement(mock(Element.class));
		instance.processElement(mock(TypeElement.class));
		verify(instance).writeGeneratedJsInDiferentTargets(any(TypeElement.class));
	}

	/**
	 * Test of writeGeneratedJsInDiferentTargets method, of class.
	 */
	@Test
	public void writeGeneratedJsInDiferentTargetsTestNoFwk() {
		System.out.println("writeGeneratedJsInDiferentTargets");
		TypeElement typeElement = mock(TypeElement.class);
		doReturn("packagePath").when(instance).getPackagePath(eq(typeElement));
		doReturn("filename.js").when(instance).getFilename(eq(typeElement), eq((String) null));
		doReturn("filename.ng.js").when(instance).getFilename(eq(typeElement), eq("ng"));
		instance.jsfwk = null;
		instance.writeGeneratedJsInDiferentTargets(typeElement);
		verify(instance).writeJsFileToJsDir(eq(typeElement), eq(visitorfwk), eq("packagePath"), eq("filename.js"), anyString());
		verify(instance).writeJsFile(eq(typeElement), eq(visitorNgfwk), eq("packagePath"), eq("filename.ng.js"));
		verify(instance).writeJsFile(eq(typeElement), eq(visitorNofwk), eq("packagePath"), eq("filename.js"));
	}
	
	/**
	 * Test of writeGeneratedJsInDiferentTargets method, of class.
	 */
	@Test
	public void writeGeneratedJsInDiferentTargetsTestFwk() {
		System.out.println("writeGeneratedJsInDiferentTargets");
		TypeElement typeElement = mock(TypeElement.class);
		doReturn("packagePath").when(instance).getPackagePath(eq(typeElement));
		doReturn("filename.ng.js").when(instance).getFilename(eq(typeElement), eq("ng"));
		doReturn("filename.js").when(instance).getFilename(eq(typeElement), eq((String) null));
		instance.jsfwk = "ng";
		instance.writeGeneratedJsInDiferentTargets(typeElement);
		verify(instance).writeJsFileToJsDir(eq(typeElement), eq(visitorfwk), eq("packagePath"), eq("filename.ng.js"), anyString());
		verify(instance).writeJsFile(eq(typeElement), eq(visitorNgfwk), eq("packagePath"), eq("filename.ng.js"));
		verify(instance).writeJsFile(eq(typeElement), eq(visitorNofwk), eq("packagePath"), eq("filename.js"));
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
		ElementVisitor visitor = mock(ElementVisitor.class);
		Writer writer = mock(Writer.class);
		when(fws.getFileObjectWriterInClassOutput(eq("packagePath"), eq("fn"))).thenReturn(writer).thenThrow(IOException.class);
		instance.writeJsFile(element, visitor, "packagePath", "fn");
		instance.writeJsFile(element, visitor, "packagePath", "fn");
		verify(element).accept(eq(visitor), eq(writer));
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
		ElementVisitor visitor = mock(ElementVisitor.class);
		Writer writer = mock(Writer.class);
		when(fws.getFileObjectWriter(eq("dir/srvs"), eq("packagePath.fn"))).thenReturn(writer).thenThrow(IOException.class);
		instance.writeJsFileToJsDir(element, visitor, "packagePath", "fn", "dir");
		instance.writeJsFileToJsDir(element, visitor, "packagePath", "fn", "dir");
		verify(element).accept(eq(visitor), eq(writer));
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

		String result = instance.getFilename(element, null);
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo("Test.js");

		result = instance.getFilename(element, "ng");
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo("Test.ng.js");
	}
	
	/**
	 * Test of writeCoreInDirectory method, of class.
	 */
	@Test
	public void writeCoreInDirectoryTestDoNothing() {
		System.out.println("writeCoreInDirectory");
		instance.writeCoreInDirectory(null, "ng");
		instance.writeCoreInDirectory(null, null);
		verify(fws, never()).copyFileToDir(anyString(), anyString(), anyString());
	}

	/**
	 * Test of writeCoreInDirectory method, of class.
	 */
	@Test
	public void writeCoreInDirectoryTestDoNoFwk() {
		System.out.println("writeCoreInDirectory");
		instance.writeCoreInDirectory("/tmp", null);
		verify(fws).copyFileToDir(anyString(), eq("core.js"), anyString());
	}

	/**
	 * Test of writeCoreInDirectory method, of class.
	 */
	@Test
	public void writeCoreInDirectoryTestNg() {
		System.out.println("writeCoreInDirectory");
		instance.writeCoreInDirectory("/tmp", "ng");
		verify(fws).copyFileToDir(anyString(), eq("core.ng.js"), anyString());
	}
	
	/**
	 * Test of writeCoreInClassesOutput method, of class.
	 */
	@Test
	public void writeCoreInClassesOutputTest() {
		System.out.println("writeCoreInClassesOutput");
		instance.writeCoreInClassesOutput();
		verify(fws).copyFileToClassesOutput(eq("/js"), eq("core.ng.min.js"));
		verify(fws).copyFileToClassesOutput(eq("/js"), eq("core.ng.js"));
		verify(fws).copyFileToClassesOutput(eq("/js"), eq("core.min.js"));
		verify(fws).copyFileToClassesOutput(eq("/js"), eq("core.js"));
	}
}
