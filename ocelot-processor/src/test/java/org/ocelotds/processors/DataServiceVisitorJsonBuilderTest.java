/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.processors;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class DataServiceVisitorJsonBuilderTest {

	@Mock
	private Messager messager;

	@Mock
	private Filer filer;

	@Mock
	private Elements elementUtils;

	@Mock
	private Types typeUtils;

	private DataServiceVisitorJsonBuilder instance;

	@Before
	public void setUp() {
		ProcessingEnvironment environment = mock(ProcessingEnvironment.class);
		when(environment.getElementUtils()).thenReturn(elementUtils);
		when(environment.getFiler()).thenReturn(filer);
		when(environment.getMessager()).thenReturn(messager);
		when(environment.getTypeUtils()).thenReturn(typeUtils);
		instance = spy(new DataServiceVisitorJsonBuilder(environment, true));
	}

	/**
	 * Test of visitType method, of class DataServiceVisitorJsonBuilder.
	 */
	@Test
	public void testVisitType() throws IOException {
		System.out.println("visitType");
		System.out.println("_visitType");
		TypeElement typeElement = mock(TypeElement.class);
		doNothing().when(instance).createClassComment(eq(typeElement), any(Writer.class));
		doReturn(0).when(instance).browseAndWriteMethods(anyListOf(ExecutableElement.class), anyString(), any(Writer.class));
		doReturn("ClassName").when(instance).getJsClassname(eq(typeElement));

		Name qname = mock(Name.class);
		List methodElements = new ArrayList();

		when(typeElement.getQualifiedName()).thenReturn(qname);
		when(qname.toString()).thenReturn("packageName.ClassName");
		when(typeElement.getEnclosedElements()).thenReturn(methodElements);

		Writer writer = getMockWriter();
		instance._visitType(typeElement, writer);
		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(14)).append(captureAppend.capture());
		List<String> appends = captureAppend.getAllValues();
		assertThat(appends.get(6)).isEqualTo("className");
		
		writer = getMockWriter();
		doReturn(false).when(instance).isFirst();
		instance._visitType(typeElement, writer);
		captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(15)).append(captureAppend.capture());
		
	}

	/**
	 * Test of visitMethodElement method, of class DataServiceVisitorJsonBuilder.
	 */
	@Test
	public void testVisitMethodElement() throws Exception {
		System.out.println("visitMethodElement");
		Writer writer = getMockWriter();
		String classname = "packageName.ClassName";
		ExecutableElement methodElement = mock(ExecutableElement.class);
		Name name = mock(Name.class);
		TypeMirror tm = mock(TypeMirror.class);
		List<String> argumentsType = new ArrayList<>();
		List<String> arguments = new ArrayList<>();

		when(methodElement.getSimpleName()).thenReturn(name);
		when(name.toString()).thenReturn("ClassName");
		doReturn(argumentsType).when(instance).getArgumentsType(eq(methodElement));
		doReturn(arguments).when(instance).getArguments(eq(methodElement));
		when(methodElement.getReturnType()).thenReturn(tm);
		
		instance.visitMethodElement(0, classname, methodElement, writer);
		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(28)).append(captureAppend.capture());
		
		writer = getMockWriter();
		argumentsType.add(String.class.getName());
		arguments.add("str");
		instance.visitMethodElement(0, classname, methodElement, writer);
		captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(34)).append(captureAppend.capture());

		writer = getMockWriter();
		argumentsType.add(String.class.getName());
		arguments.add("str2");
		instance.visitMethodElement(0, classname, methodElement, writer);
		captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(42)).append(captureAppend.capture());

		writer = getMockWriter();
		instance.visitMethodElement(1, classname, methodElement, writer);
		captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(43)).append(captureAppend.capture());
	}

	/**
	 * Test of createClassComment method, of class DataServiceVisitorJsonBuilder.
	 */
	@Test
	public void testCreateClassComment() throws Exception {
		System.out.println("createClassComment");
		TypeElement typeElement = mock(TypeElement.class);
		Elements elements = mock(Elements.class);
		doReturn(elements).when(instance).getElementUtils();
		when(elements.getDocComment(eq(typeElement))).thenReturn(null).thenReturn("Comments");

		Writer writer = getMockWriter();
		instance.createClassComment(typeElement, writer);
		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(0)).append(captureAppend.capture());
		
		writer = getMockWriter();
		instance.createClassComment(typeElement, writer);
		captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(0)).append(captureAppend.capture());
	}

	/**
	 * Test of createMethodComment method, of class DataServiceVisitorJsonBuilder.
	 */
	@Test
	public void testCreateMethodComment() throws Exception {
		System.out.println("createMethodComment");
		ExecutableElement methodElement = mock(ExecutableElement.class);
		Elements elements = mock(Elements.class);
		doReturn(elements).when(instance).getElementUtils();
		when(elements.getDocComment(eq(methodElement))).thenReturn(null).thenReturn("Comments");

		Writer writer = getMockWriter();
		instance.createMethodComment(methodElement, writer);
		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(0)).append(captureAppend.capture());
		
		writer = getMockWriter();
		instance.createMethodComment(methodElement, writer);
		captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(0)).append(captureAppend.capture());
	}

	Writer getMockWriter() throws IOException {
		Writer writer = mock(Writer.class);
		when(writer.append(anyString())).thenReturn(writer);
		return writer;
	}
}
