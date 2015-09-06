/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.processors;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
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
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.JsCacheResult;
import org.ocelotds.annotations.TransientDataService;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class DataServiceVisitorTest {

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
	private DataServiceVisitor instance;

	@Before
	public void setUp() {
		ProcessingEnvironment environment = mock(ProcessingEnvironment.class);
		when(environment.getElementUtils()).thenReturn(elementUtils);
		when(environment.getFiler()).thenReturn(filer);
		when(environment.getMessager()).thenReturn(messager);
		when(environment.getTypeUtils()).thenReturn(typeUtils);
		instance = new DataServiceVisitor(environment);
	}

	/**
	 * Test of visitType method, of class DataServiceVisitor.
	 *
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
		verify(writer, times(10)).append(captureAppend.capture());
		List<String> appends = captureAppend.getAllValues();
		assertThat(appends.get(1)).isEqualTo("ClassName");
		assertThat(appends.get(4)).isEqualTo("packageName.ClassName");
		assertThat(appends.get(7)).isEqualTo("ClassName");
	}

	/**
	 * Test of visitMethodElement method, of class DataServiceVisitor.
	 * TODO
	 * @throws java.io.IOException
	 */
//	@Test
	public void visitMethodElement() throws IOException {
		System.out.println("visitType");
		boolean first = true;
		Collection<String> methodProceeds = null;
		String classname = null;
		String jsclsname = null;
		ExecutableElement methodElement = null;
		Writer writer = null;
		// given

		// when
		// then
		instance.visitMethodElement(first, methodProceeds, classname, jsclsname, methodElement, writer);
	}

	/**
	 * Test of getJsClassname method, of class DataServiceVisitor.
	 */
	@Test
	public void testGetJsClassname() {
		TypeElement typeElement = mock(TypeElement.class);
		DataService ds = mock(DataService.class);
		Name name = mock(Name.class);

		when(typeElement.getAnnotation(eq(DataService.class))).thenReturn(ds);
		when(typeElement.getSimpleName()).thenReturn(name);
		when(name.toString()).thenReturn("DefaultClassName");
		when(ds.name()).thenReturn("").thenReturn("SpecifiedClassName");

		String result = instance.getJsClassname(typeElement);
		assertThat(result).isEqualTo("DefaultClassName");

		result = instance.getJsClassname(typeElement);
		assertThat(result).isEqualTo("SpecifiedClassName");
	}

	/**
	 * Test of createClassComment method, of class DataServiceVisitor.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testCreateClassCommentWithError() throws IOException {
		System.out.println("createClassComment");
		TypeElement typeElement = mock(TypeElement.class);
		Writer writer = mock(Writer.class);

		when(elementUtils.getDocComment(any(TypeElement.class))).thenReturn("");
		when(writer.append(anyString())).thenThrow(IOException.class);

		instance.createClassComment(typeElement, writer);
	}

	/**
	 * Test of createClassComment method, of class DataServiceVisitor.
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
	 * Test of computeComment method, of class DataServiceVisitor.
	 *
	 */
	@Test
	public void testComputeComment() {
		System.out.println("computeComment");
		String comment = "Line1\nLine2\nLine3";
		String result = instance.computeComment(comment);
		assertThat(result).isEqualTo("Line1\n *Line2\n *Line3");
	}

	/**
	 * Test of createClassComment method, of class DataServiceVisitor.
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
		assertThat(appends.get(1)).isEqualTo("Line1\n *Line2");

	}

	/**
	 * Test of isConsiderateMethod method, of class DataServiceVisitor.
	 */
	@Test
	public void testIsConsiderateMethod() {
		System.out.println("isConsiderateMethod");
		String methodname = "methodName";
		Collection<String> methodProceeds = new ArrayList<>();
		ExecutableElement methodElement = mock(ExecutableElement.class);
		VariableElement var0 = mock(VariableElement.class);
		VariableElement var1 = mock(VariableElement.class);
		List ves = Arrays.asList(var0, var1);
		Name name = mock(Name.class);
		TypeElement objectElement = mock(TypeElement.class);
		List enclosedElements = mock(List.class);
		Set modifiers = new HashSet();
		List annotationMirrors = new ArrayList();
		AnnotationMirror anno = mock(AnnotationMirror.class);
		AnnotationMirror annoTransient = mock(AnnotationMirror.class);
		DeclaredType declaredType0 = mock(DeclaredType.class);
		DeclaredType declaredType1 = mock(DeclaredType.class);

		when(anno.getAnnotationType()).thenReturn(declaredType0);
		when(annoTransient.getAnnotationType()).thenReturn(declaredType1);
		when(declaredType0.toString()).thenReturn(DataService.class.getName());
		when(declaredType1.toString()).thenReturn(TransientDataService.class.getName());
		when(name.toString()).thenReturn(methodname);
		when(methodElement.getParameters()).thenReturn(ves);
		when(methodElement.getSimpleName()).thenReturn(name);
		when(methodElement.getModifiers()).thenReturn(modifiers);
		when(methodElement.getAnnotationMirrors()).thenReturn(annotationMirrors);
		when(elementUtils.getTypeElement(eq("java.lang.Object"))).thenReturn(objectElement);
		when(objectElement.getEnclosedElements()).thenReturn(enclosedElements);
		when(enclosedElements.contains(anyObject())).thenReturn(Boolean.FALSE)
				  .thenReturn(Boolean.TRUE)
				  .thenReturn(Boolean.FALSE)
				  .thenReturn(Boolean.FALSE);
		modifiers.add(Modifier.PUBLIC);

		// first time
		boolean result = instance.isConsiderateMethod(methodProceeds, methodElement);
		assertThat(result).isTrue();
		assertThat(methodProceeds).hasSize(1);

		// alreadyProcess
		result = instance.isConsiderateMethod(methodProceeds, methodElement);
		assertThat(result).isFalse();

		// inherited from Object
		methodProceeds.clear();
		result = instance.isConsiderateMethod(methodProceeds, methodElement);
		assertThat(result).isFalse();

		// static
		methodProceeds.clear();
		modifiers.add(Modifier.STATIC);
		result = instance.isConsiderateMethod(methodProceeds, methodElement);
		assertThat(result).isFalse();

		// non public
		methodProceeds.clear();
		modifiers.clear();
		result = instance.isConsiderateMethod(methodProceeds, methodElement);
		assertThat(result).isFalse();

		// Transient
		methodProceeds.clear();
		modifiers.add(Modifier.PUBLIC);
		annotationMirrors.add(anno);
		annotationMirrors.add(annoTransient);
		result = instance.isConsiderateMethod(methodProceeds, methodElement);
		assertThat(result).isFalse();
	}

	/**
	 * Test of getArgumentsType method, of class DataServiceVisitor.
	 */
	@Test
	public void testGetArgumentsType() {
		System.out.println("getArgumentsType");
		ExecutableElement methodElement = mock(ExecutableElement.class);
		ExecutableType methodType = mock(ExecutableType.class);
		TypeMirror var0 = mock(TypeMirror.class);
		TypeMirror var1 = mock(TypeMirror.class);
		List ves = Arrays.asList(var0, var1);

		when(methodElement.asType()).thenReturn(methodType);
		when(var0.toString()).thenReturn("boolean");
		when(var1.toString()).thenReturn("java.lang.String");
		when(methodType.getParameterTypes()).thenReturn(ves);

		List<String> expResult = Arrays.asList("boolean", "java.lang.String");
		List<String> result = instance.getArgumentsType(methodElement);
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of getArguments method, of class DataServiceVisitor.
	 */
	@Test
	public void testGetArguments() {
		System.out.println("getArguments");
		ExecutableElement methodElement = mock(ExecutableElement.class);
		VariableElement var0 = mock(VariableElement.class);
		VariableElement var1 = mock(VariableElement.class);
		List ves = Arrays.asList(var0, var1);

		when(var0.toString()).thenReturn("a");
		when(var1.toString()).thenReturn("b");
		when(methodElement.getParameters()).thenReturn(ves);

		List<String> expResult = Arrays.asList("a", "b");
		List<String> result = instance.getArguments(methodElement);
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of createMethodComment method, of class DataServiceVisitor.
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
		when(elementUtils.getDocComment(any(ExecutableElement.class))).thenReturn("Default Comment");
		// then
		instance.createMethodComment(methodElement, argumentsName, argumentsType, returnType, writer);
		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		// 
		verify(writer, times(10)).append(captureAppend.capture());
		List<String> appends = captureAppend.getAllValues();
		assertThat(appends.get(2)).isEqualTo("Default Comment");
		assertThat(appends.get(5)).isEqualTo("boolean");
		assertThat(appends.get(7)).isEqualTo("b");
	}

	/**
	 * Test of createMethodComment method, of class DataServiceVisitor.
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

	/**
	 * Test of createMethodBody method, of class DataServiceVisitor.
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

		Collection<String> arguments = Arrays.asList("a", "b", "c", "d");
		Iterator<String> iterator = arguments.iterator();

		Writer writer = mock(Writer.class);
		when(writer.append(anyString())).thenReturn(writer);

		instance.createMethodBody(classname, methodElement, iterator, writer);

		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(12)).append(captureAppend.capture());
		List<String> appends = captureAppend.getAllValues();
		assertThat(appends.get(1)).isEqualTo(methodname);
		assertThat(appends.get(3)).isEqualTo("		var id = \"" + classMethodHash + "_\" + JSON.stringify([");
		assertThat(appends.get(7)).isEqualTo("\"a\",\"b\",\"c\",\"d\"");
		assertThat(appends.get(9)).isEqualTo("a,b,c,d");
	}

	/**
	 * Test of createMethodBody method, of class DataServiceVisitor.
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

		Collection<String> arguments = Arrays.asList("a", "b", "c", "d");
		Iterator<String> iterator = arguments.iterator();

		Writer writer = mock(Writer.class);
		when(writer.append(anyString())).thenReturn(writer);

		instance.createMethodBody(classname, methodElement, iterator, writer);

		ArgumentCaptor<String> captureAppend = ArgumentCaptor.forClass(String.class);
		verify(writer, times(12)).append(captureAppend.capture());
		List<String> appends = captureAppend.getAllValues();
		assertThat(appends.get(0)).isEqualTo("		var op = \"");
		assertThat(appends.get(1)).isEqualTo(methodname);
		assertThat(appends.get(2)).isEqualTo("\";\n");
		assertThat(appends.get(3)).isEqualTo("		var id = \"" + classMethodHash + "_\" + JSON.stringify([");
		assertThat(appends.get(4)).isEqualTo("a,b,c,d");
		assertThat(appends.get(5)).isEqualTo("]).md5();\n");
		assertThat(appends.get(6)).isEqualTo("		return OcelotPromiseFactory.createPromise(this.ds, id, op, [");
		assertThat(appends.get(7)).isEqualTo("\"a\",\"b\",\"c\",\"d\"");
		assertThat(appends.get(8)).isEqualTo("], [");
		assertThat(appends.get(9)).isEqualTo("a,b,c,d");
		assertThat(appends.get(10)).isEqualTo("]");
		assertThat(appends.get(11)).isEqualTo(");\n");
	}

	/**
	 * Test of visit method, of class DataServiceVisitor.
	 */
	@Test
	public void testVisit_Element_Writer() {
		System.out.println("visit");
		String result = instance.visit(null, null);
		assertThat(result).isNull();
	}

	/**
	 * Test of visit method, of class DataServiceVisitor.
	 */
	@Test
	public void testVisit_Element() {
		System.out.println("visit");
		String result = instance.visit(null);
		assertThat(result).isNull();
	}

	/**
	 * Test of visitPackage method, of class DataServiceVisitor.
	 */
	@Test
	public void testVisitPackage() {
		System.out.println("visitPackage");
		String result = instance.visitPackage(null, null);
		assertThat(result).isNull();
	}

	/**
	 * Test of visitVariable method, of class DataServiceVisitor.
	 */
	@Test
	public void testVisitVariable() {
		System.out.println("visitVariable");
		String result = instance.visitVariable(null, null);
		assertThat(result).isNull();
	}

	/**
	 * Test of visitExecutable method, of class DataServiceVisitor.
	 */
	@Test
	public void testVisitExecutable() {
		System.out.println("visitExecutable");
		String result = instance.visitExecutable(null, null);
		assertThat(result).isNull();
	}

	/**
	 * Test of visitTypeParameter method, of class DataServiceVisitor.
	 */
	@Test
	public void testVisitTypeParameter() {
		System.out.println("visitTypeParameter");
		String result = instance.visitTypeParameter(null, null);
		assertThat(result).isNull();
	}

	/**
	 * Test of visitUnknown method, of class DataServiceVisitor.
	 */
	@Test
	public void testVisitUnknown() {
		System.out.println("visitUnknown");
		String result = instance.visitUnknown(null, null);
		assertThat(result).isNull();
	}
}
