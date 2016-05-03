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
import javax.tools.Diagnostic;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.TransientDataService;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractDataServiceVisitorTest {

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
	private AbstractDataServiceVisitor instance;
	
	@Mock
	ProcessingEnvironment environment;

	@Before
	public void setUp() {
		when(environment.getElementUtils()).thenReturn(elementUtils);
		when(environment.getFiler()).thenReturn(filer);
		when(environment.getMessager()).thenReturn(messager);
		when(environment.getTypeUtils()).thenReturn(typeUtils);
		instance = spy(new DataServiceVisitorJsBuilder(environment));
	}

	/**
	 * Test of visitType method, of class DataServiceVisitorJsBuilder.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testVisitType() throws IOException {
		System.out.println("visitType");
		TypeElement typeElement = mock(TypeElement.class);
		Writer writer = getMockWriter();
		Name qname = mock(Name.class);

		when(typeElement.getQualifiedName()).thenReturn(qname);
		when(qname.toString()).thenReturn("packageName.ClassName");
		doNothing().when(instance)._visitType(eq(typeElement), eq(writer));

		String res = instance.visitType(typeElement, writer);
		assertThat(res).isNull();
	}

	/**
	 * Test of visitType method, of class DataServiceVisitorJsBuilder.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testVisitTypeFail() throws IOException {
		System.out.println("visitTypeFail");
		TypeElement typeElement = mock(TypeElement.class);
		Writer writer = getMockWriter();
		Name qname = mock(Name.class);

		when(typeElement.getQualifiedName()).thenReturn(qname);
		when(qname.toString()).thenReturn("packageName.ClassName");
		StringBuilder stringBuilder = new StringBuilder();
		doReturn(stringBuilder).when(instance).getStringBuilder();
		doThrow(IOException.class).when(instance)._visitType(eq(typeElement), eq(writer));

		String res = instance.visitType(typeElement, writer);
		assertThat(res).isNull();

		ArgumentCaptor<Diagnostic.Kind> capturePrint = ArgumentCaptor.forClass(Diagnostic.Kind.class);
		verify(messager).printMessage(capturePrint.capture(), anyString());

		assertThat(capturePrint.getValue()).isEqualTo(Diagnostic.Kind.ERROR);
		assertThat(stringBuilder.toString()).isEqualTo("Cannot Create service : packageName.ClassName cause IOException on writer");

	}

	/**
	 * Test of getJsInstancename method, of class DataServiceVisitorJsBuilder.
	 */
	@Test
	public void testGetJsInstancename() {
		String result = instance.getJsInstancename("DefaultClassName");
		assertThat(result).isEqualTo("defaultClassName");

		result = instance.getJsInstancename("SpecifiedClassName");
		assertThat(result).isEqualTo("specifiedClassName");
	}

	/**
	 * Test of getJsClassname method, of class DataServiceVisitorJsBuilder.
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
	 * Test of browseAndWriteMethods method, of class DataServiceVisitorJsBuilder.
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testBrowseAndWriteMethods() throws IOException {
		System.out.println("browseAndWriteMethods");
		List<ExecutableElement> methodElements = new ArrayList<>();
		ExecutableElement element = mock(ExecutableElement.class);
		methodElements.add(element);
		String classname = "packageName.ClassName";
		Writer writer = getMockWriter();

		doReturn(false).doReturn(true).when(instance).isConsiderateMethod(anyCollection(), any(ExecutableElement.class));
		doNothing().when(instance).visitMethodElement(anyInt(), anyString(), any(ExecutableElement.class), any(Writer.class));
		// then
		int result = instance.browseAndWriteMethods(methodElements, classname, writer);
		assertThat(result).isEqualTo(0);
		result = instance.browseAndWriteMethods(methodElements, classname, writer);
		assertThat(result).isEqualTo(1);
	}

	/**
	 * Test of isConsiderateMethod method, of class DataServiceVisitorJsBuilder.
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
		assertThat(methodProceeds).hasSize(1);

		// inherited from Object
		methodProceeds.clear();
		result = instance.isConsiderateMethod(methodProceeds, methodElement);
		assertThat(result).isFalse();
		assertThat(methodProceeds).hasSize(0);

		// static
		methodProceeds.clear();
		modifiers.add(Modifier.STATIC);
		result = instance.isConsiderateMethod(methodProceeds, methodElement);
		assertThat(result).isFalse();
		assertThat(methodProceeds).hasSize(0);

		// non public
		methodProceeds.clear();
		modifiers.clear();
		result = instance.isConsiderateMethod(methodProceeds, methodElement);
		assertThat(result).isFalse();
		assertThat(methodProceeds).hasSize(0);

		// Transient
		methodProceeds.clear();
		modifiers.add(Modifier.PUBLIC);
		annotationMirrors.add(anno);
		annotationMirrors.add(annoTransient);
		result = instance.isConsiderateMethod(methodProceeds, methodElement);
		assertThat(result).isFalse();
		assertThat(methodProceeds).hasSize(0);
	}

	/**
	 * Test of getArgumentsType method, of class DataServiceVisitorJsBuilder.
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
	 * Test of getArguments method, of class DataServiceVisitorJsBuilder.
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
	 * Test of visit method, of class DataServiceVisitorJsBuilder.
	 */
	@Test
	public void testVisit_Element_Writer() {
		System.out.println("visit");
		String result = instance.visit(null, null);
		assertThat(result).isNull();
	}

	/**
	 * Test of visit method, of class DataServiceVisitorJsBuilder.
	 */
	@Test
	public void testVisit_Element() {
		System.out.println("visit");
		String result = instance.visit(null);
		assertThat(result).isNull();
	}

	/**
	 * Test of visitPackage method, of class DataServiceVisitorJsBuilder.
	 */
	@Test
	public void testVisitPackage() {
		System.out.println("visitPackage");
		String result = instance.visitPackage(null, null);
		assertThat(result).isNull();
	}

	/**
	 * Test of visitVariable method, of class DataServiceVisitorJsBuilder.
	 */
	@Test
	public void testVisitVariable() {
		System.out.println("visitVariable");
		String result = instance.visitVariable(null, null);
		assertThat(result).isNull();
	}

	/**
	 * Test of visitExecutable method, of class DataServiceVisitorJsBuilder.
	 */
	@Test
	public void testVisitExecutable() {
		System.out.println("visitExecutable");
		String result = instance.visitExecutable(null, null);
		assertThat(result).isNull();
	}

	/**
	 * Test of visitTypeParameter method, of class DataServiceVisitorJsBuilder.
	 */
	@Test
	public void testVisitTypeParameter() {
		System.out.println("visitTypeParameter");
		String result = instance.visitTypeParameter(null, null);
		assertThat(result).isNull();
	}

	/**
	 * Test of visitUnknown method, of class DataServiceVisitorJsBuilder.
	 */
	@Test
	public void testVisitUnknown() {
		System.out.println("visitUnknown");
		String result = instance.visitUnknown(null, null);
		assertThat(result).isNull();
	}

	@Test
	public void testGetStringBuilder() {
		StringBuilder stringBuilder = instance.getStringBuilder();
		assertThat(stringBuilder).isNotNull();
		assertThat(stringBuilder.length()).isEqualTo(0);
	}
	
	/**
	 * Test of getElementUtils method, of class.
	 */
	@Test
	public void test_getElementUtils() {
		System.out.println("getElementUtils");
		Elements elements = mock(Elements.class);
		when(environment.getElementUtils()).thenReturn(elements);
		Elements result = instance.getElementUtils();
		assertThat(result).isEqualTo(elements);
	}
	
	/**
	 * Test of getTypeUtils method, of class.
	 */
	@Test
	public void test_getTypeUtils() {
		System.out.println("getTypeUtils");
		Types types = mock(Types.class);
		when(environment.getTypeUtils()).thenReturn(types);
		Types result = instance.getTypeUtils();
		assertThat(result).isEqualTo(types);
	}
	
	Writer getMockWriter() throws IOException {
		Writer writer = mock(Writer.class);
		when(writer.append(anyString())).thenReturn(writer);
		return writer;
	}
}