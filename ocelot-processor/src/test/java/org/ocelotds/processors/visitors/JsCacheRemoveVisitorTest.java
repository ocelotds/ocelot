/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ocelotds.processors.visitors;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class JsCacheRemoveVisitorTest {

	JsCacheRemoveVisitor instance;
	
	@Mock
	ProcessingEnvironment environment;
	
	@Before
	public void init() {
		instance = spy(new JsCacheRemoveVisitor(environment));
	}

	/**
	 * Test of visitType method, of class JsCacheRemoveVisitor.
	 */
	@Test
	public void testVisitType() {
		System.out.println("visitType");
		TypeElement te = mock(TypeElement.class);
		Element elt0 = mock(ExecutableElement.class);
		when(elt0.getKind()).thenReturn(ElementKind.METHOD);
		Element elt1 = mock(ExecutableElement.class);
		when(elt1.getKind()).thenReturn(ElementKind.METHOD);
		Element elt2 = mock(Element.class);
		
		Writer writer = mock(Writer.class);
		List elts = Arrays.asList(elt0, elt1, elt2);
		when(te.getEnclosedElements()).thenReturn(elts);
		doReturn(null).when(instance).visitExecutable(any(ExecutableElement.class), eq(writer));
		Void result = instance.visitType(te, writer);
		verify(instance, times(2)).visitExecutable(any(ExecutableElement.class), eq(writer));
		assertThat(result).isNull();
	}

	/**
	 * Test of visitExecutable method, of class JsCacheRemoveVisitor.
	 * @throws java.io.IOException
	 */
	@Test
	public void testVisitExecutable() throws IOException {
		System.out.println("visitExecutable");
		Writer writer;
		ExecutableElement exElt = mock(ExecutableElement.class);
		Name name = mock(Name.class);
		when(name.toString()).thenReturn("methodName");
		when(exElt.getSimpleName()).thenReturn(name);
		TypeElement te = mock(TypeElement.class);
		when(te.toString()).thenReturn("package.ClassName");
		when(exElt.getEnclosingElement()).thenReturn(te);

		doNothing().doThrow(IOException.class).when(instance).appendParameters(anyList(), any(Writer.class));
		
		writer = new StringWriter();
		instance.visitExecutable(exElt, writer);
		assertThat(writer.toString()).isEqualTo("package.ClassName.methodName=\n");

		writer = new StringWriter();
		instance.visitExecutable(exElt, writer);
		assertThat(writer.toString()).isEqualTo("package.ClassName.methodName=");
	}
	
	/**
	 * Test of appendParameters method, of class.
	 */
	@Test
	public void appendParametersTest() throws IOException {
		System.out.println("appendParameters");
		Writer writer;
		VariableElement ve = mock(VariableElement.class);
		when(ve.toString()).thenReturn("arg");
		
		writer = new StringWriter();
		instance.appendParameters(Arrays.asList(ve), writer);
		assertThat(writer.toString()).isEqualTo("arg");
		
		writer = new StringWriter();
		instance.appendParameters(Arrays.asList(ve, ve), writer);
		assertThat(writer.toString()).isEqualTo("arg,arg");
		
		writer = new StringWriter();
		instance.appendParameters(Arrays.asList(ve, ve, ve), writer);
		assertThat(writer.toString()).isEqualTo("arg,arg,arg");
	}

	/**
	 * Test of visit method, of class JsCacheRemoveVisitor.
	 */
	@Test
	public void testVisit_Element_Writer() {
		System.out.println("visit");
		Void result = instance.visit(null, null);
		assertThat(result).isNull();
	}

	/**
	 * Test of visit method, of class JsCacheRemoveVisitor.
	 */
	@Test
	public void testVisit_Element() {
		System.out.println("visit");
		Void result = instance.visit(null);
		assertThat(result).isNull();
	}

	/**
	 * Test of visitPackage method, of class JsCacheRemoveVisitor.
	 */
	@Test
	public void testVisitPackage() {
		System.out.println("visitPackage");
		Void result = instance.visitPackage(null, null);
		assertThat(result).isNull();
	}

	/**
	 * Test of visitVariable method, of class JsCacheRemoveVisitor.
	 */
	@Test
	public void testVisitVariable() {
		System.out.println("visitVariable");
		Void result = instance.visitVariable(null, null);
		assertThat(result).isNull();
	}

	/**
	 * Test of visitTypeParameter method, of class JsCacheRemoveVisitor.
	 */
	@Test
	public void testVisitTypeParameter() {
		System.out.println("visitTypeParameter");
		Void result = instance.visitTypeParameter(null, null);
		assertThat(result).isNull();
	}

	/**
	 * Test of visitUnknown method, of class JsCacheRemoveVisitor.
	 */
	@Test
	public void testVisitUnknown() {
		System.out.println("visitUnknown");
		Void result = instance.visitUnknown(null, null);
		assertThat(result).isNull();
	}

}