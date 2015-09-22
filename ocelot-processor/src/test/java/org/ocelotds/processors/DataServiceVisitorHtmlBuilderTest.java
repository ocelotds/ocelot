/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.processors;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.ocelotds.annotations.DataService;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class DataServiceVisitorHtmlBuilderTest {

	@Mock
	private Messager messager;

	@Mock
	private Filer filer;

	@Mock
	private Elements elementUtils;

	@Mock
	private Types typeUtils;

	@Mock
	private Logger logger;

	@Mock
	private DataServiceVisitorHtmlBuilder instance;
	@Before

	public void setUp() {
		ProcessingEnvironment environment = mock(ProcessingEnvironment.class);
		when(environment.getElementUtils()).thenReturn(elementUtils);
		when(environment.getFiler()).thenReturn(filer);
		when(environment.getMessager()).thenReturn(messager);
		when(environment.getTypeUtils()).thenReturn(typeUtils);
		instance = new DataServiceVisitorHtmlBuilder(environment);
	}

	/**
	 * Test of visitType method, of class DataServiceVisitorHtmlBuilder.
	 * @throws java.io.IOException
	 */
	@Test
	public void testVisitType() throws IOException {
		System.out.println("visitType");
		TypeElement typeElement = mock(TypeElement.class);
		Writer writer = mock(Writer.class);
		Name qname = mock(Name.class);
		Name sname = mock(Name.class);
		List methodElements = new ArrayList();
		DataService ds = mock(DataService.class);

		when(typeElement.getAnnotation(eq(DataService.class))).thenReturn(ds);
		when(typeElement.getQualifiedName()).thenReturn(qname);
		when(typeElement.getSimpleName()).thenReturn(sname);
		when(qname.toString()).thenReturn("packageName.ClassName");
		when(sname.toString()).thenReturn("ClassName");
		when(typeElement.getEnclosedElements()).thenReturn(methodElements);
		when(writer.append(anyString())).thenReturn(writer);
		when(ds.name()).thenReturn("");

		String result = instance.visitType(typeElement, writer);
		assertThat(result).isNull();
		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(8)).append(captureAppend.capture());
		List<String> appends = captureAppend.getAllValues();
		assertThat(appends.get(3)).isEqualTo("packageName.ClassName");
	}

	/**
	 * Test of visitMethodElement method, of class DataServiceVisitorHtmlBuilder.
	 */
//	@Test
	public void testVisitMethodElement() throws Exception {
		System.out.println("visitMethodElement");
		boolean first = false;
		Collection<String> methodProceeds = null;
		String classname = "";
		String jsclsname = "";
		ExecutableElement methodElement = null;
		Writer writer = null;
		instance.visitMethodElement(first, methodProceeds, classname, jsclsname, methodElement, writer);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}

	/**
	 * Test of createClassComment method, of class DataServiceVisitorHtmlBuilder.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testCreateClassCommentWithInterfaceComment() throws IOException {
		System.out.println("createClassComment");
		TypeElement typeElement = mock(TypeElement.class);
		Writer writer = mock(Writer.class);
		TypeMirror t0 = mock(TypeMirror.class);
		TypeMirror t1 = mock(TypeMirror.class);
		TypeElement el0 = mock(TypeElement.class);
		TypeElement el1 = mock(TypeElement.class);
		List interfaces = Arrays.asList(t0, t1);

		when(elementUtils.getDocComment(any(TypeElement.class))).thenReturn(null);
		when(typeElement.getInterfaces()).thenReturn(interfaces);
		when(typeUtils.asElement(eq(t0))).thenReturn(el0);
		when(typeUtils.asElement(eq(t1))).thenReturn(el1);
		when(writer.append(anyString())).thenReturn(writer);
		when(elementUtils.getDocComment(eq(el0))).thenReturn("Default Comment0");
		when(elementUtils.getDocComment(eq(el1))).thenReturn(null);

		instance.createClassComment(typeElement, writer);
		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(3)).append(captureAppend.capture());
		List<String> appends = captureAppend.getAllValues();
		assertThat(appends.get(1)).isEqualTo("Default Comment0");
	}

	/**
	 * Test of createClassComment method, of class DataServiceVisitorHtmlBuilder.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testCreateClassCommentWithDefaultComment() throws IOException {
		System.out.println("createClassComment");
		TypeElement typeElement = mock(TypeElement.class);
		Writer writer = mock(Writer.class);

		when(elementUtils.getDocComment(any(TypeElement.class))).thenReturn("Line1\nLine2");
		when(writer.append(anyString())).thenReturn(writer);

		instance.createClassComment(typeElement, writer);
		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(3)).append(captureAppend.capture());
		List<String> appends = captureAppend.getAllValues();
		assertThat(appends.get(1)).isEqualTo("Line1\n<br/>&nbsp;*Line2");

	}

	/**
	 * Test of escapeLtGt method, of class DataServiceVisitorHtmlBuilder.
	 */
	@Test
	public void testEscapeLtGt() {
		System.out.println("escapeLtGt");
		String comment = "Comment<Generic>";
		String expResult = "Comment&lt;Generic&gt;";
		String result = instance.escapeLtGt(comment);
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of createMethodComment method, of class DataServiceVisitorHtmlBuilder.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testCreateMethodCommentWithDefaultCommentAndReturnVoid() throws IOException {
		System.out.println("createMethodComment");
		// given
		ExecutableElement methodElement = mock(ExecutableElement.class);
		List<String> argumentsName = Arrays.asList("b");
		List<String> argumentsType = Arrays.asList("boolean");
		TypeMirror returnType = mock(TypeMirror.class);
		Writer writer = mock(Writer.class);
		// when
		// return type != void
		when(returnType.toString()).thenReturn("void");
		when(writer.append(anyString())).thenReturn(writer);
		// no comment on method
		when(elementUtils.getDocComment(any(ExecutableElement.class))).thenReturn("Default Comment\n Second line\n @param test");
		// then
		instance.createMethodComment(methodElement, argumentsName, argumentsType, returnType, writer);
		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		// 
		verify(writer, times(10)).append(captureAppend.capture());
		List<String> appends = captureAppend.getAllValues();
		assertThat(appends.get(2)).isEqualTo("Default Comment\n<br/>&nbsp;&nbsp;&nbsp;&nbsp;* Second line");
		assertThat(appends.get(5)).isEqualTo("boolean");
		assertThat(appends.get(7)).isEqualTo("b");
	}

	/**
	 * Test of createMethodComment method, of class DataServiceVisitorJsBuilder.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testCreateMethodCommentWithNoDefaultCommentAndReturnType() throws IOException {
		System.out.println("createMethodComment");
		// given
		ExecutableElement methodElement = mock(ExecutableElement.class);
		List<String> argumentsName = Arrays.asList("b");
		List<String> argumentsType = Arrays.asList("boolean");
		TypeMirror returnType = mock(TypeMirror.class);
		Writer writer = mock(Writer.class);
		// when
		// return type != void
		when(returnType.toString()).thenReturn("java.lang.String");
		when(writer.append(anyString())).thenReturn(writer);
		// no comment on method
		when(elementUtils.getDocComment(any(ExecutableElement.class))).thenReturn(null);
		// then
		instance.createMethodComment(methodElement, argumentsName, argumentsType, returnType, writer);
		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		// 
		verify(writer, times(10)).append(captureAppend.capture());
		List<String> appends = captureAppend.getAllValues();
		assertThat(appends.get(2)).isEqualTo("boolean");
		assertThat(appends.get(4)).isEqualTo("b");
		assertThat(appends.get(7)).isEqualTo("java.lang.String");
	}

}