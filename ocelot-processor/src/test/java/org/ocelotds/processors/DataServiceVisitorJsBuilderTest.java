/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.processors;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
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
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.KeyMaker;
import org.ocelotds.annotations.JsCacheResult;
import org.ocelotds.processors.stringDecorators.StringDecorator;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class DataServiceVisitorJsBuilderTest {

	@Mock
	private Messager messager;

	@Mock
	private Filer filer;

	@Mock
	private Elements elementUtils;

	@Mock
	private Types typeUtils;

	private DataServiceVisitorJsBuilder instance;

	@Before
	public void setUp() {
		ProcessingEnvironment environment = mock(ProcessingEnvironment.class);
		when(environment.getElementUtils()).thenReturn(elementUtils);
		when(environment.getFiler()).thenReturn(filer);
		when(environment.getMessager()).thenReturn(messager);
		when(environment.getTypeUtils()).thenReturn(typeUtils);
		instance = spy(new DataServiceVisitorJsBuilder(environment));
	}

	/**
	 * Test of _visitType method, of class DataServiceVisitorJsBuilder.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void test_VisitType() throws IOException {
		System.out.println("_visitType");
		TypeElement typeElement = mock(TypeElement.class);
		Writer writer = getMockWriter();
		doNothing().when(instance).createClassComment(eq(typeElement), eq(writer));
		doReturn(0).when(instance).browseAndWriteMethods(anyListOf(ExecutableElement.class), anyString(), eq(writer));
		doReturn("ClassName").when(instance).getJsClassname(eq(typeElement));

		Name qname = mock(Name.class);
		List methodElements = new ArrayList();

		when(typeElement.getQualifiedName()).thenReturn(qname);
		when(qname.toString()).thenReturn("packageName.ClassName");
		when(typeElement.getEnclosedElements()).thenReturn(methodElements);

		instance._visitType(typeElement, writer);
		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(27)).append(captureAppend.capture());
		List<String> appends = captureAppend.getAllValues();
		assertThat(appends.get(1)).isEqualTo("className");
		assertThat(appends.get(7)).isEqualTo("packageName.ClassName");
		assertThat(appends.get(23)).isEqualTo("className");
	}

	/**
	 * Test of visitMethodElement method, of class DataServiceVisitorJsBuilder. TODO
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void visitMethodElement() throws IOException {
		System.out.println("visitMethodElement");
		Writer writer = getMockWriter();
		String classname = "packageName.ClassName";
		ExecutableElement methodElement = mock(ExecutableElement.class);
		Name name = mock(Name.class);
		TypeMirror tm = mock(TypeMirror.class);
		List<String> argumentsType = new ArrayList<>();
		List<String> arguments = new ArrayList<>();

		doNothing().when(instance).createMethodComment(any(ExecutableElement.class), anyList(), anyList(), any(TypeMirror.class), any(Writer.class));
		doNothing().when(instance).createMethodBody(eq(classname), any(ExecutableElement.class), anyListOf(String.class), any(Writer.class));
		when(methodElement.getSimpleName()).thenReturn(name);
		when(name.toString()).thenReturn("ClassName");
		doReturn(argumentsType).when(instance).getArgumentsType(eq(methodElement));
		doReturn(arguments).when(instance).getArguments(eq(methodElement));
		when(methodElement.getReturnType()).thenReturn(tm);

		instance.visitMethodElement(0, classname, methodElement, writer);
		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(7)).append(captureAppend.capture());

		writer = getMockWriter();
		argumentsType.add(String.class.getName());
		arguments.add("str");
		instance.visitMethodElement(0, classname, methodElement, writer);
		captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(8)).append(captureAppend.capture());

		writer = getMockWriter();
		argumentsType.add(String.class.getName());
		arguments.add("str2");
		instance.visitMethodElement(0, classname, methodElement, writer);
		captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(10)).append(captureAppend.capture());

		writer = getMockWriter();
		instance.visitMethodElement(1, classname, methodElement, writer);
		captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(12)).append(captureAppend.capture());
	}

	/**
	 * Test of createClassComment method, of class DataServiceVisitorJsBuilder.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testCreateClassCommentWithInterfaceComment() throws IOException {
		System.out.println("createClassComment");
		TypeElement typeElement = mock(TypeElement.class);
		Writer writer = getMockWriter();
		TypeMirror t0 = mock(TypeMirror.class);
		TypeMirror t1 = mock(TypeMirror.class);
		TypeElement el0 = mock(TypeElement.class);
		TypeElement el1 = mock(TypeElement.class);
		List interfaces = Arrays.asList(t0, t1);

		when(elementUtils.getDocComment(any(TypeElement.class))).thenReturn(null);
		when(typeElement.getInterfaces()).thenReturn(interfaces);
		when(typeUtils.asElement(eq(t0))).thenReturn(el0);
		when(typeUtils.asElement(eq(t1))).thenReturn(el1);
		when(elementUtils.getDocComment(eq(el0))).thenReturn("Default Comment0");
		when(elementUtils.getDocComment(eq(el1))).thenReturn(null);

		instance.createClassComment(typeElement, writer);
		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(6)).append(captureAppend.capture());
		List<String> appends = captureAppend.getAllValues();
		assertThat(appends.get(3)).isEqualTo("Default Comment0");
	}

	/**
	 * Test of createClassComment method, of class DataServiceVisitorJsBuilder.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testCreateClassCommentWithDefaultComment() throws IOException {
		System.out.println("createClassComment");
		TypeElement typeElement = mock(TypeElement.class);
		Writer writer = getMockWriter();

		when(elementUtils.getDocComment(any(TypeElement.class))).thenReturn("Line1\nLine2");

		instance.createClassComment(typeElement, writer);
		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(6)).append(captureAppend.capture());
		List<String> appends = captureAppend.getAllValues();
		assertThat(appends.get(3)).isEqualTo("Line1\n *Line2");

	}

	/**
	 * Test of createMethodComment method, of class DataServiceVisitorJsBuilder.
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
		Writer writer = getMockWriter();
		// when
		// return type != void
		when(returnType.toString()).thenReturn("void");
		// no comment on method
		when(elementUtils.getDocComment(any(ExecutableElement.class))).thenReturn("Default Comment\n Second line\n @param test");
		// then
		instance.createMethodComment(methodElement, argumentsName, argumentsType, returnType, writer);
		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		// 
		verify(writer, times(16)).append(captureAppend.capture());
		List<String> appends = captureAppend.getAllValues();
		assertThat(appends.get(5)).isEqualTo("Default Comment\n\t\t * Second line");
		assertThat(appends.get(9)).isEqualTo("boolean");
		assertThat(appends.get(11)).isEqualTo("b");
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
		Writer writer = getMockWriter();
		// when
		// return type != void
		when(returnType.toString()).thenReturn("java.lang.String");
		// no comment on method
		when(elementUtils.getDocComment(any(ExecutableElement.class))).thenReturn(null);
		// then
		instance.createMethodComment(methodElement, argumentsName, argumentsType, returnType, writer);
		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		// 
		verify(writer, times(17)).append(captureAppend.capture());
		List<String> appends = captureAppend.getAllValues();
		assertThat(appends.get(5)).isEqualTo("boolean");
		assertThat(appends.get(7)).isEqualTo("b");
		assertThat(appends.get(11)).isEqualTo("java.lang.String");
	}

	/**
	 * Test of createMethodBody method, of class DataServiceVisitorJsBuilder.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testCreateMethodBody() throws IOException {
		System.out.println("createMethodBody");
		String classname = "packageName.ClassName";
		String methodname = "methodName";
		Name name = mock(Name.class);
		when(name.toString()).thenReturn(methodname);
		String classMethodHash = new KeyMaker().getMd5(classname + "." + methodname);

		JsCacheResult jcr = mock(JsCacheResult.class);
		when(jcr.keys()).thenReturn(new String[]{"a.c", "b.i", "d"});

		ExecutableElement methodElement = mock(ExecutableElement.class);
		when(methodElement.getSimpleName()).thenReturn(name);
		when(methodElement.getAnnotation(eq(JsCacheResult.class))).thenReturn(jcr);

		List<String> arguments = Arrays.asList("a", "b", "c", "d");

		Writer writer = getMockWriter();

		instance.createMethodBody(classname, methodElement, arguments, writer);

		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(22)).append(captureAppend.capture());
		List<String> appends = captureAppend.getAllValues();
		assertThat(appends.get(3)).isEqualTo(classMethodHash);
		assertThat(appends.get(7)).isEqualTo("(a)?a.c:null,(b)?b.i:null,d");
		assertThat(appends.get(13)).isEqualTo(methodname);
		assertThat(appends.get(16)).isEqualTo("\"a\",\"b\",\"c\",\"d\"");
		assertThat(appends.get(18)).isEqualTo("a,b,c,d");
	}

	/**
	 * Test of createMethodBody method, of class DataServiceVisitorJsBuilder.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testCreateMethodBodyAllArg() throws IOException {
		System.out.println("createMethodBody");
		String classname = "packageName.ClassName";
		String methodname = "methodName";
		Name name = mock(Name.class);
		when(name.toString()).thenReturn(methodname);
		String classMethodHash = new KeyMaker().getMd5(classname + "." + methodname);

		JsCacheResult jcr = mock(JsCacheResult.class);
		when(jcr.keys()).thenReturn(new String[]{"*"});

		ExecutableElement methodElement = mock(ExecutableElement.class);
		when(methodElement.getSimpleName()).thenReturn(name);
		when(methodElement.getAnnotation(eq(JsCacheResult.class))).thenReturn(jcr);

		List<String> arguments = Arrays.asList("a", "b", "c", "d");

		Writer writer = getMockWriter();

		instance.createMethodBody(classname, methodElement, arguments, writer);

		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(22)).append(captureAppend.capture());
		List<String> appends = captureAppend.getAllValues();
		assertThat(appends.get(3)).isEqualTo(classMethodHash);
		assertThat(appends.get(7)).isEqualTo("a,b,c,d");
		assertThat(appends.get(13)).isEqualTo(methodname);
		assertThat(appends.get(16)).isEqualTo("\"a\",\"b\",\"c\",\"d\"");
		assertThat(appends.get(18)).isEqualTo("a,b,c,d");
	}

	@Test
	public void testConsiderateNotAllArgs() {
		boolean result = instance.considerateAllArgs(null);
		assertThat(result).isTrue();

		result = instance.considerateAllArgs(new JsCacheResultLiteral(new String[]{}));
		assertThat(result).isFalse();

		result = instance.considerateAllArgs(new JsCacheResultLiteral());
		assertThat(result).isFalse();

		result = instance.considerateAllArgs(new JsCacheResultLiteral("a"));
		assertThat(result).isFalse();

		result = instance.considerateAllArgs(new JsCacheResultLiteral("a", "b"));
		assertThat(result).isFalse();

		result = instance.considerateAllArgs(new JsCacheResultLiteral("*"));
		assertThat(result).isTrue();

		result = instance.considerateAllArgs(new JsCacheResultLiteral("*", "b"));
		assertThat(result).isTrue();

	}

	@Test
	public void testComputeArgumentsFromListAndDecorateWith() {
		List<String> list = Arrays.asList("a", "b", "c", "d");
		String result = instance.stringJoinAndDecorate(null, ",", null);
		assertThat(result).isEqualTo("");
		
		result = instance.stringJoinAndDecorate(list, ",", null);
		assertThat(result).isEqualTo("a,b,c,d");

		result = instance.stringJoinAndDecorate(list, " ", null);
		assertThat(result).isEqualTo("a b c d");

		result = instance.stringJoinAndDecorate(list, ",", new UnderscoreDecorator());
		assertThat(result).isEqualTo("_a_,_b_,_c_,_d_");
	}
	
	private static class UnderscoreDecorator implements StringDecorator {

		@Override
		public String decorate(String str) {
				return "_"+str+"_";
		}
	}

	Writer getMockWriter() throws IOException {
		Writer writer = mock(Writer.class);
		when(writer.append(anyString())).thenReturn(writer);
		return writer;
	}
}
