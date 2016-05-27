/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.processors;

import java.io.IOException;
import org.ocelotds.annotations.TransientDataService;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import org.ocelotds.annotations.DataService;

/**
 * Visitor of class annoted org.ocelotds.annotations.DataService<br>
 * Generate javascript classes
 *
 * @author hhfrancois
 */
public abstract class AbstractDataServiceVisitor implements ElementVisitor<String, Writer> ,ProcessorConstants  {


	protected final ProcessingEnvironment environment;
	protected final Messager messager;

	/**
	 * Tools for log processor
	 */
	/**
	 * Tools for log processor
	 *
	 * @param environment
	 */
	public AbstractDataServiceVisitor(ProcessingEnvironment environment) {
		this.environment = environment;
		this.messager = environment.getMessager();
	}

	/**
	 *
	 * @param typeElement
	 * @param writer
	 * @return
	 */
	@Override
	public String visitType(TypeElement typeElement, Writer writer) {
		String classname = typeElement.getQualifiedName().toString();
		try {
			_visitType(typeElement, writer);
		} catch (IOException ex) {
			StringBuilder msg = getStringBuilder().append("Cannot Create service : ").append(classname).append(" cause IOException on writer");
			messager.printMessage(Diagnostic.Kind.ERROR, msg);
		}
		return null;
	}

	/**
	 *
	 * @param typeElement
	 * @param writer
	 * @throws IOException
	 */
	abstract void _visitType(TypeElement typeElement, Writer writer) throws IOException;

	/**
	 *
	 * @param first
	 * @param classname
	 * @param methodElement
	 * @param writer
	 * @throws IOException
	 */
	abstract void visitMethodElement(String classname, ExecutableElement methodElement, Writer writer) throws IOException;

	/**
	 * Compute the name of instance class
	 *
	 * @param classname
	 * @return
	 */
	String getJsInstancename(String classname) {
		char chars[] = classname.toCharArray();
		chars[0] = Character.toLowerCase(chars[0]);
		return new String(chars);
		//return Introspector.decapitalize(classname);
	}

	/**
	 * Compute the name of javascript class
	 *
	 * @param typeElement
	 * @return
	 */
	String getJsClassname(TypeElement typeElement) {
		DataService dsAnno = typeElement.getAnnotation(DataService.class);
		String name = dsAnno.name();
		if (name.isEmpty()) {
			name = typeElement.getSimpleName().toString();
		}
		return name;
	}

	/**
	 * browse valid methods and write equivalent js methods in writer
	 *
	 * @param methodElements
	 * @param classname
	 * @param writer
	 * @return
	 * @throws IOException
	 */
	void browseAndWriteMethods(List<ExecutableElement> methodElements, String classname, Writer writer) throws IOException {
		Collection<String> methodProceeds = new ArrayList<>();
		boolean first = true;
		for (ExecutableElement methodElement : methodElements) {
			if (isConsiderateMethod(methodProceeds, methodElement)) {
				if(!first) {
					writer.append(COMMA).append(CR);
				}
				visitMethodElement(classname, methodElement, writer);
				first = false;
			}
		}
	}
	
	/**
	 * Return methods from typeElement ordered by comparator
	 * @param typeElement
	 * @param comparator
	 * @return 
	 */
	protected List<ExecutableElement> getOrderedMethods(TypeElement typeElement, Comparator<ExecutableElement> comparator) {
		List<ExecutableElement> methods = ElementFilter.methodsIn(typeElement.getEnclosedElements());
		Collections.sort(methods, comparator);
		return methods;
	}

	/**
	 * Return if the method have to considerate<br>
	 * The method is public,<br>
	 * Not annotated by TransientDataService<br>
	 * Not static and not from Object herited.
	 *
	 * @param methodProceeds
	 * @param methodElement
	 * @return
	 */
	boolean isConsiderateMethod(Collection<String> methodProceeds, ExecutableElement methodElement) {
//		int argNum methodElement.getParameters().size();
		String signature = methodElement.getSimpleName().toString(); // + "(" + argNum + ")";
		// Check if method ith same signature has been already proceed.
		if (methodProceeds.contains(signature)) {
			return false;
		}
		// Herited from Object
		TypeElement objectElement = environment.getElementUtils().getTypeElement(Object.class.getName());
		if (objectElement.getEnclosedElements().contains(methodElement)) {
			return false;
		}
		// Static, not public ?
		if (!methodElement.getModifiers().contains(Modifier.PUBLIC) || methodElement.getModifiers().contains(Modifier.STATIC)) {
			return false;
		}
		// TransientDataService ?
		List<? extends AnnotationMirror> annotationMirrors = methodElement.getAnnotationMirrors();
		for (AnnotationMirror annotationMirror : annotationMirrors) {
			if (annotationMirror.getAnnotationType().toString().equals(TransientDataService.class.getName())) {
				return false;
			}
		}
		methodProceeds.add(signature);
		return true;
	}

	/**
	 * Get argument types list from method
	 *
	 * @param methodElement
	 * @return
	 */
	List<String> getArgumentsType(ExecutableElement methodElement) {
		ExecutableType methodType = (ExecutableType) methodElement.asType();
		List<String> res = new ArrayList<>();
		for (TypeMirror argumentType : methodType.getParameterTypes()) {
			res.add(argumentType.toString());
		}
		return res;
	}

	/**
	 * Get argument names list from method
	 *
	 * @param methodElement
	 * @return
	 */
	List<String> getArguments(ExecutableElement methodElement) {
		List<String> res = new ArrayList<>();
		for (VariableElement variableElement : methodElement.getParameters()) {
			res.add(variableElement.toString());
		}
		return res;
	}

	@Override
	public String visit(Element e, Writer p) {
		return null;
	}

	@Override
	public String visit(Element e) {
		return null;
	}

	@Override
	public String visitPackage(PackageElement e, Writer p) {
		return null;
	}

	@Override
	public String visitVariable(VariableElement e, Writer p) {
		return null;
	}

	@Override
	public String visitExecutable(ExecutableElement e, Writer p) {
		return null;
	}

	@Override
	public String visitTypeParameter(TypeParameterElement e, Writer p) {
		return null;
	}

	@Override
	public String visitUnknown(Element e, Writer p) {
		return null;
	}

	Elements getElementUtils() {
		return environment.getElementUtils();
	}

	Types getTypeUtils() {
		return environment.getTypeUtils();
	}

	StringBuilder getStringBuilder() {
		return new StringBuilder();
	}
}
