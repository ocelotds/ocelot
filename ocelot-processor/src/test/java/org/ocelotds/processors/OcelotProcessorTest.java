/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.processors;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
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
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.annotations.DataService;
import org.slf4j.Logger;
import static org.ocelotds.processors.OcelotProcessor.ProviderType;

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
	@Spy
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
	 * @throws java.io.IOException
	 */
	@Test
	public void testProcess() throws IOException {
		System.out.println("process");
		OcelotProcessor.setDone(Boolean.FALSE);
		String js = "jsfilename";
		doReturn(js).when(instance).createJSServicesProvider();
		Writer writerjs = mock(Writer.class);
		when(writerjs.append(anyString())).thenReturn(writerjs);
		doReturn(writerjs).when(instance).getOpendResourceFileObjectWriter(eq(js), eq(ProviderType.JAVASCRIPT));
		
		Set<? extends TypeElement> annotations = mock(Set.class);
		Set elements = new HashSet<>();
		Element element = mock(Element.class);
		elements.add(element);
		when(element.toString()).thenReturn("GeneratedClass");

		RoundEnvironment roundEnv = mock(RoundEnvironment.class);
		when(roundEnv.getElementsAnnotatedWith(eq(DataService.class))).thenReturn(elements);
		when(roundEnv.processingOver()).thenReturn(Boolean.FALSE);

		boolean result = instance.process(annotations, roundEnv);
		assertThat(result).isTrue();
		ArgumentCaptor<String> captureString = ArgumentCaptor.forClass(String.class);
		verify(messager).printMessage(any(Diagnostic.Kind.class), captureString.capture());
		assertThat(captureString.getValue()).isEqualTo(" javascript generation class : GeneratedClass");
	}

	/**
	 * Test of process method, of class OcelotProcessor.
	 * @throws java.io.IOException
	 */
	@Test
	public void testProcessFail() throws IOException {
		System.out.println("processFail");
		Writer writer = mock(Writer.class);
		when(writer.append(anyString())).thenReturn(writer);
		doReturn(writer).when(instance).getOpendSourceFileObjectWriter(anyString());
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
		System.out.println("createJSServicesProvider");
		doNothing().when(instance).createServicesProvider(anyString(), any(ProviderType.class));
		String result = instance.createJSServicesProvider();
		assertThat(result).startsWith("srv_");
	}

	@Test
	public void testCreateServicesProviderJs() throws IOException {
		System.out.println("createServicesProviderJs");
		testCreateServicesProvider(ProviderType.JAVASCRIPT);
	}

	private void testCreateServicesProvider(ProviderType type) throws IOException {
		Writer writer = mock(Writer.class);
		when(writer.append(anyString())).thenReturn(writer);
		doReturn(writer).when(instance).getOpendSourceFileObjectWriter(anyString());
		String prefix = "srv_1234";
		instance.createServicesProvider(prefix, type);
		ArgumentCaptor<String> captureString = ArgumentCaptor.forClass(String.class);
		verify(writer, times(18)).append(captureString.capture());
		List<String> allValues = captureString.getAllValues();
		assertThat(allValues).isNotEmpty();
		assertThat(allValues.get(1)).isEqualTo(prefix);
		assertThat(allValues.get(6)).isEqualTo(type.name());
		assertThat(allValues.get(12)).isEqualTo(prefix);
		assertThat(allValues.get(14)).isEqualTo(type.getExtension());
	}

	@Test
	public void testCreateServicesProviderIOException() throws IOException {
		System.out.println("createServicesProvider");
		Writer writer = mock(Writer.class);
		when(writer.append(any(CharSequence.class))).thenThrow(new IOException("ERROR"));
		doReturn(writer).when(instance).getOpendSourceFileObjectWriter(anyString());
		instance.createServicesProvider("srv_1234", ProviderType.JAVASCRIPT);
		ArgumentCaptor<String> captureString = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Diagnostic.Kind> captureKind = ArgumentCaptor.forClass(Diagnostic.Kind.class);
		verify(messager).printMessage(captureKind.capture(), captureString.capture());
		assertThat(captureString.getValue()).isEqualTo("ERROR");
		assertThat(captureKind.getValue()).isEqualTo(Diagnostic.Kind.ERROR);
	}

	@Test
	public void testGetOpendSourceFileObjectWriter() throws IOException {
		System.out.println("getOpendFileObjectWriter");
		JavaFileObject fileObject = mock(JavaFileObject.class);
		Writer writer = mock(Writer.class);
		when(filer.createSourceFile(anyString())).thenReturn(fileObject);
		when(fileObject.openWriter()).thenReturn(writer);
		Writer result = instance.getOpendSourceFileObjectWriter("test");
		assertThat(result).isEqualTo(writer);
	}

	@Test
	public void testGetOpendResourceFileObjectWriter() throws IOException {
		System.out.println("getOpendResourceFileObjectWriter");
		JavaFileObject fileObject = mock(JavaFileObject.class);
		Writer writer = mock(Writer.class);
		when(filer.createResource(eq(StandardLocation.CLASS_OUTPUT), anyString(), anyString())).thenReturn(fileObject);
		when(fileObject.openWriter()).thenReturn(writer);
		Writer result = instance.getOpendResourceFileObjectWriter("test", ProviderType.JAVASCRIPT);
		assertThat(result).isEqualTo(writer);
	}

}