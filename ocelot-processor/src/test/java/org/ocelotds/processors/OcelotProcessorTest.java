/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.processors;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.annotations.DataService;
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
		instance.init(processingEnv);
		OcelotProcessor.setDone(false);
	}
	
	@Mock
	private Logger logger;

	@InjectMocks
	private OcelotProcessor instance;

	@Mock
	private ProcessingEnvironment processingEnv;

	@Mock
	private Filer filer;

	@Mock
	private Messager messager;

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
	 * @throws java.io.IOException
	 */
	@Test
	public void testProcess() throws IOException {
		System.out.println("process");
		JavaFileObject jsProviderFileObject = mock(JavaFileObject.class);
		Writer jsProviderWriter = mock(Writer.class);
		when(jsProviderFileObject.openWriter()).thenReturn(jsProviderWriter);
		when(filer.createSourceFile(anyString())).thenReturn(jsProviderFileObject);
		JavaFileObject jsFileObject = mock(JavaFileObject.class);
		Writer jsWriter = mock(Writer.class);
		when(jsFileObject.openWriter()).thenReturn(jsWriter);
		when(filer.createResource(eq(StandardLocation.CLASS_OUTPUT), eq(""), anyString())).thenReturn(jsFileObject);
		Set<? extends TypeElement> annotations = mock(Set.class);
		RoundEnvironment roundEnv = mock(RoundEnvironment.class);
		Set elements = new HashSet<>();
		Element element = mock(Element.class);
		elements.add(element);
		when(element.toString()).thenReturn("GeneratedClass");
		when(roundEnv.getElementsAnnotatedWith(eq(DataService.class))).thenReturn(elements);
		when(roundEnv.processingOver()).thenReturn(Boolean.FALSE);
		OcelotProcessor.setDone(Boolean.FALSE);
		boolean result = instance.process(annotations, roundEnv);
		assertThat(result).isTrue();
		ArgumentCaptor<String> captureString = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Diagnostic.Kind> captureKind = ArgumentCaptor.forClass(Diagnostic.Kind.class);
		verify(messager).printMessage(captureKind.capture(), captureString.capture());
		assertThat(captureString.getValue()).isEqualTo(" javascript generation class : GeneratedClass");
		assertThat(captureKind.getValue()).isEqualTo(Diagnostic.Kind.MANDATORY_WARNING);
	}

	/**
	 * Test of process method, of class OcelotProcessor.
	 * @throws java.io.IOException
	 */
	@Test
	public void testProcessFail() throws IOException {
		System.out.println("process");
		JavaFileObject jsProviderFileObject = mock(JavaFileObject.class);
		Writer jsProviderWriter = mock(Writer.class);
		when(jsProviderFileObject.openWriter()).thenReturn(jsProviderWriter);
		when(filer.createSourceFile(anyString())).thenReturn(jsProviderFileObject);
		JavaFileObject jsFileObject = mock(JavaFileObject.class);
		Writer jsWriter = mock(Writer.class);
		when(jsFileObject.openWriter()).thenReturn(jsWriter);
		when(filer.createResource(eq(StandardLocation.CLASS_OUTPUT), eq(""), anyString())).thenThrow(new IOException("ERROR"));
		Set<? extends TypeElement> annotations = mock(Set.class);
		RoundEnvironment roundEnv = mock(RoundEnvironment.class);
		when(roundEnv.processingOver()).thenReturn(Boolean.FALSE);
		OcelotProcessor.setDone(Boolean.FALSE);
		boolean result = instance.process(annotations, roundEnv);
		assertThat(result).isTrue();
		ArgumentCaptor<String> captureString = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Diagnostic.Kind> captureKind = ArgumentCaptor.forClass(Diagnostic.Kind.class);
		verify(messager).printMessage(captureKind.capture(), captureString.capture());
		assertThat(captureString.getValue()).isEqualTo("ERROR");
		assertThat(captureKind.getValue()).isEqualTo(Diagnostic.Kind.ERROR);
	}
	
	@Test
	public void testCreateJSServicesProvider() throws IOException {
		JavaFileObject fileObject = mock(JavaFileObject.class);
		Writer writer = mock(Writer.class);
		when(fileObject.openWriter()).thenReturn(writer);
		when(filer.createSourceFile(anyString())).thenReturn(fileObject);
		String result = instance.createJSServicesProvider();
		assertThat(result).startsWith("srv_");
		assertThat(result).endsWith(".js");
		ArgumentCaptor<String> captureString = ArgumentCaptor.forClass(String.class);
		verify(writer, times(10)).append(captureString.capture());
		assertThat(captureString.getAllValues()).isNotEmpty();
	}

	@Test
	public void testCreateJSServicesProviderIOException() throws IOException {
		JavaFileObject fileObject = mock(JavaFileObject.class);
		Writer writer = mock(Writer.class);
		when(writer.append(any(CharSequence.class))).thenThrow(new IOException("ERROR"));
		when(fileObject.openWriter()).thenReturn(writer);
		when(filer.createSourceFile(anyString())).thenReturn(fileObject);
		instance.createJSServicesProvider();
		ArgumentCaptor<String> captureString = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Diagnostic.Kind> captureKind = ArgumentCaptor.forClass(Diagnostic.Kind.class);
		verify(messager).printMessage(captureKind.capture(), captureString.capture());
		assertThat(captureString.getValue()).isEqualTo("ERROR");
		assertThat(captureKind.getValue()).isEqualTo(Diagnostic.Kind.ERROR);
	}
}